(ns app.types)

(defrecord Tx [id from to amount data nonce fee])

(defrecord Block [no timestamp txs])
