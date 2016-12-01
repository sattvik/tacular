(ns io.clojure.tacular.clojure-test.assert-helpers
  (:require [clojure.spec :as s]
            [clojure.test :as t]))

(defn result-type
  "Gen a single stest/check result, determine what type of report should be
  generated."
  [ret]
  (let [failure (:failure ret)
        failure-type (::s/failure (ex-data failure))]
    (cond
     (nil? failure)
     :io.clojure.tacular.clojure-test/check-passed

     (= :check-failed failure-type)
     :io.clojure.tacular.clojure-test/check-failed

     (#{:no-args-spec :no-fn :no-fspec :instrument} failure-type)
     :io.clojure.tacular.clojure-test/spec-problem

     (= :no-gen failure-type)
     :io.clojure.tacular.clojure-test/no-gen

     :default
     :io.clojure.tacular.clojure-test/check-threw)))

#?(:clj
   (defn file-and-line [ex]
     (when (instance? Throwable ex)
       (when-let [element ^StackTraceElement (first (.getStackTrace ^Throwable ex))]
         {:file (.getFileName element)
          :line (.getLineNumber element)})))
   :cljs
   (defn file-and-line [ex]
     (when (instance? js/Error ex)
       (t/file-and-line ex 0))))

(defn assert-checks?
  [msg results]
  `(doseq [result# ~results]
     (let [failure# (:failure result#)]
       (t/do-report (merge {:type (result-type result#)
                            :message ~msg
                            :result result#}
                           (file-and-line failure#))))))
