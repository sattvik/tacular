(ns io.clojure.tacular.test-helpers
  (:require [clojure.test :as t]
            [io.clojure.tacular.test-helpers.impl :as impl]))

(defmethod t/assert-expr 'lines-match? [msg form]
  `(t/do-report ~(impl/lines-match? msg form)))
