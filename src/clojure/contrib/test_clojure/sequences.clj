;;  Copyright (c) Frantisek Sodomka. All rights reserved.  The use and
;;  distribution terms for this software are covered by the Eclipse Public
;;  License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;  be found in the file epl-v10.html at the root of this distribution.  By
;;  using this software in any fashion, you are agreeing to be bound by the
;;  terms of this license.  You must not remove this notice, or any other,
;;  from this software.
;;

(ns clojure.contrib.test-clojure.sequences
  (:use clojure.contrib.test-is))

;; *** Helper functions ***

(defn exception []
  (throw (new Exception "Exception which should never occur")))


;; *** Tests ***

(deftest test-seq
  (are (= _1 _2)
    (seq nil) nil
    (seq [nil]) '(nil)

    (seq ()) nil
    (seq []) nil
    (seq #{}) nil
    (seq {}) nil
    (seq "") nil
    (seq (into-array [])) nil

    (seq (list 1 2)) '(1 2)
    (seq [1 2]) '(1 2)
    (seq (sorted-set 1 2)) '(1 2)
    (seq (sorted-map :a 1 :b 2)) '([:a 1] [:b 2])
    (seq "abc") '(\a \b \c)
    (seq (into-array [1 2])) '(1 2)
  )
)

(deftest test-cons
  (is (thrown? IllegalArgumentException (cons 1 2)))
  (are (= _1 _2)
    (cons 1 nil) '(1)
    (cons nil nil) '(nil)

    (cons \a nil) '(\a)
    (cons \a "") '(\a)
    (cons \a "bc") '(\a \b \c)

    (cons 1 ()) '(1)
    (cons 1 '(2 3)) '(1 2 3)

    (cons 1 []) [1]
    (cons 1 [2 3]) [1 2 3]

    (cons 1 #{}) '(1)
    (cons 1 (sorted-set 2 3)) '(1 2 3)

    (cons 1 (into-array [])) '(1)
    (cons 1 (into-array [2 3])) '(1 2 3)
  )
)

(deftest test-first
  (is (thrown? IllegalArgumentException (first)))
  (is (thrown? IllegalArgumentException (first true)))
  (is (thrown? IllegalArgumentException (first false)))
  (is (thrown? IllegalArgumentException (first 1)))
  (is (thrown? IllegalArgumentException (first 1 2)))
  (is (thrown? IllegalArgumentException (first \a)))
  (is (thrown? IllegalArgumentException (first 's)))
  (is (thrown? IllegalArgumentException (first :k)))
  (are (= _1 _2)
    (first nil) nil

    ; string
    (first "") nil
    (first "a") \a
    (first "abc") \a

    ; list
    (first ()) nil
    (first '(1)) 1
    (first '(1 2 3)) 1

    (first '(nil)) nil
    (first '(1 nil)) 1
    (first '(nil 2)) nil
    (first '(())) ()
    (first '(() nil)) ()
    (first '(() 2 nil)) ()

    ; vector
    (first []) nil
    (first [1]) 1
    (first [1 2 3]) 1

    (first [nil]) nil
    (first [1 nil]) 1
    (first [nil 2]) nil
    (first [[]]) []
    (first [[] nil]) []
    (first [[] 2 nil]) []

    ; set
    (first #{}) nil
    (first #{1}) 1
    ;(first #{1 2 3}) 1

    (first #{nil}) nil
    ;(first #{1 nil}) 1
    ;(first #{nil 2}) nil
    (first #{#{}}) #{}
    ;(first #{#{} nil}) #{}
    ;(first #{#{} 2 nil}) #{}

    ; map
    (first {}) nil
    (first (sorted-map :a 1)) '(:a 1)
    (first (sorted-map :a 1 :b 2 :c 3)) '(:a 1)

    ; array
    (first (into-array [])) nil
    (first (into-array [1])) 1
    (first (into-array [1 2 3])) 1
    (first (to-array [nil])) nil
    (first (to-array [1 nil])) 1
    (first (to-array [nil 2])) nil
  )
)

(deftest test-rest
  (is (thrown? IllegalArgumentException (rest)))
  (is (thrown? IllegalArgumentException (rest true)))
  (is (thrown? IllegalArgumentException (rest false)))
  (is (thrown? IllegalArgumentException (rest 1)))
  (is (thrown? IllegalArgumentException (rest 1 2)))
  (is (thrown? IllegalArgumentException (rest \a)))
  (is (thrown? IllegalArgumentException (rest 's)))
  (is (thrown? IllegalArgumentException (rest :k)))
  (are (= _1 _2)
    (rest nil) nil

    ; string
    (rest "") nil
    (rest "a") nil
    (rest "abc") '(\b \c)

    ; list
    (rest ()) nil
    (rest '(1)) nil
    (rest '(1 2 3)) '(2 3)

    (rest '(nil)) nil
    (rest '(1 nil)) '(nil)
    (rest '(1 ())) '(())
    (rest '(nil 2)) '(2)
    (rest '(())) nil
    (rest '(() nil)) '(nil)
    (rest '(() 2 nil)) '(2 nil)

    ; vector
    (rest []) nil
    (rest [1]) nil
    (rest [1 2 3]) [2 3]

    (rest [nil]) nil
    (rest [1 nil]) [nil]
    (rest [1 []]) [[]]
    (rest [nil 2]) [2]
    (rest [[]]) nil
    (rest [[] nil]) [nil]
    (rest [[] 2 nil]) [2 nil]

    ; set
    (rest #{}) nil
    (rest #{1}) nil
    ;(rest #{1 2 3}) 1

    (rest #{nil}) nil
    ;(rest #{1 nil}) 1
    ;(rest #{nil 2}) nil
    (rest #{#{}}) nil
    ;(rest #{#{} nil}) #{}
    ;(rest #{#{} 2 nil}) #{}

    ; map
    (rest {}) nil
    (rest (sorted-map :a 1)) nil
    (rest (sorted-map :a 1 :b 2 :c 3)) '((:b 2) (:c 3))

    ; array
    (rest (into-array [])) nil
    (rest (into-array [1])) nil
    (rest (into-array [1 2 3])) '(2 3)

    (rest (to-array [nil])) nil
    (rest (to-array [1 nil])) '(nil)
    ;(rest (to-array [1 (into-array [])])) (list (into-array []))
    (rest (to-array [nil 2])) '(2)
    (rest (to-array [(into-array [])])) nil
    (rest (to-array [(into-array []) nil])) '(nil)
    (rest (to-array [(into-array []) 2 nil])) '(2 nil)
  )
)

;; (ffirst coll) = (first (first coll))
;;
(deftest test-ffirst
  (is (thrown? IllegalArgumentException (ffirst)))
  (are (= _1 _2)
    (ffirst nil) nil

    (ffirst ()) nil
    (ffirst '((1 2) (3 4))) 1

    (ffirst []) nil
    (ffirst [[1 2] [3 4]]) 1

    (ffirst {}) nil
    (ffirst {:a 1}) :a

    (ffirst #{}) nil
    (ffirst #{[1 2]}) 1
  )
)

;; (frest coll) = (first (rest coll)) = (second coll)
;;
(deftest test-frest
  (is (thrown? IllegalArgumentException (frest)))
  (are (= _1 _2)
    (frest nil) nil

    (frest ()) nil
    (frest '(1)) nil
    (frest '(1 2 3 4)) 2

    (frest []) nil
    (frest [1]) nil
    (frest [1 2 3 4]) 2

    (frest {}) nil
    (frest (sorted-map :a 1)) nil
    (frest (sorted-map :a 1 :b 2)) [:b 2]

    (frest #{}) nil
    (frest #{1}) nil
    (frest (sorted-set 1 2 3 4)) 2
  )
)

;; (rfirst coll) = (rest (first coll))
;;
(deftest test-rfirst
  (is (thrown? IllegalArgumentException (rfirst)))
  (are (= _1 _2)
    (rfirst nil) nil

    (rfirst ()) nil
    (rfirst '((1 2 3) (4 5 6))) '(2 3)

    (rfirst []) nil
    (rfirst [[1 2 3] [4 5 6]]) '(2 3)

    (rfirst {}) nil
    (rfirst {:a 1}) '(1)

    (rfirst #{}) nil
    (rfirst #{[1 2]}) '(2)
  )
)

;; (rrest coll) = (rest (rest coll))
;;
(deftest test-rrest
  (is (thrown? IllegalArgumentException (rrest)))
  (are (= _1 _2)
    (rrest nil) nil

    (rrest ()) nil
    (rrest '(1)) nil
    (rrest '(1 2)) nil
    (rrest '(1 2 3 4)) '(3 4)

    (rrest []) nil
    (rrest [1]) nil
    (rrest [1 2]) nil
    (rrest [1 2 3 4]) '(3 4)

    (rrest {}) nil
    (rrest (sorted-map :a 1)) nil
    (rrest (sorted-map :a 1 :b 2)) nil
    (rrest (sorted-map :a 1 :b 2 :c 3 :d 4)) '([:c 3] [:d 4])

    (rrest #{}) nil
    (rrest #{1}) nil
    (rrest (sorted-set 1 2)) nil
    (rrest (sorted-set 1 2 3 4)) '(3 4)
  )
)


(deftest test-interpose
  (are (= _1 _2)
    (interpose 0 []) nil
    (interpose 0 [1]) '(1)
    (interpose 0 [1 2]) '(1 0 2)
    (interpose 0 [1 2 3]) '(1 0 2 0 3)
  )
)

(deftest test-interleave
  (are (= _1 _2)
    (interleave [1 2] [3 4]) '(1 3 2 4)

    (interleave [1] [3 4]) '(1 3)
    (interleave [1 2] [3]) '(1 3)

    (interleave [] [3 4]) nil
    (interleave [1 2] []) nil
    (interleave [] []) nil
  )
)

(deftest test-concat
  (are (= _1 _2)
    (concat) nil

    (concat []) nil
    (concat [1 2]) '(1 2)

    (concat [1 2] [3 4]) '(1 2 3 4)
    (concat [] [3 4]) '(3 4)
    (concat [1 2] []) '(1 2)
    (concat [] []) nil

    (concat [1 2] [3 4] [5 6]) '(1 2 3 4 5 6)
  )
)

(deftest test-cycle
  (are (= _1 _2)
    (cycle []) nil

    (take 3 (cycle [1])) '(1 1 1)
    (take 5 (cycle [1 2 3])) '(1 2 3 1 2)

    (take 3 (cycle [nil])) '(nil nil nil)
  )
)

(deftest test-partition
  (are (= _1 _2)
    (partition 2 [1 2 3]) '((1 2))
    (partition 2 [1 2 3 4]) '((1 2) (3 4))
    (partition 2 []) nil

    (partition 2 3 [1 2 3 4 5 6 7]) '((1 2) (4 5))
    (partition 2 3 [1 2 3 4 5 6 7 8]) '((1 2) (4 5) (7 8))
    (partition 2 3 []) nil

    (partition 1 []) nil
    (partition 1 [1 2 3]) '((1) (2) (3))

    (partition 5 [1 2 3]) nil

;    (partition 0 [1 2 3]) (repeat nil)   ; infinite sequence of nil
    (partition -1 [1 2 3]) nil
    (partition -2 [1 2 3]) nil
  )
)

(deftest test-reverse
  (are (= _1 _2)
    (reverse []) nil
    (reverse [1]) '(1)
    (reverse [1 2 3]) '(3 2 1)
  )
)

(deftest test-take
  (are (= _1 _2)
    (take 1 [1 2 3 4 5]) '(1)
    (take 3 [1 2 3 4 5]) '(1 2 3)
    (take 5 [1 2 3 4 5]) '(1 2 3 4 5)
    (take 9 [1 2 3 4 5]) '(1 2 3 4 5)

    (take 0 [1 2 3 4 5]) nil
    (take -1 [1 2 3 4 5]) nil
    (take -2 [1 2 3 4 5]) nil
  )
)

(deftest test-drop
  (are (= _1 _2)
    (drop 1 [1 2 3 4 5]) '(2 3 4 5)
    (drop 3 [1 2 3 4 5]) '(4 5)
    (drop 5 [1 2 3 4 5]) nil
    (drop 9 [1 2 3 4 5]) nil

    (drop 0 [1 2 3 4 5]) '(1 2 3 4 5)
    (drop -1 [1 2 3 4 5]) '(1 2 3 4 5)
    (drop -2 [1 2 3 4 5]) '(1 2 3 4 5)
  )
)

(deftest test-take-nth
  (are (= _1 _2)
     (take-nth 1 [1 2 3 4 5]) '(1 2 3 4 5)
     (take-nth 2 [1 2 3 4 5]) '(1 3 5)
     (take-nth 3 [1 2 3 4 5]) '(1 4)
     (take-nth 4 [1 2 3 4 5]) '(1 5)
     (take-nth 5 [1 2 3 4 5]) '(1)
     (take-nth 9 [1 2 3 4 5]) '(1)

     ; infinite seq of 1s = (repeat 1)
     ;(take-nth 0 [1 2 3 4 5])
     ;(take-nth -1 [1 2 3 4 5])
     ;(take-nth -2 [1 2 3 4 5])
  )
)

(deftest test-take-while
  (are (= _1 _2)
    (take-while pos? []) nil
    (take-while pos? [1 2 3 4]) '(1 2 3 4)
    (take-while pos? [1 2 3 -1]) '(1 2 3)
    (take-while pos? [1 -1 2 3]) '(1)
    (take-while pos? [-1 1 2 3]) nil
    (take-while pos? [-1 -2 -3]) nil
  )
)

(deftest test-drop-while
  (are (= _1 _2)
    (drop-while pos? []) nil
    (drop-while pos? [1 2 3 4]) nil
    (drop-while pos? [1 2 3 -1]) '(-1)
    (drop-while pos? [1 -1 2 3]) '(-1 2 3)
    (drop-while pos? [-1 1 2 3]) '(-1 1 2 3)
    (drop-while pos? [-1 -2 -3]) '(-1 -2 -3)
  )
)

(deftest test-butlast
  (are (= _1 _2)
    (butlast []) nil
    (butlast [1]) nil
    (butlast [1 2 3]) '(1 2)
  )
)

(deftest test-drop-last
  (are (= _1 _2)
    ; as butlast
    (drop-last []) nil
    (drop-last [1]) nil
    (drop-last [1 2 3]) '(1 2)

    ; as butlast
    (drop-last 1 []) nil
    (drop-last 1 [1]) nil
    (drop-last 1 [1 2 3]) '(1 2)

    (drop-last 2 []) nil
    (drop-last 2 [1]) nil
    (drop-last 2 [1 2 3]) '(1)

    (drop-last 5 []) nil
    (drop-last 5 [1]) nil
    (drop-last 5 [1 2 3]) nil

    (drop-last 0 []) nil
    (drop-last 0 [1]) '(1)
    (drop-last 0 [1 2 3]) '(1 2 3)

    (drop-last -1 []) nil
    (drop-last -1 [1]) '(1)
    (drop-last -1 [1 2 3]) '(1 2 3)

    (drop-last -2 []) nil
    (drop-last -2 [1]) '(1)
    (drop-last -2 [1 2 3]) '(1 2 3)
  )
)

(deftest test-split-at
  (is (vector? (split-at 2 [])))
  (is (vector? (split-at 2 [1 2 3])))

  (are (= _1 _2)
    (split-at 2 []) [nil nil]
    (split-at 2 [1 2 3 4 5]) [(list 1 2) (list 3 4 5)]

    (split-at 5 [1 2 3]) [(list 1 2 3) nil]
    (split-at 0 [1 2 3]) [nil (list 1 2 3)]
    (split-at -1 [1 2 3]) [nil (list 1 2 3)]
    (split-at -5 [1 2 3]) [nil (list 1 2 3)]
  )
)

(deftest test-split-with
  (is (vector? (split-with pos? [])))
  (is (vector? (split-with pos? [1 2 -1 0 3 4])))

  (are (= _1 _2)
    (split-with pos? []) [nil nil]
    (split-with pos? [1 2 -1 0 3 4]) [(list 1 2) (list -1 0 3 4)]

    (split-with pos? [-1 2 3 4 5]) [nil (list -1 2 3 4 5)]
    (split-with number? [1 -2 "abc" \x]) [(list 1 -2) (list "abc" \x)]
  )
)

(deftest test-empty?
  (are (empty? _)
    nil
    ()
    []
    {}
    #{}
    ""
    (into-array []))
  (are (not (empty? _))
    '(1 2)
    [1 2]
    {:a 1 :b 2}
    #{1 2}
    "abc"
    (into-array [1 2])))


;; pmap
;;
(deftest pmap-does-its-thing
  ;; regression fixed in r1218; was OutOfMemoryError
  (is (= '(1) (pmap inc [0]))))


;; equality
;;
(deftest vectors-equal-other-seqs-by-items-equality
  ;; regression fixed in r1208; was not equal
  (is (= '() []))

  (is (= '(1) [1])))
