(ns app.events
  (:require
    [re-frame.core :as rf]
    [app.types :as t]))


(rf/reg-event-db
  :creators/add
  (fn [db [_ creator]]
    (update db :creators conj creator)))

(rf/reg-event-db
  :blocks/add
  (fn [db [_ block]]
    (update db :blocks conj block)))


;; with a little bit of processing
;(rf/reg-event-db
;  :blocks/new
;  (fn [db [_ no txs]]
;    (let [time (js/Date.)
;          block (t/Block. no time txs)]
;      (update db :blocks conj block))))

(rf/reg-event-db
  :tx/new
  (fn [db [_ tx]]
    (update db :mempool conj tx)))

(comment
  (conj [1] 2)
  (cons 2 [1]))