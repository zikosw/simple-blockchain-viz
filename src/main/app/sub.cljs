(ns app.sub
  (:require [re-frame.core :as rf]))


(rf/reg-sub
  :blocks
  (fn [db _]     ;; db is current app state. 2nd unused param is query vector
    (:blocks db))) ;; return a query computation over the application state

(rf/reg-sub
  :blocks-sorted
  :<- [:blocks]
  (fn [blocks _]
    (reverse blocks)))

(rf/reg-sub
  :creators
  (fn [db _]
    (:creators db)))

(rf/reg-sub
  :mempool
  (fn [db _]
    (:mempool db)))

(rf/reg-sub
  :world-state
  :<- [:blocks]
  (fn [blocks _]
    (let [blocks @(rf/subscribe [:blocks])
          ops (for [{:keys [no txs :as b]} blocks
                    {:keys [from to amount fee :as tx]} txs]
                  [{from (- amount)}
                   {from (- fee)}
                   {:fee fee}
                   {to amount}])]
      (->> ops
          (mapcat identity)
          (remove (comp nil? first keys))
          (apply merge-with +)))))

(comment
  (keys {:a 1})
  (let [blocks @(rf/subscribe [:blocks])
        ops (for [{:keys [no txs :as b]} blocks
                  {:keys [from to amount fee :as tx]} txs]
               [
                {from (- amount)}
                {from (- fee)}
                {to amount}])]
    (->> ops
      (mapcat identity)
      (remove (comp nil? first keys))
      (apply merge-with +))))




       ;[{:wallet from :amount (neg amount)}
       ; {:wallet to   :amount amount}])))

