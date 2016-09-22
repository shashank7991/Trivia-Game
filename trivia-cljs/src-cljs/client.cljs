(ns trivia.client
  (:require [goog.net.XhrIo :as xhr]
            [cljs.reader :as reader]
            [domina :as d]
            [goog.dom.forms :as gforms]
            [domina.events :as events]))

; shashank

(def btn-get-question-id "btn-get-question")
(def btn-check-answer-id "btn-check-answer")
(def btn-logout-id "btn-logout")
(def div-question-id "question-div")
(def div-result-id "result-div")

(def select-radio-quiz-1-id "select-radio-quiz-1")
(def select-radio-quiz-2-id "select-radio-quiz-2")
(def quiz-quiestions-id      "quiz-quiestions")
(def quiz-final-total-id    "quiz-final-total")


(def quiz-question-count (atom []))
(def quiz-user-input (atom []))
(def quiz-number (atom "q1"))



(def result-user "user-result")
(def result-password "password-result")
(def expr-userid "eval-userid")
(def expr-password "eval-password")
(def url "/eval-user")
(def url1 "/eval-pass")
; new
(def select-show-quiz-id "select-show-quiz")
(def select-show-grades-id "select-show-grades")

(defn serialize [m] (str m))

(defn receive-result [event]
  (d/set-text! (d/by-id result-password)
               (.getResponseText (.-target event))))

(defn receive-result1 [event]
  (d/set-text! (d/by-id result-user)
               (.getResponseText (.-target event))))

(defn post-for-eval [expr-str]
  (xhr/send "/eval-pass" receive-result "POST" expr-str))

(defn post-for-eval1 [expr-str]
  (xhr/send "/eval-user" receive-result1 "POST" expr-str))

(defn check-logout []
  (xhr/send "/logout" receive-result1 "GET" ""))

(defn get-expr []
  (let [password (.-value (d/by-id expr-password))]
    (serialize {:password password})))

(defn get-expr1 []
  (let [username (.-value (d/by-id expr-userid))]
    (serialize {:username username})))

(defn de-serialize [s] (reader/read-string s))


;1

; ===============================================================================

(defn get-all-user-quiz-score
  [event]
  (.log js/console (str "get all user quiz score"))
  (xhr/send "/get-all-quiz-score" receive-all-user-quiz-score "POST" "")
  (events/stop-propagation event)
  (events/prevent-default event))

(defn receive-all-user-quiz-score[event]
  (d/set-inner-html! (d/by-id "show-all-user-quiz-score")
                     (quiz-score-to-html (.getResponseText (.-target event))))
  (d/set-inner-html! (d/by-id "get-quiz-questions") (str ""))
  (d/set-inner-html! (d/by-id "select-quiz-button") (str "")))


(defn quiz-score-to-html [data]
  (.log js/console  data)
  ;(let [data1 (de-serialize data)
    ;    uname (get-in data1 [:username])
    ;    qscore (get data1 :user_quiz_score)]
  ;      username (:username data1)
  ;      quiz_score (:user_quiz_score data1)]
  ; (.log js/console username)
   ; (.log js/console uname))
  (str "<table border=1>"
       "<tr> <td>Quiz</td><td>Score</td> </tr>"
       "<tr> <td>1</td><td>2/2</td></tr>"
       "<tr> <td>2</td><td>2/3</td></tr>"
       "</table>"))


(defn get-all-quiz [event]
  (.log js/console (str "get all quiz"))
  (xhr/send "/get-all-quiz" receive-all-quiz "POST" "")
  (events/stop-propagation event)
  (events/prevent-default event))

(defn receive-all-quiz [event]
  (d/set-inner-html! (d/by-id "vatsal")
                     (show-all-quizes (.getResponseText (.-target event)))))

(defn show-all-quizes [data]
  ;(.log js/console data)
  (let [d (de-serialize data)
        quiz_nos (:allquiz d)]
  (str "<form>"(all-quizes-to-html quiz_nos) "</form>")))

