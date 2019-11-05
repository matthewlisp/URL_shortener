(ns url-shortener.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [response redirect]])
  (:gen-class))


;; SPEC

(def uri-regex  #"\w+:(\/?\/?)[^\s]+")


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



(defn -main
  []
  (run-jetty (wrap-json-response handler) {:port 3000}))
