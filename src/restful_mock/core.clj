(ns restful-mock.core
  (:require [clojure.spec.alpha :as s]
            [ring.core.spec :as ring-spec]
            [ring.adapter.jetty :as jetty]
            [restful-mock.request-response-pred :refer [req-resp->fn satisfied?]]
            [restful-mock.handler-pred :refer [handler-component
                                               container-handler
                                               get-unexpected-requests]]))

(def ^:dynamic *restful-port* 8081)

(s/def :restful-mock/list-of-mock-request-response-spec
  (s/*
   (s/cat :request :restful-mock/pred-request
          :response :restful-mock/mock-response)))

(defn expected-calls-and-responses->req-resp-preds
  [expected-calls-and-responses]
  {:pre [(s/valid? :restful-mock/list-of-mock-request-response-spec expected-calls-and-responses)]}
  (map req-resp->fn (partition 2 expected-calls-and-responses)))

(defn rest-driven-fn
  [expected-calls-and-responses f]
  (let [server (atom nil)
        req-resp-pred (expected-calls-and-responses->req-resp-preds expected-calls-and-responses)
        handler (handler-component req-resp-pred)]
    (try
      (reset! server (jetty/run-jetty (container-handler handler)
                                      {:port *restful-port*
                                       :join? false}))
      (f)
      [(filter #(not (satisfied? %)) req-resp-pred)
       (get-unexpected-requests handler)]
      (finally
        (swap! server #(when % (.stop %)))))))
