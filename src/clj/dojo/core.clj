(ns dojo.core
  (:require
    [ring.util.response :refer [response]]
    [chord.http-kit :refer [with-channel]]
    [clojure.core.async :refer [<! >! put! close! go-loop]]
    [compojure.core :refer [defroutes GET routes]]
    [compojure.handler :refer [api]]
    [compojure.route :refer [resources]]
    [hiccup.page :refer [html5 include-js]]
    [hiccup.element :refer [javascript-tag]]
    [dojo.words :refer [words]]
    [dojo.trie :refer [add-to-trie]]))

(defn index-page []
  (html5
   [:head
    [:title "Prefix Tree Demo"]]
    [:body
      [:div#app]
      (include-js "//fb.me/react-0.8.0.js") ; only required in dev build
      (include-js "/out/goog/base.js") ; only required in dev build
      (include-js "/js/dojo.js")
      (javascript-tag "goog.require('dojo.client');") ; only required in dev build
      ]))

(defn sample-words [n]
  (take n (shuffle words)))

(defn ws-handler [req]
  (with-channel req ws
    (println "Opened connection from" (:remote-addr req))
    (go-loop []
      (when-let [{:keys [message]} (<! ws)]
        (doseq [word (sample-words 1000)]
          (>! ws word))
        (recur)))))

(defn app-routes []
  (routes
    (GET "/" [] (response (index-page)))
    (GET "/ws" [] ws-handler)
    (resources "/js" {:root "js"})
    (resources "/out" {:root "out"}) ; only required in dev build
    ))

(defn webapp []
  (-> (app-routes)
      api))

(def coll ["a" "b" "c"])

(map #(interleave [:message] %) coll)