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


(rf/reg-sub
  :balance
  :<- [:world-state]
  (fn [state [_ wallet]]
    (get state wallet)))


(rf/reg-sub
  :wallet/txs
  :<- [:blocks]
  (fn [blocks [_ wallet]]
    (let [txs (for [{:keys [timestamp txs :as b]} blocks
                    {:keys [id from to :as tx]} txs]
                {from [{:type :out :id id :time timestamp}]
                 to [{:type :in :id id :time timestamp}]})
      ;(cljs.pprint/pprint txs)
          wallets (->> txs
                       (apply merge-with into)
                       (remove (comp nil? first))
                       (into {}))]
       (get wallets wallet))))

(comment
  (keys {:a 1})

  (rf/subscribe [:balance :vitalik])
  (rf/subscribe [:balance :fee])

  (rf/subscribe [:wallet/txs :satoshi])
  (rf/subscribe [:wallet/txs :ziko])

  (merge-with +  {:a 1 :b 2} {:b 1})
  ;; sub :user/txs
  (let [blocks @(rf/subscribe [:blocks])
        txs (for [{:keys [timestamp txs :as b]} blocks
                  {:keys [id from to :as tx]} txs]
              {from [{:type :out :id id :time timestamp}]
               to [{:type :in :id id :time timestamp}]})]
    ;(cljs.pprint/pprint txs)
    (->> txs
         (apply merge-with into)
         (remove (comp nil? first))
         (into {}))))



      ;[{:wallet from :amount (neg amount)}
      ; {:wallet to   :amount amount}])))
