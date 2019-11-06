(ns url-shortener.core
  (:require [ring.adapter.jetty     :refer [run-jetty]]
            [ring.middleware.json   :refer [wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response     :refer [response redirect]]
            [compojure.core         :refer [routes GET POST]]
            [compojure.route        :refer [not-found]]
            [compojure.coercions    :refer [as-int]])
  (:gen-class))


;;;;; SPEC ;;;;;

(def uri-regex  #"\w+:(\/?\/?)[^\s]+")

(def short-url-resp {:original_url nil :short_url nil})

(def short-url-err {:error "invalid URL"})



;;;;; STORAGE ;;;;;

(def uris (atom {}))

(defn generate-key []
  (let [random-key (rand-int 1000)]
    (if (contains? @uris random-key)
      (generate-key)
      random-key)))

(defn shorten-uri
  [uri]
  (let [key (generate-key)
        _ (swap! uris conj {key (redirect uri)})] key))


;;;;; API ;;;;;

(def my-routes
  (routes
   (POST  "/shorturl"  [address]             (fn [_] (if (re-matches uri-regex address) (response (assoc short-url-resp :original_url address :short_url (shorten-uri address))) (response short-url-err))))
   (GET   "/:shorturl" [shorturl :<< as-int] (fn [_] (if (contains? @uris shorturl) (get @uris shorturl) (response short-url-err))))
   (not-found (response {:error "Not found"}))))

(def app 
  (-> my-routes
      wrap-params
      wrap-json-response))

(defn -main
  []
  (run-jetty app {:port 3000}))
