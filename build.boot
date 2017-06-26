(def project 'io.clojure/tacular)
(def version "0.1.1-SNAPSHOT")

(set-env!
  :resource-paths #{"src"}
  :dependencies `[[org.clojure/clojure ~(clojure-version) :scope "provided"]
                  [org.clojure/clojurescript "1.9.562" :scope "provided"]
                  [com.gfredericks/test.chuck "0.2.7" :scope "provided"
                                                      :exclusions [org.clojure/test.check]]

                  [org.clojure/test.check "0.10.0-alpha1" :scope "test"]

                  ;; Boot deps
                  [adzerk/boot-test "1.2.0" :scope "test"]
                  [adzerk/bootlaces "0.1.13" :scope "test"]
                  [crisptrutski/boot-cljs-test "0.3.1" :scope "test"]
                  [samestep/boot-refresh "0.1.0" :scope "test"]]
  :exclusions '#{org.clojure/clojure}
  :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(require '[adzerk.boot-test :refer [test]])
(require '[adzerk.bootlaces :refer :all])
(require '[boot.git :refer [last-commit]])
(require '[crisptrutski.boot-cljs-test :refer [test-cljs]])
(require '[samestep.boot-refresh :refer [refresh]])

(bootlaces! version)

(def test-namespaces
  '#{io.clojure.tacular-test
     io.clojure.tacular.clojure-test-test
     io.clojure.tacular.implicits-test})

(task-options!
  pom {:project     project
       :version     version
       :description "tacular; what’s missing from spec"
       :url         "https://github.com/sattvik/tacular"
       :scm         {:url "https://github.com/sattvik/tacular"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}}
  test {:namespaces test-namespaces}
  test-cljs {:namespaces test-namespaces})

(deftask with-tests
  "Adds test settings."
  []
  (set-env! :resource-paths #(conj % "test"))
  identity)

(deftask dev
  "Runs in development mode."
  []
  (comp (with-tests)
        (watch)
        (refresh)
        (repl :server true)
        (test)
        (test-cljs)))

(deftask build
  "Build and install the project locally."
  []
  (comp (pom)
        (jar)
        (install)))
