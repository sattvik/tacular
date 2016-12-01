(ns io.clojure.tacular.clojure-test
  "Provides clojure.test integration for clojure.spec."
  {:author "Daniel Solano Gómez"}
  (:require [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [clojure.stacktrace :as stack]
            [clojure.test :as t]
            [io.clojure.tacular.clojure-test.assert-helpers :as helpers]))

(defmethod t/assert-expr 'checks? [msg form]
  ;; (is (checks? sym-o-syms))
  ;; (is (checks? sym-o-syms opts))
  ;; Passes the arguemnts to core.spec.test/check and reports on any failures.
  (helpers/assert-checks? msg `(stest/check ~@(next form))))

(defmethod t/report ::check-passed
  [_]
  (t/with-test-out (t/inc-report-counter :pass)))

(defmethod t/report ::check-failed
  [{:keys [result] :as m}]
  (t/with-test-out
    (t/inc-report-counter :fail)
    (println "\nFAIL in" (t/testing-vars-str m))
    (when (seq t/*testing-contexts*) (println (t/testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "checking:" (pr-str (:sym result)))
    (s/explain-printer (select-keys (ex-data (:failure result))
                                    [::s/problems]))))

(defmethod t/report ::spec-problem
  [{{:keys [failure spec sym]} :result :as m}]
  (t/with-test-out
    (t/inc-report-counter :error)
    (println "\nERROR in" (t/testing-vars-str m))
    (when (seq t/*testing-contexts*) (println (t/testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "checking:" (pr-str sym))
    (println "   error:" (.getMessage failure))
    (println "    spec:" (pr-str (s/describe spec)))))

(defmethod t/report ::no-gen
  [{{:keys [sym] :as result} :result :as m}]
  (t/with-test-out
    (t/inc-report-counter :error)
    (println "\nERROR in" (t/testing-vars-str m))
    (when (seq t/*testing-contexts*) (println (t/testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "checking:" (pr-str sym))
    (println "   error: Unable to generate arguments")
    (let [summary (:failure (stest/abbrev-result result))]
      (println "    path:" (pr-str (::s/path summary)))
      (println "    form:" (pr-str (::s/form summary))))))

(defmethod t/report ::check-threw
  [{{:keys [failure sym]} :result :as m}]
  (t/with-test-out
    (t/inc-report-counter :error)
    (println "\nERROR in" (t/testing-vars-str m))
    (when (seq t/*testing-contexts*) (println (t/testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "checking:" (pr-str sym))
    (stack/print-cause-trace failure t/*stack-trace-depth*)))
