(ns app.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [json-html.core :as jh]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    ;[app.db :as db]
    [ethereumjs-tx :as eth]
    [ethereumjs-util :as eutil]
    [react-slidez :as slidez]
    [utf8-string-bytes :as str-byte]
    [moment]
    [app.sub]
    [app.events]
    [app.types :as t]))
;(:require [bootstrap :as b]))

(defn secret-to-private-key [s]
  (eutil/rlphash s))

(comment







  ;; 1 ETH = 1e18 wei


  (let [secret      "clj-address"
        private-key (eutil/rlphash secret)
        tx          #js {:nonce 1
                         ;; gasUsed
                         :gasPrice 1e9
                         :gasLimit 1e5 ;; -> 1e14
                         :to "0x00Dc6C45bCDad11Dd6fC5ABDe85bBEEdfBd63D88"
                         :value 1e17}
        etx          (eth/Transaction. tx #js {:chain 3})]
      (.sign etx private-key)
      (println :rawtx (.toString (.serialize etx) "hex"))
      (println :hash  (-> etx
                          .hash
                          (.toString "hex"))))
























  (-> "clj-address"
    eutil/rlphash
    eutil/privateToAddress
    (.toString "hex"))


  (let [tx #js {:nonce 0,
                :gasPrice 1e9,
                :gasLimit 100000,
                :to "0x00Dc6C45bCDad11Dd6fC5ABDe85bBEEdfBd63D88"
                :value 1e9,
                :data nil}
        priv-key (-> "clj-address" eutil/rlphash)
        etx(eth/Transaction. tx #js {:chain 3})]
    (println :sign (.. etx (sign priv-key)))

    ;(println :tx-serialize (-> etx .serialize (.toString "hex")))
    (println :tx-serialize (.. etx serialize (toString "hex")))
    (println :hash (-> etx
                       (.hash true)
                       (.toString "hex"))))

  :eth
  (let [rawtx #js ["0x00"
                   "0x09184e72a000"
                   "0x2710"
                   "0x0000000000000000000000000000000000000000"
                   "0x00"
                   "0x7f7465737432000000000000000000000000000000000000000000000000000000600057"
                   "0x1c"
                   "0x5e1d3a76fbf824220eafc8c79ad578ad2b67d01b0c2425eb1f1347e8f50882ab"
                   "0x5bd428537f05f9830e93792f90ea6a3e2d1ee84952dd96edbae9f658f831ab13"]]
    rawtx
    (let [Tx eth/Transaction]
       [(-> (Tx. rawtx)
            .verifySignature)
        (-> (Tx. rawtx)
            .getSenderAddress
            (.toString "hex"))])))

(rf/reg-event-db
  :initialize
  (fn [_ _]
    (println :initialized)
    {:panel nil
     :creators ["Vitalik"]
     :mempool []
     :blocks [(t/Block. 0 (js/Date.) [(t/Tx. :gen0 nil :vitalik 50 nil 0 0)])]}))


(defn mempool-panel []
  [:div
    [:h2 "mempool"]
    (let [txs @(rf/subscribe [:mempool])]
      (json-html.core/edn->hiccup txs))])

(defn wallet-txs [wallet]
  (let [txs @(rf/subscribe [:wallet/txs wallet])]
    [:div
     [:h3 (str "wallet: " wallet)]
     (json-html.core/edn->hiccup txs)]))


(defn experiment-panel []
  [:div
   [wallet-txs :ziko]
   ;[wallet-txs :vitalik]
   [mempool-panel]])

(defn block-component [{:keys [no timestamp txs :as b]}]
  (let [number no
        hash "0x16660abc742c9f41cf1e9f4236c68c053eebfc309abce1278977708f35cb1d6c"
        txs-count (count txs)
        txs-bytes (* 16 txs-count)
        sum-amount (reduce + (map :amount txs))
        time (-> timestamp moment .fromNow)]

    ;[:div.tile.tile-type-transaction.fade-up
    [:div.tile.tile-type-block.fade-up
       {:data-selector "block-tile"
        :data-block-number number
        :data-block-hash hash}
       [:div.row
        [:div.tile-transaction-type-block.col-md-2.d-flex.flex-row.flex-md-column
          [:a.tile-label
           {:data-selector "block-number"
            :href (str "/blocks/" number)}
           (str "#" number)]
          [:span.tile-status-label.font-weight-400
            {:data-test "transaction_type"}
            "Block"]]
        [:div.col-md-6.col-lg-8
         [:div
            [:span.mr-2 (str txs-count " transactions")]
            [:span.mr-2 (str txs-bytes " bytes")]
            [:span time]]
         #_[:div.text-nowrap.text-truncate.mt-3.mt-md-0 "Miner"
            [:a {:data-test "address_hash_link",
                 :href "/address/0xfd54b38ed9e181c5632c692afe168487b1a40814"}
             [:span {:data-address-hash "0xfd54B38eD9E181C5632c692aFE168487B1A40814"}
              [:span.d-none.d-md-none.d-lg-inline "0xfd54B38eD9E181C5632c692aFE168487B1A40814"]
              [:span.d-md-inline-block.d-lg-none "0xfd54–a40814"]]]]
         [:div.text-nowrap.text-truncate.mt-3.mt-md-0
          "Total amount"
          [:span.ml-2 (str sum-amount " ETH")]]]
        #_[:div.col-md-4.col-lg-3.text-md-right.d-flex.flex-column.align-items-md-end.justify-content-md-end.mt-3.mt-md-0
           [:span "8,000,000 Gas Limit"]
           #_[:div {:class "mr-3 mr-md-0"} "0 0%\nGas Used"]
           #_[:div {:class "progress"}
              [:div {:class "progress-bar",
                     :role "progressbar",
                     :style {:width "0%"}
                     :aria-valuenow "50",
                     :aria-valuemin "0",
                     :aria-valuemax "100"}]]]]]))

