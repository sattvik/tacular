(ns io.clojure.tacular.test-helpers.impl
  (:require [clojure.string :as str]))

(defn regex?
  "Returns true if the argument is a regular expression."
  [o]
  #?(:clj (instance? java.util.regex.Pattern o)
     :cljs (instance? js/RegExp o)))

(defn matches?
  "Returns true if the given line is matched by the matcher."
  [matcher line]
  (cond
    (regex? matcher)
    (re-matches matcher line)

    (string? matcher)
    (= matcher line)

    :else
    (ex-info "Matcher must be a regex or a string"
             {:matcher matcher})))

(defn lines-match?
  [msg [_ matchers string :as form]]
  `(let [lines# (str/split-lines ~string)
         has-rest?# (= (last ~matchers) :rest)
         matchers# (if has-rest?#
                     (butlast ~matchers)
                     ~matchers)
         lines# (if has-rest?#
                  (take (count matchers#) lines#)
                  lines#)]
     (if (not= (count matchers#) (count lines#))
       {:type :fail
        :message ~msg
        :expected '~form
        :actual (ex-info "Wrong number of lines."
                         {:lines lines#
                          :matchers ~matchers
                          :line-count (count lines#)
                          :matcher-count ~(count matchers)})}
       (loop [rows# (map vector (map inc (range)) matchers# lines#)]
         (if (seq rows#)
           (let [[no# matcher# line#] (first rows#)]
             (if (matches? matcher# line#)
               (recur (next rows#))
               {:type :fail
                :message ~msg
                :expected '~form
                :actual (ex-info "Line failed match."
                                 {:lines lines#
                                  :matchers ~matchers
                                  :failed-line line#
                                  :failed-matcher matcher#
                                  :failed-line-number no#})}))
           {:type :pass
            :message ~msg
            :expected '~form
            :actual '~form})))))
