(ns restful-mock.handler-pred
  (:require [restful-mock.request-response-pred :refer [my-request? give-response]]))

(defprotocol ^:private Handler
  (get-unexpected-requests [this])
  (container-handler [this]))

(defrecord ^:private SimpleHandler
    [req-resp-preds unexpected-requests]
  Handler
  (container-handler
    [this]
    (fn [request]
      (if-let [req-resp-pred (some #(when (my-request? % request) %) req-resp-preds)]
        (give-response req-resp-pred)
        (do
          (swap! unexpected-requests conj request)
          {:status 500}))))
  (get-unexpected-requests [this]
    @unexpected-requests))

(defn handler-component
  [req-resp-preds]
  (->SimpleHandler req-resp-preds (atom [])))
