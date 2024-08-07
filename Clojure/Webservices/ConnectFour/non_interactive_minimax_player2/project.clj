(defproject non_interactive_minimax_player2 "0.1.0-SNAPSHOT"
  :description "Minimax player to be used for Connect Four"
  :url "https://www.ai-agents.com"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                  [org.clojure/clojure "1.10.3"]
                  [compojure "1.7.0"]
		  [http-kit "2.6.0"]
		  [ring/ring-defaults "0.3.4"]
		  [org.clojure/data.json "2.4.0"]
		]
  :main ^:skip-aot non-interactive-minimax-player2.player
  :resource-paths ["resources/Aiamg.jar"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
