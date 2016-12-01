(ns io.clojure.tacular.clojure-test-test.examples
  (:require [clojure.spec :as s]
            [clojure.test :refer [deftest is testing]]
            [io.clojure.tacular.clojure-test]))

(defn ok [x] x)

(s/fdef ok
  :args (s/cat :arg int?)
  :ret int?
  :fn #(= (-> % :args :arg)
          (:ret %)))

(deftest test-ok
  (is (checks? `ok)
      "checks a function that will pass"))

(defn not-ok [x])

(s/fdef not-ok
  :args (s/cat :arg int?)
  :ret int?)

(deftest test-not-ok
  (is (checks? `not-ok)
      "checks a function that will fail"))

(defn spec-problem [x])

(s/fdef spec-problem)

(deftest test-spec-problem
  (is (checks? `spec-problem)))

(defn missing-generator [x])

(s/fdef missing-generator
  :args (s/cat :arg #(instance? #?(:clj java.util.regex.Pattern
                                   :cljs js/RegExp)
                                %)))

(deftest test-missing-generator
  (is (checks? `missing-generator)))

(defn failing-function []
  (throw (ex-info "boom!" {})))

(s/fdef failing-function
  :args (s/cat)
  :ret int?)

(deftest test-failing-function
  (is (checks? `failing-function)))

(deftest test-many-vars
  (is (checks? `[ok not-ok spec-problem missing-generator failing-function])))
