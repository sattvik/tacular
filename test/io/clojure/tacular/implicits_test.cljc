(ns io.clojure.tacular.implicits-test
  (:require [clojure.spec :as s]
            #?(:clj [clojure.spec.gen :as gen]
               :cljs [cljs.spec.impl.gen :as gen])
            [clojure.test :as t :refer [deftest testing is]]
            [io.clojure.tacular.implicits]))

(deftest regex-specs-test
  (testing "validates"
    (is (= true (s/valid? #"foo" "foo")))
    (is (= true (s/valid? #"fo*" "f")))
    (is (= true (s/valid? #"fo*" "fo")))
    (is (= true (s/valid? #"fo*" "foo")))
    (is (= false (s/valid? #"foo" "bar"))))
  (testing "conforms strings to themselves"
    (is (identical? "foo" (s/conform #"foo" "foo")))
    (is (identical? "foo" (s/conform #"fo*" "foo")))
    (is (s/invalid? (s/conform #"foo" "foobar")))
    (is (s/invalid? (s/conform #"foo" 2))))
  (testing "unforms strings to themselves"
    (is (identical? "foooo" (s/unform #"fo*" "foooo")))
    (is (identical? "foo" (s/unform #"foo" "foo"))))
  (testing "explains"
    (is (= #::s{:problems [{:path [] :pred 'string? :val 2 :via [] :in []}]}
           (s/explain-data #"foo" 2)))
    ;; we use pr-str here because two patterns don't equal each other
    (is (= (pr-str #::s{:problems [{:path [] :pred (list 're-matches #"foo" "bar") :val "bar" :via [] :in []}]})
           (pr-str (s/explain-data #"foo" "bar"))))
    (is (nil? (s/explain-data #"foo" "foo"))))
  (testing "describes"
    ;; again, pr-str
    (is (= (pr-str #"foo") (pr-str (s/describe #"foo")))))
  ;; only test generation in Clojure (no string-from-regex is available in ClojureScript
  #?(:clj (testing "gen"
            (is (every? #(re-matches #"fo*" %)
                        (gen/sample (s/gen #"fo*"))))))
  (testing "with-gen"
    (is (= "foo"
           (gen/generate (s/gen (s/with-gen #"foo" #(gen/return "foo"))))))))
