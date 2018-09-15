(ns restful-mock.core
  (:require [clojure.spec.alpha :as s]
            [ring.core.spec :as ring-spec]
            [ring.adapter.jetty :as jetty]
            [restful-mock.request-response-pred :refer [expected-calls-and-responses->req-resp-preds
                                                        satisfied?]]
            [restful-mock.handler-pred :refer [handler-component
                                               container-handler
                                               get-unexpected-requests]]))

(def ^:dynamic *restful-port* 8081)

(s/def :restful-mock/pred-request
  (s/keys :opt-un [:ring.request/uri]))

(s/def :restful-mock/mock-response
  (s/keys :opt-un [:ring.response/status]))

(s/def ::list-of-mock-request-response-spec
  (s/*
   (s/cat :request :restful-mock/pred-request
          :response :restful-mock/mock-response)))

(s/fdef rest-driven-fn
        :args (s/cat ::list-of-mock-request-response-spec (s/coll-of fn?)))

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
