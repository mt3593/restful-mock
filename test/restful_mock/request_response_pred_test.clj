(ns restful-mock.request-response-pred-test
  (:require [restful-mock.request-response-pred :refer :all]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]))

(deftest match-any-request
  (let [r-pred (req-resp->fn [{} {}])
        request (mock/request :post "/anything" {:content-type :json})]
    (is (false? (satisfied? r-pred))) ;; should not be satisfied yet as no call made
    (is (true? (my-request? r-pred request)))
    (is (false? (satisfied? r-pred))) ;; just checking if the request is for me should not satisfy the pred yet
    (is (= {} (give-response r-pred)))
    (is (true? (satisfied? r-pred))) ;; serving a response satisfies the pred
    ))

