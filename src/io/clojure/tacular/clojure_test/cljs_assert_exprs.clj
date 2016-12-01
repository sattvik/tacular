(ns io.clojure.tacular.clojure-test.cljs-assert-exprs
  (:require [cljs.spec.test :as stest]
            [cljs.test]
            [io.clojure.tacular.clojure-test.assert-helpers :as helpers]))

(defmethod cljs.test/assert-expr 'checks? [_ msg form]
  (helpers/assert-checks? msg `(stest/check ~@(next form))))
