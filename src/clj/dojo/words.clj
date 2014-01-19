(ns dojo.words
  (:require [clojure.string :refer [lower-case split-lines]]))

(defn file-path [path]
  (str "resources/" path))

(def words
  (map lower-case
       (->> (file-path "word-list.txt")
       slurp
       split-lines)))