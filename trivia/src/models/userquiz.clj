(ns models.userquiz
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.pprint :refer :all]
            [monger.result :refer [ok? has-error?]]))


(def conn (mg/connect))
(def db (mg/get-db conn "project")) ;; database name
(def document "user-quiz") ;; document



(defn get-all-quiz-score-by-user
  [data]
  (mc/find-maps db document data))

(defn add-user-quiz
  [quiz-data]
  (mc/insert-and-return db document quiz-data))



(def user-quiz-1
    {
     :username "shashank7991"
     :quiz_no 1
     :quiz_score 2
     :total_score 2
    }
  )

(def user-quiz-2
    {
     :username "shashank7991"
     :quiz_no 2
     :quiz_score 1
     :total_score 2
    }
  )

  ;(add-user-quiz user-quiz-1)
  ;(add-user-quiz user-quiz-2)

  (get-all-quiz-score-by-user {:username "shashank7991"})
