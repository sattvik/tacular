(def project 'io.clojure/tacular)
(def version "0.1.0")

(set-env!
  :resource-paths #{"src"}
  :dependencies `[[org.clojure/clojure ~(clojure-version) :scope "provided"]
                  [org.clojure/clojurescript "RELEASE" :scope "provided"]
                  [com.gfredericks/test.chuck "RELEASE" :scope "provided"]

                  [org.clojure/test.check "RELEASE" :scope "test"]

                  ;; Boot deps
                  [adzerk/boot-test "RELEASE" :scope "test"]
                  [adzerk/bootlaces "RELEASE" :scope "test"]
                  [crisptrutski/boot-cljs-test "RELEASE" :scope "test"]
                  [samestep/boot-refresh "RELEASE" :scope "test"]]
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
  push {:gpg-sign false}
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
