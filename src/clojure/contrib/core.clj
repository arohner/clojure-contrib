;   Copyright (c) Laurent Petit and others, March 2009. All rights reserved.

;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this 
;   distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

;; functions/macros variants of the ones that can be found in clojure.core

;; note to other contrib members: feel free to add to this lib

(ns clojure.contrib.core
  (:use clojure.contrib.def))

(defmacro- defnilsafe [docstring non-safe-name nil-safe-name]
  `(defmacro ~nil-safe-name ~docstring
     {:arglists '([~'x ~'form] [~'x ~'form ~'& ~'forms])}
	   ([x# form#]
	     `(let [~'i# ~x#] (when-not (nil? ~'i#) (~'~non-safe-name ~'i# ~form#))))
  	 ([x# form# & more#]
	     `(~'~nil-safe-name (~'~nil-safe-name ~x# ~form#) ~@more#))))
       
(defnilsafe 
  "Same as clojure.core/-> but returns nil as soon as the threaded value is nil itself (thus short-circuiting any pending computation).
   Examples :
   (-?> \"foo\" .toUpperCase (.substring 1)) returns \"OO\"
   (-?> nil .toUpperCase (.substring 1)) returns nil
   "
   -> -?>)
    
(defnilsafe 
  "Same as clojure.core/.. but returns nil as soon as the threaded value is nil itself (thus short-circuiting any pending computation).
   Examples :
   (.?. \"foo\" .toUpperCase (.substring 1)) returns \"OO\"
   (.?. nil .toUpperCase (.substring 1)) returns nil
   "
   .. .?.)
