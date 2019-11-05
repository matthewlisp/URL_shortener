(defproject url-shortener "0.1.0-SNAPSHOT"
  :description "HTTP API for shortening URLs"
  :url "https://github.com/matthewlisp/URL_shortener"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring/ring-core "1.7.1"]]
  :main ^:skip-aot url-shortener.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
