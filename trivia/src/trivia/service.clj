(ns trivia.service
  (:require [trivia.layout :as layout]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [clojure.pprint :refer :all]
            [cheshire.core :as ches]
            [clojure.data.json :as json]
            [models.userquiz :as user-quiz-model]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as response]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [io.pedestal.interceptor.helpers :refer [definterceptor]]
            [ring.middleware.session.cookie :as cookie]
            [ring.util.response :as ring-resp]
            [clojure.java.io :as io]
            [models.quiz :as quiz]
            [models.grade :as grades]
            [models.register :as register]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defn selmer-page
  [request]
  (layout/render "example.html" {:title "example"}))

(defn hw3-page
  [request]
  (layout/render "hw3.html"  {:title "CS590 Homework 3"}))

(defn option [request]
   (layout/render "option.html"))

(defn error-page
  [error]
  (layout/render "error.html"))

(defn quiz-page
  [request]
  (let [session (get-in request [:session])]
    (if (empty? session)
      (ring-resp/response "please login again.")
      (layout/render "quiz.html" {:title "Quiz"}))))

(defn register
  [request]
  (layout/render "register.html" {:title "Register"}))

(defn login
  [request]
  (layout/render "login.html" {:title "Login"}))

(defn get-assignment
  [request]
  (layout/render "assignment.html" {:title "Assignment"}))

;(defn get-question
;  [request]
;  (let [ques (model/read-quiz-questions 1)]
;

;  (ring-resp/response (str ques))
;    ))
;
;(defn get-correct-choice [q]
;  (let [v (:opt q)
;        a (:correct q)]
;    (+ 1 (.indexOf v a))))
;
; (defn check-answer
;  [request]
;  (try
;    (let [expr (read-string (slurp (:body request)))
;          answer (:answer expr)
;          a (if (or (nil? answer) (= answer "")) 0 (Integer. (:answer expr)))]
;      (if (= a (get-correct-choice (get-question "hello")))
;        (ring-resp/response "correct")
;        (ring-resp/response "incorrect")))
;    (catch Throwable t
;      (str "ERROR: " t))))



(def upper (re-pattern "[A-Z]+"))
(def number (re-pattern "[0-9]+"))
(def special (re-pattern "[\"'!@#$%^&*()?]+"))

(defn strength? [password]
  (not (nil? (and (re-find upper password)
                  (re-find number password)
                  (re-find special password)))))

(defn length? [password]
  (> (count password) 8))

(defn valid-password? [password]
  (and (strength? password) (length? password)))

(defn check-password
  [request]
  (try
    (let [expr (read-string (slurp (:body request)))
          password (:password expr)]

      (if (valid-password? password)
        (ring-resp/response (str "Password Valid."))
        (ring-resp/response (str "Password Not-Valid."))))
    (catch Throwable t
      (str "ERROR: " t))))

(defn check-user
  [request]
  (try
    (let [expr (read-string (slurp (:body request)))
          username (:username expr)]

      (if (register/exist-user? username)
        (ring-resp/response (str "Username Not-Available."))
        (ring-resp/response (str "Username Available."))))
    (catch Throwable t
      (str "ERROR: " t))))

(defn insert
  [firstName lastName username password]
  (str (register/register-user {:firstName firstName
                               :lastName lastName
                               :username username
                               :password password}))
  (layout/render "login.html")
)

(defn check-register
  [request]
    (let [expr (:form-params (body-params/form-parser request))
          firstName (get expr "firstName")
          lastName (get expr "lastName")
          username (get expr "username")
          password (get expr "password")
          repassword (get expr "confirmPassword")]
      (pprint firstName)
      (pprint password)
      (pprint repassword)
    (if (= password repassword)
       (insert firstName lastName username password)
       (layout/render "register.html")))
)

(definterceptor session-interceptor
  (middlewares/session {:store (cookie/cookie-store)}))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn logout [{session :session}]
  (-> (layout/render "logout.html")
      (assoc :session nil)
  )
)

(defn check-login
  [request]
  (try
    (let [expr (:form-params (body-params/form-parser request))
            username (get expr "username")
            password (get expr "password")]
      (pprint username)
      (pprint password)

      (if (register/auth-user? username password)
         (-> (ring-resp/redirect "/option")
            (assoc :session {:name username}))
         (layout/render "login.html")
      )
    )
  )
)

