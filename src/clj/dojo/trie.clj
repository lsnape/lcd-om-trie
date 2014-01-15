(ns dojo.trie
  (:require [clojure.string :refer [lower-case split-lines]]))

(defn file-path [path]
  (str "resources/" path))

(def words
  (map lower-case
       (->> (file-path "word-list.txt")
       slurp
       split-lines)))

; helper method !!! might be suitable somewhere ese
(defn in?
  "true if seq contains elem"
  [seq elem]
  (some #(= elem %) seq))

; from SO post http://stackoverflow.com/questions/1452680/clojure-how-to-generate-a-trie

; to understand what add-to-trie is doing, we first have to understand assoc-in
; assoc-in takes 3 args: a map, an arbitrary sequence of KEYS, and a value
;
;   >> (assoc-in {} ["a" :a 1] 42) ; returns {"a" {:a {1 42}}}
;
; in our case, we treat the string as a sequence of chars
; the terminal key/value pair is used to denote the end of a word in the trie

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

