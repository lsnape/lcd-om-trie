(ns dojo.client
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:refer-clojure :exclude [chars])
  (:require
    [cljs.core.async :refer [<! >! put! close! timeout]]
    [chord.client :refer [ws-ch]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [dojo.trie :refer [add-to-trie prefixes trie-to-vector]]))

(enable-console-print!)

; debugging
; log to console
; (prn "test")
; trigger javascript breakpointf
; (js* "debugger;")

(defn save-prefix [e owner]
  (om/set-state! owner :prefix (.. e -target -value)))

; TODO : google search for this word
(defn send-message [ws e owner]
  (om/set-state! owner :prefix "")
  (.preventDefault e))

; TODO re-render the DOM?
; undo on backspace
(defn filter-words [e owner trie messages]
  (let [prefix (.. e -target -value)]
    (save-prefix e owner)
    (om/update! trie prefixes prefix)
    (let [new-words  (om/read trie (fn [trie]
                       (trie-to-vector (om/value trie))))
          new-messages (mapv hash-map (repeat :message) new-words)]
      (prn new-messages)
      (om/set-state! owner :messages new-messages))))

(defn the-sender [{:keys [ws messages trie]} owner]
  (reify
    om/IInitState
    (init-state [_] {:prefix ""})
    om/IRender
    (render [_]
      (dom/form #js {:onSubmit #(send-message ws % owner)} ; (no action on submit)
      (dom/h3 nil "Filter words on input:")
      (dom/input
          #js {:type "text"
               :ref "text-field"
               :value (om/get-state owner :prefix)
               :onChange #(filter-words % owner trie messages)})))))

(defn listen-for-messages [{:keys [ws trie messages]} owner]
  (put! ws "words please!")
  (go-loop []
    (when-let [msg (<! ws)]
      ; server sent us a word update our vector and trie of words
      (do (om/update! messages conj msg)
          (om/update! trie add-to-trie (:message msg)))
      (recur))))

(defn message [{:keys [message]} owner]
    (om/component
     (dom/li nil message)))


(defn word-list [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (listen-for-messages data owner))
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h3 nil "1000 words taken at random:")
        (dom/ol nil (om/build-all message (data :messages)))))))   ; om/build-all takes a seq of cursors
                                                                   ; calls (message cursor owner) when seq updates
(go
  (let [ws (<! (ws-ch "ws://localhost:3000/ws"))
        app-state (atom {:ws ws
                         :messages []
                         :trie {}})]
        (om/root
          app-state
          (fn [app owner]
            (reify
              om/IRender
              (render [_]
                (dom/div nil
                  (om/build the-sender app)
                  (om/build word-list app)))))
          (.getElementById js/document "app"))))