(def question { :quiz 1
                :question1{:q "What is 1+1?"
                           :c ["2", "3", "1", "0"]
                           :a "2"
                           :l "q3"
                          },

                :question2 {:q "is 5 prime number?"
                            :c ["false", "true", "dont know", "cant say"]
                            :a "true"
                            :l "q3"
                           },

                :question3 {:q "What is 5X5?"
                            :c ["15", "20", "30", "25"]
                            :a "25"
                            :l "q3"
                           }
               }
 )

(def question2 {:quiz 2
                :question1{:q "what is 100-50?"
                           :c ["50", "60", "40", "70"]
                           :a "50"
                           :l "q3"
                          },

                :question2 {:q "What is 30+30?"
                            :c ["40", "60", "40", "20"]
                            :a "60"
                            :l "q3"
                           },

                :question3 {:q "What is 99+1?"
                            :c ["100", "101", "99", "102"]
                            :a "100"
                            :l "q3"
                           }

                })

(defn get-quiz-1
  [request]
  (ring-resp/response (str question)))
(defn get-quiz-2
  [request]
  (ring-resp/response (str question2)))

(defn get-question
  [request]
  (ring-resp/response (str (dissoc question :a))))

(defn get-correct-choice-1 [q]
  (let [que (:question1 q)
        v (:c que)
        a (:a que)]
    (+ 1 (.indexOf v a))))

(defn get-correct-choice-2 [q]
  (let [que (:question2 q)
        v (:c que)
        a (:a que)]
    (+ 1 (.indexOf v a))))

(defn get-correct-choice-3 [q]
  (let [que (:question3 q)
        v (:c que)
        a (:a que)]
    (+ 1 (.indexOf v a))))

(def score (atom 0))
 @score

(defn check-answer
  [request]
  (try
   (let [expr (read-string (slurp (:body request)))
          answer (:answer expr)
          answer2 (:answer2 expr)
          answer3 (:answer3 expr)
          quiz (:quiz expr)

          a  (if (or (nil? answer)  (= answer ""))  0 (Integer. (:answer expr)))
          a2 (if (or (nil? answer2) (= answer2 "")) 0 (Integer. (:answer2 expr)))
          a3 (if (or (nil? answer3) (= answer3 "")) 0 (Integer. (:answer3 expr)))
          quiz-number (Integer.(:quiz expr))]

      (if (= quiz-number 1) (def que question))
      (if (= quiz-number 2) (def que question2))

      (if (= a (get-correct-choice-1 que))
        (swap! score inc))
      (if (= a2 (get-correct-choice-2 que))
        (swap! score inc))
      (if (= a3 (get-correct-choice-3 que))
        (swap! score inc))

      (def grade @score)
      (reset! score 0)

      (ring-resp/response (str "Quiz Score:" grade)))

    (catch Throwable t
      (str "ERROR: " t))))

;=====================================================================================

;nichenu hu jetlu select kru a badhu comment kr de. mac thi nahi thay commands different hase

;(defn get-quizes [request]
;  (let [db_documents (quiz/get-all-quiz)
;        quiz_no (map quiz-array db_documents)
;        body {:allquiz quiz_no}]
;  (pprint body)
;  (ring-resp/response (str body))));
;
;(defn quiz-array [data]
;  (str (:qno data)))
;
;(defn get-all-quiz-score[request]
;  (try
;    (let [username (get-in request [:session :username])
;          db-doc (user-quiz-model/get-all-quiz-score-by-user {:username username})
;          new_score (into [] db-doc)
;          body {:username (str username) :user_quiz_score new_score}]
;      (pprint (str "In quiz-score-" username))
;      ;(pprint new_score)
;      (pprint body)
;  (ring-resp/response (str body)))
;     (catch Throwable t
;      (str "ERROR: " t))))
;=====================================================================================  ;getting-quiz

(defn- nonhidden-filter
  "return a FilenameFilter that ignores files that begin with dot or end with ~."
  []
  (reify java.io.FilenameFilter
    (accept [_ dir name] (and (not (.startsWith name "."))
			      (not (.endsWith name "~"))))))

