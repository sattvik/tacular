(ns io.clojure.tacular.test-helpers.cljs-support
  (:require [cljs.test]
            [io.clojure.tacular.test-helpers.impl :as impl]))

(defmethod cljs.test/assert-expr 'lines-match?
  [_ msg form]
  `(cljs.test/do-report ~(impl/lines-match? msg form)))
