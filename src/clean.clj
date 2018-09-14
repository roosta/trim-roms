(ns clean
  (:require [me.raynes.fs :as fs]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]))

(def cli-options
  [["-d" "--dry-run"]])

(defn images
  [game]
  (let [path (-> (fs/parent game)
                 (.getPath)
                 (str "/images")
                 )]
    (fs/list-dir path)))

(defn -main [& args]
  (let [opts (parse-opts args cli-options)
        base (first (:arguments opts))
        parent (.getPath (fs/parent base))]
    (if (fs/directory? base)
      (doseq [sys (fs/list-dir base)]

        (let [backup-path (str parent "/roms-cleaned/" (fs/name sys))]
          (doseq [game (fs/glob sys "*.zip")]
            (let [game-name (fs/name game)
                  image (filter #(str/includes? (fs/name %) game-name) (images game))]
              (when (and (empty? image) (fs/file? game))
                (let [target (fs/file (str backup-path "/" (fs/base-name game)))]
                  (if (-> opts :options :dry-run)
                    (println "Going to move" (.getPath game) "-->" (.getPath target))
                    (do
                      (fs/copy+ game target)
                      (fs/delete game)
                      (println "Moved" (.getPath game) "-->" (.getPath target))))))))))
      (binding [*out* *err*]
        (println "Argument supplied does not appear to be a valid directory:" (first (:arguments opts)))))))