(defn directory-list
  "Given a directory and a regex, return a sorted seq of matching filenames.  To find something like *.txt you would pass in \".*\\\\.txt\""
  ([dir]
     (sort (.list (clojure.java.io/file dir) (nonhidden-filter))))
  )

(defn full-directory-list
  [dir]
  (sort (map #(.getPath %) (.listFiles (io/file dir)))))

(def quiz-directory "./resources/quizes")

(def veriable (directory-list (str quiz-directory)))


(defn get-title
  [path]
  (let [file-path (str "quizes/" path)]
    (str (:title (quiz/read-exam file-path)))
  ))

(defn get-quiz-list
  []
  (map get-title (remove nil? veriable)))

(defn create-quiz-map
  [item]
  {:title item :id (str "q" (subs item 5))})

(defn get-quiz-map
  []
  (map create-quiz-map (get-quiz-list)))

(defn get-quizes [request]
  (let [quizes (get-quiz-map)
        username (or (get-in request [:session :name]) "Stranger")]
    (println "hello")
    ;;(pprint (get-quiz-map))
    (println get-quiz-map)
     (if (= username "Stranger")
       (layout/render "quiz.html" {:title "Quiz"
                                     :name username
                                     :quizes quizes}))
    ))

(defn show-quiz
  [request]
  (let [quiz-num (get-in request [:path-params :quiz-number])
        exam-file (str "quizes/" quiz-num ".json")
        exam (quiz/read-exam exam-file)
        ]
  (println exam)
  (ring-resp/response (str exam))))

;====================== grading quiz

(defn get-correct-choice [q]
  (let [v (:c q)
        a (:a q)]
    (+ 1 (.indexOf v a))))


(defn grade-question
  [question user-answer]
  (let [correct-choice (get-correct-choice question)]
    (println (str "user-answer: " user-answer "correct-choice" correct-choice))
    (if (= (str user-answer) (str correct-choice))
      1 0)))

(defn grade-quiz
  [quiz-num user-answers]
  (let [exam-file (str "quizes/" quiz-num ".json")
        exam (quiz/read-exam exam-file)
        questions (:questions exam)]
    (println "i'm here now")
    (str (reduce + (map grade-question questions user-answers)))
   ))

(defn check-answer
  [request]
  (println (str "i'm here"))
  (try
    (let [expr (read-string (slurp (:body request)))
          username (get-in request [:session :name])
          answers (:answers expr)
          quiz-num (:quiz-num expr)
          grade (grade-quiz quiz-num answers)
          ]
      (grades/add-grade {:username username :score grade :quiz quiz-num})
      (println quiz-num)
      (ring-resp/response grade))
    (catch Throwable t
      (str "ERROR: " t))))

;======================
(defroutes routes
  [[["/" {:get home-page}
     ["/check-login" ^:interceptors [session-interceptor] {:post check-login}]
     ["/check-register" {:post check-register}]
     ["/selmer" {:get selmer-page}]
     ["/register" {:get register}]
     ["/login" {:get login}]
     ["/logout" ^:interceptors [session-interceptor] {:get logout}]
     ["/check-answer" ^:interceptors [session-interceptor] {:post check-answer}]
     ["/quiz" ^:interceptors [session-interceptor] {:get quiz-page}]
     ["/option" {:get option}]
   ;  ["/get-all-quiz"    ^:interceptors [session-interceptor] {:post get-quizes}]
   ;  ["/get-all-quiz-score"  ^:interceptors [session-interceptor]  {:post get-all-quiz-score}]
     ["/eval-user" ^:interceptors [session-interceptor] {:post check-user}]
     ["/eval-pass" ^:interceptors [session-interceptor] {:post check-password}]
     ["/quiz/1" ^:interceptors [session-interceptor] {:get get-quiz-1}]
     ["/quiz/2" ^:interceptors [session-interceptor] {:get get-quiz-2}]
     ["/get-question" ^:interceptors [session-interceptor] {:get get-question}]
     ["/about"  {:get about-page}]
     ["/quizes" {:get get-quizes}]
     ["/quiz/:quiz-number" {:get show-quiz}]]]])

(def service {:env :prod
              ::bootstrap/routes routes
              ::bootstrap/resource-path "/public"
              ::bootstrap/type :jetty
              ::bootstrap/port 4000})
