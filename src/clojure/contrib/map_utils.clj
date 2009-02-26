;;  Copyright (c) Jason Wolfe. All rights reserved.  The use and
;;  distribution terms for this software are covered by the Eclipse Public
;;  License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;  be found in the file epl-v10.html at the root of this distribution.  By
;;  using this software in any fashion, you are agreeing to be bound by the
;;  terms of this license.  You must not remove this notice, or any other,
;;  from this software.
;;
;;  map_utils.clj
;;
;;  Utilities for operating on Clojure maps.
;;
;;  jason at w01fe dot com
;;  Created 25 Feb 2009

(ns clojure.contrib.map-utils)


(defmacro lazy-get 
  "Like get, but doesn't evaluate not-found unless it is needed."
  [map key not-found]
  `(if-let [pair# (find ~map ~key)] 
       (val pair#)
     ~not-found))

(defn safe-get 
  "Like get, but throws an exception if the key is not found."
  [map key] 
  (lazy-get map key 
   (throw (IllegalArgumentException. (format "Key %s not found in %s" key map)))))

(defn safe-get-in 
  "Like get-in, but throws an exception if any key is not found."
  [map ks]
  (reduce safe-get map ks))


