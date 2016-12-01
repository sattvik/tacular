(ns io.clojure.tacular.clojure-test
  "Provides clojure.test integration for clojure.spec."
  {:author "Daniel Solano Gómez"}
  (:require [cljs.spec :as s]
            [cljs.spec.test :as stest]
            [cljs.test :as t]
            [io.clojure.tacular.clojure-test.assert-helpers])
  (:require-macros [io.clojure.tacular.clojure-test.cljs-assert-exprs]))

;; FIXME: replace with cljs.spec/explain-printer when it exists
(defn ^:private explain-printer
  [ed]
  (print
    (with-out-str
      (doseq [{:keys [path pred val reason via in] :as prob} (::s/problems ed)]
        (when-not (empty? in)
          (print "In:" (pr-str in)))
        (print "val: ")
        (pr val)
        (print " fails")
        (when-not (empty? via)
          (print " spec:" (pr-str (last via))))
        (when-not (empty? path)
          (print " at:" (pr-str path)))
        (print " predicate: ")
        (pr (s/abbrev pred))
        (when reason (print ", " reason))
        (doseq [[k v] prob]
          (when-not (#{:path :pred :val :reason :via :in} k)
            (newline)
            (print "\t" (pr-str k) " " (pr v))))
        (newline)))))

(defmethod t/report [::t/default ::check-passed]
  [_]
  (t/inc-report-counter! :pass))

(defmethod t/report [::t/default ::check-failed]
  [{:keys [result] :as m}]
  (t/inc-report-counter! :fail)
  (println "\nFAIL in" (t/testing-vars-str m))
  (when (seq (:testing-contexts (t/get-current-env)))
    (println (t/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (println "checking:" (pr-str (:sym result)))
  (explain-printer (select-keys (ex-data (:failure result))
                                [::s/problems])))

(defmethod t/report [::t/default ::spec-problem]
  [{{:keys [failure spec sym]} :result :as m}]
  (t/inc-report-counter! :error)
  (println "\nERROR in" (t/testing-vars-str m))
  (when (seq (:testing-contexts (t/get-current-env)))
    (println (t/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (println "checking:" (pr-str sym))
  (println "   error:" (.-message failure))
  (println "    spec:" (pr-str (s/describe spec))))

;; TODO ::no-gen does not appear to happen in ClojureScript
(defmethod t/report [::t/default ::no-gen]
  [{{:keys [sym] :as result} :result :as m}]
  (t/inc-report-counter! :error)
  (println "\nERROR in" (t/testing-vars-str m))
  (when (seq (:testing-contexts (t/get-current-env)))
    (println (t/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (println "checking:" (pr-str sym))
  (println "   error: Unable to generate arguments")
  (let [summary (:failure (stest/abbrev-result result))]
    (println "    path:" (pr-str (::s/path summary)))
    (println "    form:" (pr-str (::s/form summary)))))

(defmethod t/report [::t/default ::check-threw]
  [{{:keys [failure sym]} :result :as m}]
  (t/inc-report-counter! :error)
  (println "\nERROR in" (t/testing-vars-str m))
  (when (seq (:testing-contexts (t/get-current-env)))
    (println (t/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (println "checking:" (pr-str sym))
  (println "   error:" (.-message failure)))
