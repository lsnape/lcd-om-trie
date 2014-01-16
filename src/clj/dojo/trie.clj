(ns dojo.trie
  (:require [clojure.string :refer [lower-case split-lines blank?]]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :refer [walk]]))

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

(defn trie [words]
  (reduce add-to-trie {} words))

(defn remove-last-char [word]
  (if (blank? word)
    word
    (subs word 0 (- (count word) 1))))

(defn trie-to-vector [trie]
  (loop [current    trie
         parent     [{}]
         words      []
         word       ""]
    (if (empty? current)
      (if (empty? parent)
        words
        (recur (peek parent) (pop parent) words (if (blank? word)
                                                    word
                                                    (remove-last-char word))))
      (let [ch-key (key (first current))
            ch-val (val (first current))]
        (if (= ch-key :terminal)
          (recur (dissoc current ch-key) parent (conj words word) word)
          (recur ch-val (conj parent (dissoc current ch-key)) words (str word ch-key)))))))

(defn auto-complete [trie prefix]
  (trie-to-vector (prefixes trie prefix)))

; (auto-complete (trie words) "st")
