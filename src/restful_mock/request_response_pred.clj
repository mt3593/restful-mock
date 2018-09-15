(ns restful-mock.request-response-pred)

(defprotocol RequestResponsePred
  (my-request? [this request])
  (give-response [this])
  (times-called [this])
  (satisfied? [this])
  (get-raw-req-resp [this]))

(defn match-request
  [expected-request request]
  (= (:uri expected-request)
     (:uri (select-keys request (keys expected-request)))))

(defrecord SimpleRequestResponsePred
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

(defn req-resp->fn
  [[expected-request mock-response]]
  (->SimpleRequestResponsePred expected-request mock-response (atom 0)))

(defn expected-calls-and-responses->req-resp-preds
  [expected-calls-and-responses]
  (map req-resp->fn (partition 2 expected-calls-and-responses)))
