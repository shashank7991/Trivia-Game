(ns models.register
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.result :refer [ok? has-error?]]))

(def conn (mg/connect))
(def db (mg/get-db conn "project")) ;; database name
(def document "user") ;; document

(defn register-user
  [user-data]
  (mc/insert-and-return db document user-data))

; (register-user {:username "shashank" :password "shashank"})

(defn get-result
  [user-data]
  (mc/find-maps db document user-data))

(defn add-user
  [user-map]
  (mc/insert-and-return db document user-map))

(defn get-user
  [search-criteria]
  (mc/find-maps db document search-criteria))

(defn exist-user?
  [username]
  (if (empty? (get-user {:username username}))
    false
    true))

(defn auth-user?
  [username password]
  (if (empty? (get-user {:username username :password password}))
    false
    true))
