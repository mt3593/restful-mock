(ns restful-mock.request-response-pred
  (:require [clojure.spec.alpha :as s]))

(s/def :restful-mock/pred-request
  (s/keys :opt-un [:ring.request/uri]))

(s/def :restful-mock/mock-response
  (s/keys :opt-un [:ring.response/status]))

(s/def :restful-mock/list-of-mock-request-response-spec
  (s/*
   (s/cat :request :restful-mock/pred-request
          :response :restful-mock/mock-response)))

(defprotocol RequestResponsePred
  (my-request? [this request])
  (give-response [this])
  (times-called [this])
  (satisfied? [this])
  (get-raw-req-resp [this]))

(defn- match-request
  [expected-request request]
  (= (:uri expected-request)
     (:uri (select-keys request (keys expected-request)))))

(defrecord ^:private SimpleRequestResponsePred
    [expected-request response called]
  RequestResponsePred
  (my-request? [this request]
    (when (< @called 1)
      (match-request expected-request request)))
  (give-response [this]
    (swap! called inc)
    response)
  (times-called [this] @called)
  (satisfied? [this]
    (= @called 1))
  (get-raw-req-resp [this]
    [expected-request response]))

(defn- req-resp->fn
  [[expected-request mock-response]]
  (->SimpleRequestResponsePred expected-request mock-response (atom 0)))

(defn expected-calls-and-responses->req-resp-preds
  [expected-calls-and-responses]
  {:pre [(s/valid? :restful-mock/list-of-mock-request-response-spec expected-calls-and-responses)]}
  (map req-resp->fn (partition 2 expected-calls-and-responses)))
