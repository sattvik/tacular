(ns io.clojure.tacular-test
  (:require [clojure.spec :as s]
            #?(:clj [clojure.spec.gen :as gen]
               :cljs [cljs.spec.impl.gen :as gen])
            [clojure.test :refer [deftest is testing]]
            [io.clojure.tacular :as tacular]))

(deftest re-matches?-test
  (testing "validates"
    (is (= true (s/valid? (tacular/re-matches? #"foo") "foo")))
    (is (= true (s/valid? (tacular/re-matches? #"fo*") "f")))
    (is (= true (s/valid? (tacular/re-matches? #"fo*") "fo")))
    (is (= true (s/valid? (tacular/re-matches? #"fo*") "foo")))
    (is (= false (s/valid? (tacular/re-matches? #"foo") "bar"))))
  (testing "conforms strings to themselves"
    (is (identical? "foo" (s/conform (tacular/re-matches? #"foo") "foo")))
    (is (s/invalid? (s/conform (tacular/re-matches? #"foo") "foobar")))
    (is (s/invalid? (s/conform (tacular/re-matches? #"foo") 2))))
  (testing "unforms strings to themselves"
    (is (identical? "foooo" (s/unform (tacular/re-matches? #"fo*") "foooo")))
    (is (identical? "foo" (s/unform (tacular/re-matches? #"foo") "foo"))))
  (testing "explains"
    (is (= #::s{:problems [{:path [] :pred 'string? :val 2 :via [] :in []}]}
           (s/explain-data (tacular/re-matches? #"foo") 2)))
    ;; we use pr-str here because two patterns don't equal each other
    (is (= (pr-str #::s{:problems [{:path [] :pred (list 're-matches #"foo" "bar") :val "bar" :via [] :in []}]})
           (pr-str (s/explain-data (tacular/re-matches? #"foo") "bar"))))
    (is (nil? (s/explain-data (tacular/re-matches? #"foo") "foo"))))
  (testing "describes"
    ;; again, pr-str
    (is (= (pr-str '(re-matches? #"foo"))
           (pr-str (s/describe (tacular/re-matches? #"foo"))))))
  ;; only test generation in Clojure (no string-from-regex is available in ClojureScript
  #?(:clj (testing "gen"
            (is (every? #(re-matches #"fo*" %)
                        (gen/sample (s/gen (tacular/re-matches? #"fo*")))))))
  (testing "with-gen"
    (is (= "foo"
           (gen/generate (s/gen (s/with-gen (tacular/re-matches? #"foo") #(gen/return "foo"))))))))
