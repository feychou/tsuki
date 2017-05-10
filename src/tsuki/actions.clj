(ns tsuki.actions
  (:gen-class)
  (:require [tsuki.facebook :as fb]
            [tsuki.utils :as utils]
            [taoensso.faraday :as far]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure.core.async
             :as a
             :refer [<! go timeout]]
            [environ.core :refer [env]]))

(defn get-astro-pic [date]
  (json/read-str
    (slurp (str "https://api.nasa.gov/planetary/apod?api_key=" (env :nasa-api-key) "&date=" date))
    :key-fn keyword))

(defn get-today-astro-pic []
  (get-astro-pic nil))

(defn get-chunks [chunk]
  (re-seq #"[^.!?;]+[.!?;]?" chunk))

(defn send-subscribe-prompt [user-id]
  (fb/send-message user-id (fb/text-message (str "If you subscribe I'll make sure to send you a new astropic every day " (format "%c" (int 128302)))))
  (fb/send-message user-id (fb/quick-replies-message "Do you want to subscribe?"
                                                     [{:content_type "text"
                                                       :title "Yes"
                                                       :payload "SUBSCRIBE"}
                                                      {:content_type
                                                       "text" :title "No"
                                                       :payload "NO_SUBSCRIPTION"}])))

(defn send-unsubscribe-prompt [user-id]
  (fb/send-message user-id (fb/quick-replies-message "Do you want to unsubscribe?"
                                                     [{:content_type "text"
                                                       :title "Yes"
                                                       :payload "UNSUBSCRIBE"}
                                                      {:content_type "text"
                                                       :title "No"
                                                       :payload "NO_UNSUBSCRIPTION"}])))

(defn send-menu [user-id]
  (fb/send-message user-id 
  (fb/button-template "What do you want me to do next?"
                      [{:title "Send today's APOD"
                        :type "postback"
                        :payload "TODAY_APOD"}
                       {:title "Send yda's APOD"
                        :type "postback"
                        :payload "YESTERDAY_APOD"}
                       {:title "Send random APOD"
                        :type "postback"
                        :payload "RANDOM_APOD"}])))

(defn send-apod-description [user-id pic]
  (let [pic-date (s/split (:date pic) #"-")]
    (fb/send-message user-id 
      (fb/button-template (first (get-chunks (:explanation pic))) 
                           [{:title "Read more"
                             :type "web_url"
                             :url (str "https://apod.nasa.gov/apod/ap" (subs (first pic-date) 2 4) (second pic-date) (nth pic-date 2) ".html")}]))))

(defn send-astropic-template [user-id pic & {:keys [toast menu subscribe-prompt] 
                                             :or {toast false menu true subscribe-prompt false}}]
  (when (true? toast)
    (fb/send-message user-id (fb/text-message "Here is your astropic ☾")))
  (fb/send-message user-id (fb/image-message (:hdurl pic)))
  (go
    (fb/send-message user-id (fb/text-message (:title pic)))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (send-apod-description user-id pic)
    (when (true? menu) 
      ((fb/type-on user-id)
       (<! (timeout 1000)
       (send-menu user-id))))
    (when (true? subscribe-prompt)
      (send-subscribe-prompt user-id))))


(defn send-astro-emoji [user-id]
  (let [emojis [128125 128156 127773 127770 127776 128302 128126 128640]]
    (fb/send-message user-id (fb/text-message (format "%c" (int (rand-nth emojis)))))))

(defn save-subscriber [user-id]
  (far/put-item utils/dynamo-creds
                :tsuki-users {:fb-id user-id}))

(defn delete-subscriber [user-id]
  (far/delete-item utils/dynamo-creds
                   :tsuki-users {:fb-id user-id}))

(defn get-subscriber [user-id]
  (far/get-item utils/dynamo-creds 
                :tsuki-users {:fb-id user-id}))

(defn greet [user-id]
  (go
    (fb/send-message user-id (fb/text-message "Hi earthling ☾"))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (fb/send-message user-id (fb/text-message "I am Tsuki and I report space facts to you."))
    (fb/type-on user-id)
    (<! (timeout 2000))
    (fb/send-message user-id (fb/text-message "Tap on the menu icon below whenever you feel like it."))))

(defn on-menu-pick
  ([user-id] 
    (let [is-subscriber (not (nil? (get-subscriber user-id)))]
      (send-astropic-template user-id (get-today-astro-pic) :menu is-subscriber :subscribe-prompt (not is-subscriber))))
  ([user-id date] (send-astropic-template user-id (get-astro-pic date)))
  ([user-id date postback] 
    (let [today-pic (get-today-astro-pic)
          chosen-pic (get-astro-pic date)]
        (if (not= (:url today-pic) (:url chosen-pic))
          (send-astropic-template user-id chosen-pic)
          (send-astropic-template user-id (get-astro-pic utils/day-before-yesterday))))))

(defn on-manage-subscription [user-id] 
  (if (nil? (get-subscriber user-id))
    (send-subscribe-prompt user-id)
    (send-unsubscribe-prompt user-id)))


(defn motivate [user-id]
  (fb/send-message user-id (fb/text-message "Good choice."))
  (fb/send-message user-id (fb/text-message (str "Space is way too dangerous on your own " utils/full-moon))))

(defn demotivate [user-id]
  (fb/send-message user-id (fb/text-message (str "Your chances of becoming an astronaut on your own are embarrassingly close to zero " utils/full-moon)))
  (fb/send-message user-id (fb/text-message "Did you know?")))

(defn subscribe [user-id]
  (save-subscriber user-id)
  (fb/send-message user-id (fb/text-message "Space is dangerous on your own."))
  (fb/send-message user-id (fb/text-message (str "I hope I'll make for a nice companion " utils/full-moon))))
  
(defn unsubscribe [user-id]
  (delete-subscriber user-id)
  (fb/send-message user-id (fb/text-message "It's sad to see you go."))
  (fb/send-message user-id (fb/text-message (str "I'll be here if you need me again " utils/full-moon))))
  
