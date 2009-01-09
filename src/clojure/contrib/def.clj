;;  Copyright (c) Stephen C. Gilardi. All rights reserved.  The use and
;;  distribution terms for this software are covered by the Eclipse Public
;;  License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;  be found in the file epl-v10.html at the root of this distribution.  By
;;  using this software in any fashion, you are agreeing to be bound by the
;;  terms of this license.  You must not remove this notice, or any other,
;;  from this software.
;;
;;  File: def.clj
;;
;;  def.clj provides variants of def that make including doc strings and
;;  making private definitions more succinct.
;;
;;  scgilardi (gmail)
;;  17 May 2008

(ns clojure.contrib.def)

(defmacro defvar
  "Defines a var with an optional intializer and doc string"
  ([name]
     (list `def name))
  ([name init]
     (list `def name init))
  ([name init doc]
     (list `def (with-meta name (assoc (meta name) :doc doc)) init)))

(defmacro defunbound
  "Defines an unbound var with optional doc string"
  ([name]
     (list `def name))
  ([name doc]
     (list `def (with-meta name (assoc (meta name) :doc doc)))))

(defmacro defmacro-
  "Same as defmacro but yields a private definition"
  [name & decls]
  (list* `defmacro (with-meta name (assoc (meta name) :private true)) decls))

(defmacro defvar-
  "Same as defvar but yields a private definition"
  [name & decls]
  (list* `defvar (with-meta name (assoc (meta name) :private true)) decls))

(defmacro defunbound-
  "Same as defunbound but yields a private definition"
  [name & decls]
  (list* `defunbound (with-meta name (assoc (meta name) :private true)) decls))

(defmacro defstruct-
  "Same as defstruct but yields a private definition"
  [name & decls]
  (list* `defstruct (with-meta name (assoc (meta name) :private true)) decls))

(defmacro defonce-
  "Same as defonce but yields a private definition"
  ([name expr]
     (list `defonce (with-meta name (assoc (meta name) :private true)) expr))
  ([name expr doc]
     (list `defonce (with-meta name (assoc (meta name) :private true :doc doc)) expr)))

(defmacro defalias
  "Defines an alias for a var: a new var with the same value and metadata
  as another with the exception of :namespace, :name, :file, :line, and
  optionally :doc which are those of new var."
  ([name orig]
     `(let [v# (def ~name (. (var ~orig) (get)))]
        (. v# (setMeta (merge (meta (var ~orig)) (meta (var ~name)))))
        v#))
  ([name orig doc]
     `(let [v# (def ~name (. (var ~orig) (get)))]
        (. v# (setMeta (merge (meta (var ~orig)) (assoc (meta (var ~name)) :doc ~doc))))
        v#)))

; defhinted by Chouser:
(defmacro defhinted
  "Defines a var with a type hint matching the class of the given
  init.  Be careful about using any form of 'def' or 'binding' to a
  value of a different type.  See http://paste.lisp.org/display/73344"
  [sym init]
  `(do
     (def ~sym ~init)
     (alter-meta! (var ~sym) assoc :tag (class ~sym))
     (var ~sym)))
