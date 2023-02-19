(defproject games "0.1.0-SNAPSHOT"
  :description "Various games"
  :url "https://www.aia.dk"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.typedclojure/typed.clj.checker "1.0.17"]]
  :java-source-paths ["src/aiamg_java/"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns games.infection.non-interactive-random-player}
)
