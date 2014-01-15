(ns dojo.client
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:refer-clojure :exclude [chars])
  (:require
    [cljs.core.async :refer [<! >! put! close! timeout]] 
    [chord.client :refer [ws-ch]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(enable-console-print!)

; debugging
; log to console
; (prn "test")
; trigger javascript breakpoint
; (js* "debugger;")

(defn save-message [e owner]
  (om/set-state! owner :message-to-send (.. e -target -value)))

(defn send-message [ws e owner]
  (om/set-state! owner :message-to-send "")
  (.preventDefault e))

(defn filter-words [e owner]
  (let [messages (om/get-state owner :messages)
        prefix (.. e -target -value)]
    ; TODO filter our words and update
    ))

(defn the-sender [{:keys [ws messages]} owner]
  (reify
    om/IInitState
    (init-state [_] {:message-to-send ""})
    om/IRender
    (render [_]
      (dom/form #js {:onSubmit #(send-message ws % owner)}  ; TODO no action on submit
      (dom/h3 nil "Filter words on input:")
      (dom/input
          #js {:type "text"
               :ref "text-field"
               :value (om/get-state owner :message-to-send)
               :onChange #(save-message % owner)}))))) ; TODO call filter-words

(defn listen-for-messages [{:keys [ws messages]}]
  (put! ws "words please!")
  (go-loop []
    (when-let [msg (<! ws)]
      (om/update! messages conj msg)
      (recur))))

(defn message [{:keys [message]} owner]
  (om/component
    (dom/li nil message)))

(defn word-list [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (listen-for-messages data))
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h3 nil "1000 words taken at random")
        (dom/ol nil (om/build-all message (data :messages)))))))

(go
  (let [ws (<! (ws-ch "ws://localhost:3000/ws"))      ; establish web socket connection
        app-state (atom {:ws ws :messages []})]       ; initialise global state of app
        (om/root                                      ;
          app-state                                   ; fn must implement IRender as a minimum
          (fn [app owner]
            (reify
              om/IRender
              (render [_]
                (dom/div nil
                  (om/build the-sender app)
                  (om/build word-list app)))))
          (.getElementById js/document "app"))))

