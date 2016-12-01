(ns io.clojure.tacular.clojure-test-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is testing]]
            [io.clojure.tacular.clojure-test-test.examples :as ex]
            [io.clojure.tacular.test-helpers]))

(defn run-test-var
  "Runs the test associated with a test var and returns a tuple containing
  the test output and the report counts."
  [v]
  #?(:clj
     (binding [t/*testing-vars* (list)
               t/*testing-contexts* (list)
               t/*report-counters* (ref t/*initial-report-counters*)
               t/*test-out* (java.io.StringWriter.)]
       (t/test-var v)
       [(str t/*test-out*) @t/*report-counters*])
     :cljs
     (binding [t/*current-env* (t/empty-env)]
       (let [output (with-out-str (t/test-var v))]
         [output (:report-counters t/*current-env*)]))))

(defn matches-lines?
  [line-regexes s]
  (let [lines (str/split-lines s)]
    (and (= (count lines) (count line-regexes))
         (every? identity (map re-matches line-regexes lines)))))

(deftest checks?-test
  (testing "a single, successful test"
    (let [[output counts] (run-test-var #'ex/test-ok)]
      (is (= "" output))
      (is (= {:test 1 :pass 1 :fail 0 :error 0} counts))))
  (testing "a single, unsuccessful test"
    (let [[output counts] (run-test-var #'ex/test-not-ok)]
      (is (lines-match? [#"^FAIL in \(test-not-ok\) \(.*\)$"
                         "checks a function that will fail"
                         "checking: io.clojure.tacular.clojure-test-test.examples/not-ok"
                         "val: nil fails at: [:ret] predicate: int?"]
                        (str/trim output)))
      (is (= {:test 1 :pass 0 :fail 1 :error 0} counts))))
  (testing "a single test with a broken spec"
    (let [[output counts] (run-test-var #'ex/test-spec-problem)]
      (is (lines-match? [#"^ERROR in \(test-spec-problem\) \(.*\)$"
                         "checking: io.clojure.tacular.clojure-test-test.examples/spec-problem"
                         "   error: No :args spec"
                         "    spec: (fspec :args nil :ret nil :fn nil)"]
                        (str/trim output)))
      (is (= {:test 1 :pass 0 :fail 0 :error 1} counts))))
  (testing "a single test with no generator"
    (let [[output counts] (run-test-var #'ex/test-missing-generator)]
      (is (lines-match? #?(:clj [#"^ERROR in \(test-missing-generator\) \(.*\)$"
                                 "checking: io.clojure.tacular.clojure-test-test.examples/missing-generator"
                                 "   error: Unable to generate arguments"
                                 "    path: [:arg]"
                                 "    form: (clojure.core/fn [%] (clojure.core/instance? java.util.regex.Pattern %))"]
                           :cljs [#"^ERROR in \(test-missing-generator\) \(.*\)$"
                                  "checking: io.clojure.tacular.clojure-test-test.examples/missing-generator"
                                  "   error: Unable to construct gen at: [:arg] for: (instance? RegExp %)"])
                        (str/trim output)))
      (is (= {:test 1 :pass 0 :fail 0 :error 1} counts))))
  (testing "a single test with a failing function"
    (let [[output counts] (run-test-var #'ex/test-failing-function)]
      (is (lines-match? #?(:clj [#"^ERROR in \(test-failing-function\) \(.*\)$"
                                 "checking: io.clojure.tacular.clojure-test-test.examples/failing-function"
                                 "clojure.lang.ExceptionInfo: boom!"
                                 :rest]
                           :cljs [#"^ERROR in \(test-failing-function\) \(.*\)$"
                                  "checking: io.clojure.tacular.clojure-test-test.examples/failing-function"
                                  "   error: boom!"])
                        (str/trim output)))
      (is (= {:test 1 :pass 0 :fail 0 :error 1} counts))))
  (testing "testing multiple vars"
    (let [[output counts] (run-test-var #'ex/test-many-vars)]
      (is (lines-match? [#"^FAIL in \(test-many-vars\) \(.*\)$"
                          "checking: io.clojure.tacular.clojure-test-test.examples/not-ok"
                          "val: nil fails at: [:ret] predicate: int?"
                          ""
                          :rest]
                        (str/trim output)))
      (is (= {:test 1 :pass 1 :fail 1 :error 3} counts)))))
