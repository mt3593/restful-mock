(ns restful-mock.core-test
  (:require [clojure.test :refer :all]
            [restful-mock.core :refer :all]
            [restful-mock.handler-pred :refer [get-raw-req-resp]]
            [clj-http.client :as client]))

(defn restful-mock-url
  [path]
  (str "http://localhost:"  *restful-port* path))

(deftest simple-test
  (let [[unsatisfied-expected-calls
         unexpected-calls] (rest-driven-fn
                            [{}
                             {:status 201}]
                            #(is (= 201
                                    (:status (client/get (restful-mock-url  "/anything"))))))]
    (is (empty? unsatisfied-expected-calls))
    (is (empty? unexpected-calls))))

(deftest match-on-path
  (let [[unsatisfied-expected-calls
         unexpected-calls] (rest-driven-fn
                            [{:uri "/anything1"}
                             {:status 200}
                             {:uri "/anything2"}
                             {:status 201}]
                            #(do
                               (is (= 200
                                      (:status (client/get (restful-mock-url  "/anything1")))))
                               (is (= 201
                                      (:status (client/get (restful-mock-url  "/anything2")))))))]
    (is (empty? unsatisfied-expected-calls))
    (is (empty? unexpected-calls))))


(deftest only-match-one-call
  (let [[unsatisfied-expected-calls
         unexpected-calls] (rest-driven-fn
                            [{:uri "/anything"}
                             {:status 200}]
                            #(do
                               (is (= 200
                                      (:status (client/get (restful-mock-url  "/anything")))))
                               (is (= 500
                                      (:status (client/get (restful-mock-url  "/anything") {:throw-exceptions false}))))))]
    (is (empty? unsatisfied-expected-calls))
    (is (= ["/anything"]
           (map :uri unexpected-calls)))))

(deftest return-all-not-called
  (let [[unsatisfied-expected-calls
         unexpected-calls] (rest-driven-fn
                            [{:uri "/anything"}
                             {:status 200}
                             {:uri "/anything12"}
                             {:status 200}]
                            #(is (= 200
                                    (:status (client/get (restful-mock-url  "/anything"))))))]
    (is (= 1 (count unsatisfied-expected-calls)))
    (is (= [{:uri "/anything12"} {:status 200}]
           (get-raw-req-resp (first unsatisfied-expected-calls))))
    (is (empty? unexpected-calls))))
