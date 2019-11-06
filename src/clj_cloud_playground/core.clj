(ns clj-cloud-playground.core
  (:gen-class)
  (:require
    [reitit.ring :as ring]
    [environ.core :refer [env]]
    [hiccup.page :refer [html5]]
    [ring.util.http-response :refer [ok not-found]]
    [taoensso.timbre :as timbre]
    [nrepl.server :refer [start-server stop-server]]
    [clojure.pprint :as pp]
    [drawbridge.core]
    [partsbin.core :refer [start stop] :as partsbin]
    [partsbin.ring.adapter.jetty.core :as web]
    [ring.middleware.defaults :refer :all])
  (:import (java.util Date)))

(defn hello-world-handler [request]
  (ok (html5
        [:h1 "Hello World"]
        [:ul
         [:li [:a {:href "/time"} "What time is it?"]]
         [:li [:a {:href "/stats"} "See some system stats"]]
         [:li [:a {:href "/dump"} "Dump the request"]]])))

(defn stats-handler [request]
  (ok
    (let [rt (Runtime/getRuntime) mb (* 1024.0 1024.0)]
      (with-out-str
        (pp/pprint
          {:free-memory-MB  (/ (.freeMemory rt) mb)
           :max-memory-MB   (/ (.maxMemory rt) mb)
           :total-memory-MB (/ (.totalMemory rt) mb)})))))

(def app
  (ring/ring-handler
    (ring/router
      [["/" {:handler hello-world-handler}]
       ["/time" {:handler (fn [request] (ok (str "The time is: " (Date.))))}]
       ["/stats" {:handler stats-handler}]
       ["/dump" {:handler (fn [request] (ok (with-out-str (pp/pprint request))))}]
       (let [nrepl-handler (drawbridge.core/ring-handler)]
         ["/repl" {:handler nrepl-handler}])]
      {:data {:middleware [[wrap-defaults (assoc-in api-defaults [:responses :content-types] false)]]}})
    (constantly (not-found "Not found"))))

(def config
  {::web/server
   {:host    "0.0.0.0"
    :join? false
    :port    3000
    :handler #'app}})

(defonce sys (partsbin/create config))

(defn -main [& [port]]
    (let [nrepl-port 3001                                   ;(some->> :nrepl-port env (re-matches #"\d+") Long/parseLong)
          nrepl-host (env :nrepl-host "0.0.0.0")
          production? (#{"true" true} (env :is-production false))
          server (start-server :bind nrepl-host :port nrepl-port)
          port-actual (or (cond-> port (string? port) (Integer/parseInt port))
                          (env :port)
                          (get-in config [::web/server :port]))
          _ (println port-actual)
          config-actual (partsbin/swap-config! sys (fn [config] (assoc-in config [::web/server :port] port-actual)))
          system (start sys)]
      (timbre/info "System started!!!")
      (when server (timbre/info (str "nrepl port started on port " nrepl-port ".")))
      (when (and nrepl-port production?)
        (timbre/info (str "Not launching nrepl server in production environment.")))
      (try
        (.addShutdownHook
          (Runtime/getRuntime)
          (let [^Runnable shutdown #(do (stop sys) (when server (stop-server server)))]
            (Thread. shutdown)))
        (catch Throwable t
          (timbre/warn t)
          (do
            (stop sys)
            (when server (stop-server server)))))))