(defn card
  ([body]
   [:div.card>div.card-body
    body])
  ([{:keys [title]} body]
   [:div.card>div.card-body
    (when-not (empty? title) [:h1.card-title title])
    body]))

(defn blocks-panel [blocks]
  [:div.row
   [:section.container {:data-page "block-list"}
    [:div.card
     [:div.card-body {:data-async-listing "/blocks"}
      [:h1.card-title "Blocks"]
      [:div {:data-items ""}
       (for [{:keys [no :as b]} blocks]
         ^{:key no}[block-component b])]]]]])

(def eth-rpc "https://eth.jigko.net")

(def eth-rpc-proxy "http://localhost:8888/proxy")

(comment
  :hi
  ;curl https://cloudflare-eth.com -H '' --data '{"jsonrpc":"2.0","method":"eth_getBlockByNumber","params":["0x7ced32", true],"id":1}'

  (go (let [response (<! (http/post eth-rpc {:headers {"Content-Type" "application/json"}
                                             :with-credentials? false
                                             :json-params {:jsonrpc "2.0"
                                                           :id 1
                                                           :method "eth_getBlockByNumber"
                                                           :params ["0x7ced32" true]}}))
            result (get-in response [:body :result])]
         (prn :status (:status response))
         (println :keys (keys result))
         (println :keys-cnt (map (juxt first (comp count second)) result))
         (cljs.pprint/pprint (map (juxt first (comp count second)) result))
         (println :txs (count (:transactions result)))))

  (.fromNow (moment (js/Date.)))
  (let [s (aget slidez "default")]
     s))

(defn slide [content]
  [:div
   {:style {:background-color "#eee"
            :height "100%"}}
   content])

(defn slide-center [& child]
  [:div.loading-center {:style {:width "80%"}}
   child])

(defn code [& child]
 [:div.alert.alert-primary
  child])

(defn slide-panel []
  (let [sz (aget slidez "default")]
    [:div.row ;{:style {:background-color "black"}}
     [:> sz
      {:show-index true
       :show-arrow true
       :enable-keyboard true
       :autoplay false
       :default-index 0
       :effect "bounce-right"
       :height "100%"
       :width "100%"}

      [slide
       [:div.loading-center
        [:h1.display-4 "Ethereum for Blockchain developer"]]]
      [slide
       [slide-center
        [:div
         [:h1.display-3 "Account/Wallet (secp256k1)"]]
        [:div
         [:h2.display-4 "#Private Key"]
         [code
           [:h3.display-4 "Data: 32 bytes"]]]]]
      [slide
       [slide-center
        [:div
         [:h2.display-4 "#Public Key"]
         [code
           [:h3.display-4 "(conj [] 0x04 X Y) ;; |0x04|=1 |x|=32 |y|=32"]
           [:h3.display-4 ";; byte[65]: 04XXXXXXXXYYYYYYYY"]]]
        [:div
         [:h2.display-4 "#Address"]
         [code
           [:h3.display-4 "(let [address (drop 12 (sha3 pubkey))])"]
           [:h3.display-4 "(str \"0x\" address-bytes)"]]]]]
      [slide
       [slide-center
        [:h1.display-3 "Signing"]]]
      [slide
       [slide-center
        [:h2.display-4 "Block"]
        [:h2.display-4 "Transaction"]
        [:h2.display-4 "SmartContract"]]]
      [slide
       [slide-center
        [:h1.display-4 "Clojure(script)"]
        [:h3.display-4 "Clojure is a dialect of Lisp, and shares with Lisp the code-as-data philosophy and a powerful macro system. Clojure is predominantly a functional programming language, and features a rich set of immutable, persistent data structures. When mutable state is needed, Clojure offers a software transactional memory system and reactive Agent system that ensure clean, correct, multithreaded designs."]]]
      [slide
       [slide-center
        [:h1.display-4 "Why Clojure(script)?"]
        [:h3.display-4 "- REPL -> Fast feedback loop -> Shorter dev. time"]
        [:h3.display-4 "- Macro -> Extend the language"]
        [:h3.display-4 "- Interop - JVM, JS"]
        [:h3.display-4 "- Dynamic"]
        [:h3.display-4 "- Practical"]]]
      [slide
       [slide-center
        [:h2.display-4 "Static type?"]
        [:a {:href "https://youtu.be/2V1FtfBDsLU?t=4097"}
         [:h3.display-4 "Types help on software maintenance"]]]]]]))

