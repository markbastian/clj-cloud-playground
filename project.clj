(defproject clj-cloud-playground "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-time "0.15.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [compojure "1.6.1"]
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [org.immutant/web "2.1.10"]
                 [integrant "0.7.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [nrepl "0.6.0"]
                 [nrepl/drawbridge "0.2.0"]
                 ;[ring "1.7.1"]
                 ;[ring/ring-defaults "0.3.2"]
                 ]

  :main clj-cloud-playground.core

  :profiles {:uberjar {:aot :all}}

  :repl-options {:init-ns clj-cloud-playground.core})
