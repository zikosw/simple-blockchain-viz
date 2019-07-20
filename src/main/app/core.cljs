(ns app.core
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [json-html.core :as jh]
    ;[app.db :as db]
    [app.sub]
    [app.events]
    [app.types :as t]))
;(:require [bootstrap :as b]))

(rf/reg-event-db
  :initialize
  (fn [_ _]
    (println :initialized)
    {:creators ["Vitalik"]
     :mempool []
     :blocks [(t/Block. 0 (js/Date.) [(t/Tx. :gen0 nil :vitalik 50 nil 0 0)])]}))

(defn main-panel []
  [:div.container
   [:h2 "Blockchain viz"]
   [:div.row
     [:div.col-md
      [:div.row
       [:div.col-md
        [:h2 "world state"]
        (let [txs @(rf/subscribe [:world-state])]
          (json-html.core/edn->hiccup txs))]]
      [:div.row
       [:div.col-md
        [:h2 "mempool"]
        (let [txs @(rf/subscribe [:mempool])]
          (json-html.core/edn->hiccup txs))]]]
     [:div.col-md
      [:h2 "blocks"]
      (let [blocks @(rf/subscribe [:blocks-sorted])]
         (json-html.core/edn->hiccup blocks))]]])

(comment
  (println "heyyy")
  (rf/dispatch [:creators/add "ziko"])
  (rf/dispatch [:tx/new (t/Tx. :id :vitalik :ziko :30 nil 0 1)])

  (rf/dispatch-sync [:initialize])

  (rf/dispatch [:blocks/add (t/Block. 1 (js/Date.) [(t/Tx. :gen0 nil      :vitalik 50 nil 0 0)])])
  (rf/dispatch [:blocks/add (t/Block. 2 (js/Date.) [(t/Tx. :gen1 :vitalik :ziko 10 nil 0 1)])])
  (rf/dispatch [:blocks/add (t/Block. 3 (js/Date.) [(t/Tx. :gen2 :vitalik :ziko 20 nil 0 2)
                                                    (t/Tx. :gen3 :ziko :satoshi 5 "donate" 0 1)])])

  (json-html.core/edn->hiccup {:foo [1 2 3] :bar "baz"})
  ;(json-html.core/edn->html {:foo [1 2 3] :bar "baz"})

  (edn->html {:foo [1 2 3] :bar "baz"})

  @re-frame.db/app-db

  @(rf/subscribe [:creators])
  (let [blocks @(rf/subscribe [:blocks])]
    (json-html.core/edn->hiccup blocks))


  (println "hello from nrepl"))


(defn init []
  (r/render-component [main-panel] (js/document.getElementById "app")))

(defn main! []
  (println "main!")
  (rf/dispatch-sync [:initialize])
  (init))

(defn reload! []
  (println "reload!")
  (init))
