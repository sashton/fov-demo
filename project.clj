(defproject fov-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [clojure-lanterna "0.9.7"]
                 [com.squidpony/squidlib-util "3.0.0-b9"]]
  :main ^:skip-aot fov-demo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
