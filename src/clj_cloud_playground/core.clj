(ns clj-cloud-playground.core
  (:gen-class)
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [environ.core :refer [env]]
    [hiccup.page :refer [html5]]
    [immutant.web :as immutant]
    [integrant.core :as ig]
    [ring.util.http-response :refer [ok]]
    [clj-time.core :as time]
    [taoensso.timbre :as timbre]
    [nrepl.server :refer [start-server stop-server]]
    [clojure.pprint :as pp]))

(defmethod ig/init-key :web/server [_ {:keys [handler host port]}]
  (immutant/run handler {:host host :port port}))

(defmethod ig/halt-key! :web/server [_ server]
  (immutant/stop server))

(def app
  (->
    (routes
      (GET "/" [] (ok (html5 [:h1 "Hello World"])))
      (GET "/time" [] (str "The time is: " (time/now)))
      (GET "/stats" [] (ok
                         (let [rt (Runtime/getRuntime) mb (* 1024.0 1024.0)]
                           (with-out-str
                             (pp/pprint
                               {:free-memory-MB  (/ (.freeMemory rt) mb)
                                :max-memory-MB   (/ (.maxMemory rt) mb)
                                :total-memory-MB (/ (.totalMemory rt) mb)})))))
      (route/not-found "<h1>Page not found</h1>"))))

(def config
  {:web/server
   {:host    "0.0.0.0"
    :port    3000
    :handler #'app}})

(defonce ^:dynamic *system* nil)
(defn system [] *system*)
(defn start
  ([system]
   (alter-var-root system (fn [s] (if-not s (ig/init config) s))))
  ([] (start #'*system*)))
(defn stop
  ([system] (alter-var-root system (fn [s] (when s (ig/halt! s)) nil)))
  ([] (stop #'*system*)))
(defn restart
  ([system] (do (stop system) (start system)))
  ([] (restart #'*system*)))

(defn -main [& args]
  (let [nrepl-port (some->> :nrepl-port env (re-matches #"\d+") Long/parseLong)
        nrepl-host (env :nrepl-host "0.0.0.0")
        production? (#{"true" true} (env :is-production false))
        server (when (and nrepl-port (not production?)) (start-server :bind nrepl-host :port nrepl-port))
        system (start)]
    (timbre/info "System started!!!")
    (when server (timbre/info (str "nrepl port started on port " nrepl-port ".")))
    (when (and nrepl-port production?)
      (timbre/info (str "Not launching nrepl server in production environment.")))
    (try
      (.addShutdownHook
        (Runtime/getRuntime)
        (let [^Runnable shutdown #(do (stop) (when server (stop-server server)))]
          (Thread. shutdown)))
      (catch Throwable t
        (timbre/warn t)
        (do
          (stop)
          (when server (stop-server server)))))))