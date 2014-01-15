(ns dojo.trie
  (:require [clojure.string :refer [lower-case split-lines]]))

(defn file-path [path]
  (str "resources/" path))

(def words
  (map lower-case
       (->> (file-path "word-list.txt")
       slurp
       split-lines)))

(defn in?
  "true if seq contains elem"
  [seq elem]
  (some #(= elem %) seq))

; extended from SO post http://stackoverflow.com/questions/1452680/clojure-how-to-generate-a-trie

(defn add-to-trie [trie elem]
  "given a trie (hashmap), return a new hashmap with elem added"
  (assoc-in trie elem {:terminal true}))

(defn in-trie? [trie elem]
  (get-in trie `(~@elem :terminal)))

(defn prefixes [trie prefix]
  (assoc-in {} prefix (get-in trie prefix)))

(defn is-prefix? [trie prefix]
  (map? (prefixes trie prefix)))

; work in progress
; (loop [frontier (first pt)
;        word (str)]
; (if (= (key frontier) :terminal)
;    word
;    (recur (first (val frontier)) (str word (key frontier)))))

