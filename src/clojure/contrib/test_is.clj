;;; test_is.clj: test framework for Clojure

;; by Stuart Sierra, http://stuartsierra.com/
;; January 15, 2009

;; Thanks to Chas Emerick, Allen Rohner, and Stuart Halloway for
;; contributions and suggestions.

;; Copyright (c) Stuart Sierra, 2008. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.



(comment
  ;; Inspired by many Common Lisp test frameworks and clojure/test,
  ;; this file is a Clojure test framework.
  ;;
  ;; Define tests as :test metadata on your fns.  Use the "is" macro
  ;; for assertions.  Examples:

  (defn add2
    ([x] (+ x 2))
    {:test (fn [] (is (= (add2 3) 5))
	     (is (= (add2 -4) -2)
		 (is (> (add2 50) 50))))})

  ;; You can also define tests in isolation with the "deftest" macro:

  (deftest test-new-fn
    (is (= (new-fn) "Awesome")))

  ;; You can test that a function throws an exception with the
  ;; "is thrown?" form:

  (defn factorial
    ([n] (cond
	  (zero? n) 1	       ; 0!=1 is often defined for convenience
	  (> n 0) (* n (factorial (dec n)))
	  :else (throw (IllegalArgumentException. "Negative factorial"))))
    {:test (fn [] (is (= (factorial 3) 6))
	     (is (= (factorial 6) 720))
	     (is (thrown? IllegalArgumentException (factorial -2))))}) 

  ;; Run tests with (run-tests). As in any language with macros, you
  ;; may need to recompile functions after changing a macro
  ;; definition.
  ;;
  ;; If you want write a bunch of tests with the same predicate, use
  ;; "are", which takes a template and applies it inside "is".
  ;;
  ;; Examples:

  (deftest test-addition
    (are (= _1 _2)
	 3 (+ 2 1)
	 4 (+ 2 2)
	 5 (+ 4 1)))

  (deftest test-predicates
    (are _ ;; the template is just an underscore
	 (true? true)
	 (false? false)
	 (nil? nil)))

) ;; end comment block



(ns clojure.contrib.test-is
  (:require [clojure.contrib.template :as temp]
            [clojure.contrib.stacktrace :as stack]))


(defonce
  #^{:doc "True by default.  If set to false, no test functions will
   be created by deftest, set-test, or with-test.  Use this to omit
   tests when compiling or loading production code."}
  *load-tests* true)



;;; PRIVATE GLOBALS

(def *report-counters* nil)	  ; bound to a ref of a map in test-ns

(def *testing-vars* (list))  ; bound to hierarchy of vars being tested

(def *testing-contexts* (list))	   ; bound to strings of test contexts

(def *initial-report-counters*  ; used to initialize *report-counters*
     {:test 0, :pass 0, :fail 0, :error 0})



;;; UTILITIES USED BY TEST REPORTING FUNCTIONS

(defn file-position
  "Returns a vector [filename line-number] for the nth call up the
  stack."
  [n]
  (let [s (nth (.getStackTrace (new java.lang.Throwable)) n)]
    [(.getFileName s) (.getLineNumber s)]))