(defonce panel (r/atom nil))

(defn balance [[wallet bal]]
  [:div.row
   [:div.col-md-6 wallet]
   [:div.col-md-6 bal]])


(defn world-state-panel [state]
  ;; <p class="attribute-hereee">this is children</p>
  ;; [tag/component {map-attributes} children]
  (comment
    (let [state @(rf/subscribe [:world-state])]
       state))
  [:div
   [:h3 "World state"]
   (for [s state]
     [balance s])])

(defn ether-panel []
  (let [blocks @(rf/subscribe [:blocks-sorted])]
    [:div.container
     [:h1.display-4 "Blockchain viz"]
     ;[:img {:src "./cutie-fox-loading.svg"}]

     [:div.row
       [:div.col-md

        [card {:title "World state"}
          (let [state @(rf/subscribe [:world-state])]
            [world-state-panel state]
            #_(json-html.core/edn->hiccup state))]
        [card {:title "Experiments"} [:div.row>div.col-md [experiment-panel]]]]
       [:div.col-md
        [blocks-panel blocks]
        (json-html.core/edn->hiccup blocks)]]]))

(comment
  panel
  (reset! panel :slide)
  (reset! panel :ether))

(defn main-panel []
   (let [active-panel @panel]
     [:div
      (case active-panel
        :ether [ether-panel]
        :slide [slide-panel]
        [:div.container>div.jumbotron
         [:h1 "Please select panel"]])]))

(comment
  (println "heyyy")
  (rf/dispatch [:creators/add "ziko"])

  (rf/dispatch [:tx/new (t/Tx. :mem1 :vitalik :ziko :30 nil 1 1)])
  (rf/dispatch [:tx/new (t/Tx. :mem2 :vitalik :ziko :30 nil 2 2)])
  (rf/dispatch [:tx/new (t/Tx. :mem3 :ziko :satoshi :5 nil 1 3)])

  (rf/dispatch-sync [:initialize])

  (rf/dispatch [:blocks/add (t/Block. 1 (js/Date.) [(t/Tx. :gen1 nil      :vitalik 50 nil 0 0)])])
  (rf/dispatch [:blocks/add (t/Block. 2 (js/Date.) [(t/Tx. :gen2 :vitalik :ziko 10 nil 0 1)])])
  (rf/dispatch [:blocks/add (t/Block. 3 (js/Date.) [(t/Tx. :gen3 :vitalik :ziko 20 nil 1 2)
                                                    (t/Tx. :gen4 :ziko :satoshi 5 "donate" 0 1)])])

  (json-html.core/edn->hiccup {:foo [1 2 3] :bar "baz"})

  (edn->html {:foo [1 2 3] :bar "baz"})

  @re-frame.db/app-db

  @(rf/subscribe [:creators])
  (let [blocks @(rf/subscribe [:blocks])]
    (json-html.core/edn->hiccup blocks)))


(comment

  (+ 1 2 3 4 5 6)

  (merge-with
    (fn [x y] (* x y))
    {:a 3}
    {:a 2 :b 2})

  (let [blocks @(rf/subscribe [:blocks])
        ops (for [{:keys [txs]} blocks
                  {:keys [from to amount]}            txs]
              [{from (- amount)}
               {to amount}])
        flat-ops (mapcat identity ops)
        non-nil (remove (comp nil? first first) flat-ops)]
     (apply merge-with + non-nil)))












(defn init []
  (r/render-component [main-panel] (js/document.getElementById "app")))

(defn main! []
  (println "main!")
  (rf/dispatch-sync [:initialize])
  (init))

(defn reload! []
  (println "reload!")
  (init))