(defn all-quizes-to-html [data]
  (apply str (map button-to-html data)))

(defn button-to-html [data]
  (def q_val (str "Quiz-" data))
  (str "<input type=button id=select-quiz-button class=btn btn-primary onclick=trivia.client.buttonalert('"data"'); value="q_val"></button>&nbsp;&nbsp;"))

(defn ^:export buttonalert[data]
  (get-quiz data))


(defn get-quiz [data]
  (let [body (serialize {:quiz-no data})]
    (xhr/send "/get-quiz" receive-get-quiz "POST" body)))

(defn receive-get-quiz [data]
 (d/set-inner-html! (d/by-id "get-quiz-questions")
                    (quiz-to-html (.getResponseText (.-target data))))
 (d/set-inner-html! (d/by-id "show-all-user-quiz-score") (str "")))

(defn choice-to-html [choice]
  (str "<li>" choice "</li>"))

(defn choices-to-html [choices]
  (apply str (map choice-to-html choices)))

(def score (atom 0))

;@score
(defn fun1 [data]
  ;(swap! score inc)
  (str "<h3>" (:id data) "." "</h3>"
       "<div>" (get-in data [:value :q])
       "<ol>"
       (choices-to-html (get-in data [:value :options]))
       "</ol>"
       "<input id=answer-text-box-"(str(get-in data [:value :l]))" type=text />"
       "<input id=ques_no type=hidden value="(str(get-in data [:value :l]))" />"
       "</div>"))

(defn quiz-to-html [data]
  (let [quiz (de-serialize data)
        q_no (:q_no quiz)
        qq (:quiz quiz)
        questions (get-in quiz [:quiz :questions])
        que_count (count questions)]
      (.log js/console (str "Question Count") que_count)
    (str "<form id=quiz-submit-form>"
        (map fun1 questions)
         "<input id=quiz-que-count type=hidden value="que_count">"
         "<input id=user-quiz-number type=hidden value="q_no">"
         "<br/> <input id=submit-quiz-button class=btn btn-primary type=button value=Done onclick=trivia.client.submitquiz(); />"
         "<br/> <br/> </form>")))

(defn ^:export submitquiz []
  ;(.alert js/window (str "Vatsal Sevak"))
  (def temp_list [])
  (def temp_val 0)
  (let [quiz_no   (.-value (d/by-id "user-quiz-number"))
        que_count (.-value (d/by-id "quiz-que-count"))
        text_box1 (.-value (d/by-id "answer-text-box-q1"))
        text_box2 (.-value (d/by-id "answer-text-box-q2"))
        ;form_value (goog.json.serialize (gforms/getFormDataMap (d/by-id "quiz-submit-form")))

        m {:qno quiz_no :que_count que_count :text_box1 text_box1 :text_box2 text_box2}
        body (serialize {:qno quiz_no :que_count que_count :text_box1 text_box1 :text_box2 text_box2})]

        ;(loop [x que_count]
        ;  (when (> x 0)
              ;(= temp_val (int(.-value (d/by-id (str "answer-text-box-q" x)))))
              ;(list* temp_val temp_list)
        ;      (def xx (str "que-num" x))
         ;     (.log js/console (str "xx " xx))
         ;     (assoc m (keyword xx) (.-value (d/by-id (str "answer-text-box-q" x))))
              ;(.log js/console (str "Question no-" x (.-value (d/by-id (str "answer-text-box-q" x)))))
         ;     (recur (- x 1))
         ;   ))
        ;{:form form_value}
        ;(.log js/console (str "[i] check  textbox input: " form_value))
        ;(.log js/console (str "[i] check  textbox input: " (serialize m)))
        (.log js/console (str "[i] check  answer: " body))
        (xhr/send "/submit-user-quiz" receive-submit-user-quiz "POST" body)))

(defn receive-submit-user-quiz [event]
  (d/set-text! (d/by-id "user-quiz-score")
               (.getResponseText (.-target event))))

; ===========================================================================

