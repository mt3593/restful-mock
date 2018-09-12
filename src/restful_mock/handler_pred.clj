(ns restful-mock.handler-pred)

(defn match-request
  [expected-request request]
  (= (:uri expected-request)
     (:uri (select-keys request (keys expected-request)))))

(defn req-resp->fn
  [[expected-request mock-response]]
  (fn [request]
    (when (match-request expected-request request)
      mock-response)))

(defn expected-calls-and-responses->req-resp-preds
  [expected-calls-and-responses]
  (map req-resp->fn (partition 2 expected-calls-and-responses)))

(defn handler
  [expected-calls-and-responses]
  (let [req-resp-preds (expected-calls-and-responses->req-resp-preds expected-calls-and-responses)]
    (fn [request]
      (if-let [response ((apply some-fn req-resp-preds) request)]
        response
        (do
          (println "oshdfosdhfsdfj")
          {:status 500})))))
