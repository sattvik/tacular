(ns io.clojure.tacular
  "Provides extra specs that do not come with Clojure(Script)."
  {:author "Daniel Solano Gómez"}
  (:require [clojure.spec :as s]
            #?(:clj [clojure.spec.gen :as gen]
               :cljs [cljs.spec.impl.gen :as gen])))

(defn ^:skip-wiki re-spec-impl
  "Do not call this directly, use (re-matches? re) or just use a regular
  expression as a spec."
  ([re]
   (re-spec-impl re nil nil))

  ([re form gfn]
   (reify
     s/Specize
     (specize* [s] s)
     (specize* [s _] s)

     s/Spec
     (conform* [_ x]
       (if (and (string? x) (re-matches re x))
         x
         ::s/invalid))
     (unform* [_ y] y)
     (explain* [_ path via in x]
       (cond
         (not (string? x))
         [{:path path :pred 'string? :val x :via via :in in}]

         (not (re-matches re x))
         [{:path path :pred (list 're-matches re x) :val x :via via :in in}]))
     (gen* [spec overrides path rmap]
       (if gfn
         (gfn)
         (let [string-from-regex #?(:clj (@#'gen/dynaload 'com.gfredericks.test.chuck.generators/string-from-regex)
                                    :cljs (gen/dynaload 'com.gfredericks.test.chuck.generators/string-from-regex))]
           (string-from-regex re))))
     (with-gen* [spec gfn]
       (re-spec-impl re form gfn))
     (describe* [_]
       (or form re)))))

(defmacro re-matches?
  "Provides a spec that will check to see if a string matches a regular
  expression.  In Clojure, if test.chuck is available, even comes with a free
  generator.  Thanks, Gary!"
  [re]
  (assert #?(:clj (instance? java.util.regex.Pattern re)
             :cljs (instance? js/RegExp re))
          "re must be a regular expression.")
  `(re-spec-impl ~re '(~'re-matches? ~re) nil))