(defn question-to-html [question]
  (let [q (:q question)]
    (swap! quiz-question-count conj (:l question))
    (.log js/console (str "test:" @quiz-question-count))
    (str "<form>"
         q "<br/>"
         "<ol>"
         (choices-to-html (:c question))
         "</ol>"
         "<input id=answer-box-" (:l question) " type=text />"
         "</form>")))

(defn questions-to-html [questions]
  (apply str (map question-to-html questions)))

(defn quiz-to-html[buffer]
  (let [m (de-serialize buffer)
        questions (:questions m)
        title (:title m)
        quiz-num (str "q" (:exam-number m))]
        ;;question-count (count questions)]
    ;;(swap! quiz-question-count question-count)
    ;;(.log js/console (str "Question count: " question-count))
    (reset! quiz-number quiz-num)
    (.log js/console (str "quiz-number" @quiz-number))
    (str
     "<div>"
     "<h2>" title "</h2>"
         (questions-to-html questions)
         "</div>")))

(defn receive-question-callback [event]
  (reset! quiz-question-count [])
  (d/set-inner-html! (d/by-id "quiz-div")
                     (quiz-to-html (.getResponseText (.-target event)))))

(defn get-quiz [event quiz-number]
  (.log js/console (str "[i] get question"))
  (xhr/send (str "/quiz/" quiz-number) receive-question-callback "GET" "")
  (events/stop-propagation event)
  (events/prevent-default event))



;=== result


(defn read-field
  [quiz-question]
  (let [a (.-value (d/by-id (str "answer-box-" quiz-question)))
        b @quiz-question-count]
    (swap! quiz-user-input conj a)
    (.log js/console (str "test2:" b))))
    ;;(let [a (.-value (d/by-id (str "answer-box-" answer)))]
      ;;(swap! quiz-user-input conj a))))

(defn check-answers []
  (let [b @quiz-question-count]
    (reset! quiz-user-input [])
    (mapv read-field b)
    (.log js/console (str "questions" b))
    (.log js/console (str "answers" @quiz-user-input)))
  )

(defn receive-result-callback [event]
  (.log js/console (str "[i] received result"))
  (d/set-text! (d/by-id quiz-final-total-id)
               (.getResponseText (.-target event))))


(defn check-answer [event]
  (check-answers)
  (let [
        b {:questions @quiz-question-count
           :answers @quiz-user-input
           :quiz-num @quiz-number
           }
        body (serialize b)]
    (.log js/console (str "[i] check answer: " body))
    (xhr/send "/check-answer" receive-result-callback "POST" body)
    (events/stop-propagation event)
    (events/prevent-default event)))



;===========================

(defn ^:export main []
  (events/listen! (d/by-id expr-password)
                  :keyup
                  (fn [event]
                    (post-for-eval (get-expr))
                    (events/stop-propagation event)
                    (events/prevent-default event)))

  (events/listen! (d/by-id expr-userid)
                  :keyup
                  (fn [event]
                    (post-for-eval1 (get-expr1))
                    (events/stop-propagation event)
                    (events/prevent-default event)))

  (events/listen! (d/by-id btn-check-answer-id)
                  :click check-answer)

  (events/listen! (d/by-id "select-show-quiz")
                  :click get-all-quiz)

  (events/listen! (d/by-id select-show-grades-id)
                  :click get-all-user-quiz-score)

  (events/listen! (d/by-id btn-logout-id)
                  :click check-logout)

  (events/listen! (d/by-id btn-get-question-id)
                  :click get-question)

  (events/listen! (d/by-id select-radio-quiz-1-id)
                   :change get-quiz-1)

  (events/listen! (d/by-id select-radio-quiz-2-id)
                   :change get-quiz-2)

  (events/listen! (d/by-id "btn-q1")
                  :click (fn [event] (get-quiz event "q1")))
  (events/listen! (d/by-id "btn-q2")
                  :click (fn [event] (get-quiz event "q2")))
  (events/listen! (d/by-id "btn-q3")
                  :click (fn [event] (get-quiz event "q3"))))
