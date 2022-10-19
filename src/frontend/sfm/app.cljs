(ns sfm.app (:require [reagent.core :as r]
                      [reagent.dom :as rd]
                      ))
(defn app []
  [:div {:class "app"}
   [:h1 "Hello World"]])

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
(rd/render [app] (-> js/document (.getElementById "app")))
(js/console.log "this get's called every time you change and save one of the files, reloading the app"))

(defn init []
  (println "this gets called the first time the app gets loaded")
  (start)
  ())
