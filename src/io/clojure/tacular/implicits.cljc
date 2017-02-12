(ns io.clojure.tacular.implicits
  "Requiring this namespace will allow spec to recognise additional types as
  specs.  Currently, only regular expressions are supported.

      (require [io.clojure.tacular.implicits])

      (s/def ::foo #\"fo+\")
  "
  {:author "Daniel Solano Gómez"}
  (:require [clojure.spec :as s]
            [io.clojure.tacular :as t]))

(extend-type #?(:clj java.util.regex.Pattern
                :cljs js/RegExp)
  s/Specize
  (specize*
    ([re] (t/re-spec-impl re))
    ([re form] (t/re-spec-impl re form nil))))
