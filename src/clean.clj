(ns clean
  (:require [me.raynes.fs :as fs]
            [clojure.string :as str]))

(def excludes #{"psx"})

(defn images
  [game]
  (let [path (-> (fs/parent game)
                 (.getPath)
                 (str "/images")
                 )]
    (fs/list-dir path)))

(defn -main [& args]

  (doseq [sys (fs/list-dir (first args))]

    (let [backup-path (str "roms-cleaned/" (fs/name sys))]
      (if (fs/exists? backup-path)
        (println (str "Backup location already exist. Using: " backup-path))
        (do
          (fs/mkdirs backup-path)
          (println (str "Created backup folder: " backup-path))))


      (doseq [game (fs/list-dir sys)]
        (let [game-name (fs/name game)
              image (filter #(str/includes? (fs/name %) game-name) (images game))]
          (when (empty? image)
            (when (fs/file? game)
              (fs/copy game (str backup-path "/" (fs/base-name game))))))))))
