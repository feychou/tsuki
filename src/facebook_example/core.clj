(ns facebook-example.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [facebook-example.facebook :as fb]
            [facebook-example.bot :as bot]
            ; Dependencies via Heroku Example
            [compojure.handler :refer [site]]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello Lemming :)"})

(defroutes fb-routes
  (GET "/" [] (splash))
  (POST "/webhook" request
                   (fb/handle-message request bot/on-message bot/on-postback bot/on-attachments)
                   {:status 200})
  (GET "/webhook" request
                  (fb/validate-webhook request)))

(def app
  (-> (wrap-defaults fb-routes api-defaults)
      (wrap-keyword-params)
      (wrap-json-params)))

(defn -main [& args]
  (println "Started up"))
