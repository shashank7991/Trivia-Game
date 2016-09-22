(ns models.grade
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.result :refer [ok? has-error?]]))

(def conn (mg/connect))
(def db (mg/get-db conn "project")) ;; database name
(def document "grades") ;; document

(defn add-grade
  [quiz-result]
  (mc/insert-and-return db document quiz-result))

(defn get-grades
  []
  (mc/find-maps db document))

(defn get-grades-by-name
  [username]
  (mc/find-maps db document { :username username }))
