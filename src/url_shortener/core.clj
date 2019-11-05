(ns url-shortener.core
  (:require [ring.adapter.jetty   :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response   :refer [response redirect]]
            [compojure.core       :refer [routes GET POST]]
            [compojure.route      :refer [not-found]]
            [compojure.coercions  :refer [as-int]])
  (:gen-class))


;; SPEC

(def uri-regex  #"\w+:(\/?\/?)[^\s]+")

(def number-regex #"^[0-9]*$")

(def short-url-resp {:original_url nil :short_url nil})

(def short-url-err {:error "invalid URL or malformed header"})


;;; STORAGE

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

;; API


(defn remove-slash
  [uri]
  (clojure.string/replace uri #"/" ""))

(defn handler
  [{{:strs [uri]} :headers :as req}]
  (let [short-url (keyword (remove-slash (:uri req)))]
    (if (contains? @uris short-url)
      (short-url @uris)
      (if (and (= (:uri req) "shorturl")
               (= (:request-method req) :post)
               (re-matches uri-regex uri))
        (response {:original_url uri
                   :short_url (shorten-uri uri)})
        (response {:error "invalid URL or malformed header"})))))

(def my-routes
  (routes
   (POST "/shorturl"  [address]              (fn [x] (str "REDIRECT TO " address)))
   (GET  "/:shorturl" [shorturl :<< as-int] #(if (contains? @uris %) ((keyword %) @uris) short-url-err))
   (not-found {:error "Not found"})))


(defn -main
  []
  (run-jetty (wrap-json-response my-routes) {:port 3000}))