(defn testing-vars-str
  "Returns a string representation of the current test.  Renders names
  in *testing-vars* as a list, then the source file and line of
  current assertion."
  []
  (let [[file line] (file-position 4)]
    (str
     ;; Uncomment to include namespace in failure report:
     ;;(ns-name (:ns (meta (first *testing-vars*)))) "/ "
     (reverse (map #(:name (meta %)) *testing-vars*))
     " (" file ":" line ")")))

(defn testing-contexts-str
  "Returns a string representation of the current test context. Joins
  strings in *testing-contexts* with spaces."
  []
  (apply str (interpose " " (reverse *testing-contexts*))))

(defn report-count
  "Increments the named counter in *report-counters*, a ref to a map.
  Does nothing if *report-counters* is nil."
  [name]
  (when *report-counters*
    (dosync (commute *report-counters* assoc name
                     (inc (or (*report-counters* name) 0))))))



;;; TEST RESULT REPORTING

(defmulti
  #^{:doc "Handles the result of a single assertion.  'event' is one
   of :pass, :fail, or :error.  'msg' is a comment string associated
   with the assertion.  'expected' and 'actual' are quoted forms,
   which will be rendered with pr-str.

   Special case: if 'event' is :info, just the 'msg' will be
   printed.

   You can rebind this function during testing to plug in your own
   test-reporting framework."}
  report (fn [event msg expected actual] event))

(defmethod report :info [event msg expected actual]
  (newline)
  (println msg))

(defmethod report :pass [event msg expected actual]
  (report-count :pass))

(defmethod report :fail [event msg expected actual]
  (report-count :fail)
  (println "\nFAIL in" (testing-vars-str))
  (when (seq *testing-contexts*) (println (testing-contexts-str)))
  (when msg (println msg))
  (println "expected:" (pr-str expected))
  (println "  actual:" (pr-str actual)))

(defmethod report :error [event msg expected actual]
  (report-count :error)
  (println "\nERROR in" (testing-vars-str))
  (when (seq *testing-contexts*) (println (testing-contexts-str)))
  (when msg (println msg))
  (println "expected:" (pr-str expected))
  (print "  actual: ")
  (if (instance? Throwable actual)
    (stack/print-cause-trace actual 5)
    (prn actual)))



;;; ASSERTION METHODS

;; You don't call these, but you can add methods to extend the 'is'
;; macro.  These define different kinds of tests, based on the first
;; symbol in the test expression.

(defmulti assert-expr 
  (fn [msg form]
    (cond
     (nil? form) :always-fail
     (seq? form) (first form)
     :else :single)))

;; Not currently used -- how can we determine if predicate is a normal
;; function and not a macro or method call?
(defmethod assert-expr :predicate [msg form]
  ;; Generic assertion for any functional predicate.  The 'expected'
  ;; argument to 'report' contains the original form, the 'actual'
  ;; argument contains the form with all its sub-forms evaluated.  If
  ;; the predicate returns false, the 'actual' form is wrapped in
  ;; (not...).
  (let [args (rest form)
        pred (first form)]
     `(let [values# (list ~@args)
            result# (apply ~pred values#)]
        (if result#
          (report :pass ~msg '~form (cons ~pred values#))
          (report :fail ~msg '~form (list '~'not (cons '~pred values#))))
        result#)))

(defmethod assert-expr :always-fail [msg form]
  ;; nil test: always fail
  `(report :fail ~msg nil nil))

(defmethod assert-expr :default [msg form]
  ;; Evaluate any expression and pass if it is logical true.
  `(let [value# ~form]
     (if value#
       (report :pass ~msg '~form value#)
       (report :fail ~msg '~form value#))
     value#))

(defmethod assert-expr 'instance? [msg form]
  ;; Test if x is an instance of y.
  `(let [klass# ~(nth form 1)
         object# ~(nth form 2)]
     (let [result# (instance? klass# object#)]
       (if result#
         (report :pass ~msg '~form (class object#))
         (report :fail ~msg '~form (class object#)))
       result#)))

(defmethod assert-expr 'thrown? [msg form]
  ;; (is (thrown? c expr))
  ;; Asserts that evaluating expr throws an exception of class c.
  ;; Returns the exception thrown.
  (let [klass (second form)
        body (rrest form)]
    `(try ~@body
          (report :fail ~msg '~form nil)
          (catch ~klass e#
            (report :pass ~msg '~form e#)
            e#))))

;; New assertions coming soon:
;; * thrown-with-msg?


;;; CATCHING UNEXPECTED EXCEPTIONS

(defmacro try-expr
  "Used by the 'is' macro to catch unexpected exceptions.
  You don't call this."
  [msg form]
  `(try ~(assert-expr msg form)
        (catch Throwable t#
          (report :error ~msg '~form t#))))



;;; ASSERTION MACROS

;; you use these in your tests

(defmacro is
  "Generic assertion macro.  'form' is any predicate test.
  'msg' is an optional message to attach to the assertion.
  
  Example: (is (= 4 (+ 2 2)) \"Two plus two should be 4\")

  Special form (is (thrown? c body)) checks that an instance of c is
  thrown from body, fails if not; then returns the thing thrown."
  ([form] `(is ~form nil))
  ([form msg] `(try-expr ~msg ~form)))

(defmacro are
  "Experimental.  May be removed in the future.
  Checks multiple assertions with a template expression.
  Example: (are (= _1 _2)  2 (+ 1 1),  4 (+ 2 2))
  See clojure.contrib.template for documentation of templates."
  [expr & args]
  `(temp/do-template (is ~expr) ~@args))

(defmacro testing
  "Adds a new string to the list of testing contexts.  May be nested,
  but must occur inside a test function (deftest)."
  [string & body]
  `(binding [*testing-contexts* (conj *testing-contexts* ~string)]
     ~@body))



;;; DEFINING TESTS INDEPENDENT OF FUNCTIONS

(defmacro deftest
  "Defines a test function with no arguments.  Test functions may call
  other tests, so tests may be composed.  If you compose tests, you
  should also define a function named test-ns-hook; run-tests will
  call test-ns-hook instead of testing all vars.

  Note: Actually, the test body goes in the :test metadata on the var,
  and the real function (the value of the var) calls test-var on
  itself.

  When *load-tests* is false, deftest is ignored."
  [name & body]
  (when *load-tests*
    `(def ~(with-meta name {:test `(fn [] ~@body)})
          (fn [] (test-var (var ~name))))))


(defmacro set-test
  "Experimental.
  Sets :test metadata of the named var to a fn with the given body.
  The var must already exist.  Does not modify the value of the var.

  When *load-tests* is false, set-test is ignored."
  [name & body]
  (when *load-tests*
    `(alter-meta! (var ~name) assoc :test (fn [] ~@body))))


(defmacro with-test
  "Takes any definition form (that returns a Var) as the first argument.
  Remaining body goes in the :test metadata function for that Var.

  When *load-tests* is false, only evaluates the definition, ignoring
  the tests."
  [definition & body]
  (if *load-tests*
    `(doto ~definition (alter-meta! assoc :test (fn [] ~@body)))
    definition))



;;; RUNNING TESTS

(defn test-var
  "If v has a function in its :test metadata, calls that function,
  with *testing-vars* bound to (conj *testing-vars* v)."
  [v]
  (when-let [t (:test (meta v))]
    (binding [*testing-vars* (conj *testing-vars* v)]
      (report-count :test)
      (try (t)
	   (catch Throwable e
	     (report :error "Uncaught exception, not in assertion."
		     nil e))))))

(defn test-all-vars
  "Calls test-var on every var interned in the namespace."
  [ns]
  (doseq [v (vals (ns-interns ns))]
    (test-var v)))

(defn test-ns
  "If the namespace defines a function named test-ns-hook, calls that.
  Otherwise, calls test-all-vars on the namespace. Returns a map of
  counts for :test, :pass, :fail, and :error results.

  'ns' is a namespace object or a symbol."
  [ns]
  (binding [*report-counters* (ref *initial-report-counters*)]
    (let [ns (if (symbol? ns) (find-ns ns) ns)]
      (report :info (str "Testing " ns) nil nil)
      ;; If ns has a test-ns-hook function, call that:
      (if-let [v (find-var (symbol (str (ns-name ns)) "test-ns-hook"))]
	((var-get v))
        ;; Otherwise, just test every var in the ns.
        (test-all-vars ns)))
    @*report-counters*))

(defn print-results
  "Prints formatted results message based on the reported
  counts in r."
  [r]
  (println "\nRan" (:test r) "tests containing"
           (+ (:pass r) (:fail r) (:error r)) "assertions.")
  (println (:fail r) "failures," (:error r) "errors."))

(defn run-tests
  "Runs all tests in the given namespaces; prints results.
  Defaults to current namespace if none given."
  ([] (run-tests *ns*))
  ([& namespaces]
     (print-results (apply merge-with + (map test-ns namespaces)))))

(defn run-all-tests
  "Runs all tests in all namespaces; prints results."
  []
  (apply run-tests (all-ns)))
