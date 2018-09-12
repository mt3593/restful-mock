(ns restful-mock.core
  (:require [clojure.spec.alpha :as s]
            [ring.core.spec :as ring-spec]
            [ring.adapter.jetty :as jetty]
            [restful-mock.handler-pred :refer [handler]]))

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
  (let [server (atom nil)]
    (try
      (reset! server (jetty/run-jetty (handler expected-calls-and-responses)
                                      {:port *restful-port*
                                       :join? false}))
      (f)
      (finally
        (swap! server #(when % (.stop %)))))))
