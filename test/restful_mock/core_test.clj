(ns restful-mock.core-test
  (:require [clojure.test :refer :all]
            [restful-mock.core :refer :all]
            [clj-http.client :as client]))

(defn restful-mock-url
  [path]
  (str "http://localhost:"  *restful-port* path))

(deftest simple-test
  (rest-driven-fn
   [{}
    {:status 201}]
   #(is (= 201
           (:status (client/get (restful-mock-url  "/anything")))))))

(deftest match-on-path
  (rest-driven-fn
   [{:uri "/anything1"}
    {:status 200}
    {:uri "/anything2"}
    {:status 201}]
   #(do
      (is (= 200
             (:status (client/get (restful-mock-url  "/anything1")))))
      (is (= 201
             (:status (client/get (restful-mock-url  "/anything2"))))))))
