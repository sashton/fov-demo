(ns fov-demo.core
  (:gen-class)
  (:require [lanterna.screen :as s]
            [fov-demo.heckendorf-fov :as heckendorf])
  (:import (squidpony.squidgrid.mapping DungeonGenerator DungeonUtility)
           (squidpony.squidgrid FOV)))

(defonce world (atom nil))
(defonce screen (atom nil))

(def fov-types {FOV/SHADOW "Shadow"
                FOV/RIPPLE "Ripple"
                FOV/RIPPLE_LOOSE "Ripple Loose"
                FOV/RIPPLE_TIGHT "Ripple Tight"
                FOV/RIPPLE_VERY_LOOSE "Ripple Very Loose"})

(defn next-fov-type
  [type]
  (if (= FOV/SHADOW type)
    FOV/RIPPLE
    (inc type)))

(defn clear
  [screen]
  (let [[size-x size-y] (s/get-size screen)]
    (doseq [x (range size-x)
            y (range size-y)]
      (s/put-string screen x y " "))))

(defn cell-type
  [dungeon coord-x coord-y]
  (aget dungeon coord-x coord-y))

(defn draw-dungeon
  [screen dungeon visible-points seen-points offset-x]
  (doseq [x (range (alength dungeon))
          y (range (alength (aget dungeon x)))]
    (cond
      (get visible-points [x y])
      (do
        (s/put-string screen (+ x offset-x) y (str (cell-type dungeon x y))))

      (get seen-points [x y])
      (do
        (s/put-string screen (+ x offset-x) y (str (cell-type dungeon x y))
                      {:fg :blue})))))

(defn draw-divider
  [screen offset-x width height]
  (doseq [y (range height)
          x (range width)]
    (s/put-string screen (+ x offset-x) y "|" {:bg :white})))

(defn draw-player
  [screen x y]
  (s/put-string screen x y "0" {:bg :yellow}))

(defn generate-dungeon
  [width height]
  (let [generator (DungeonGenerator. width height)]
    (.generate generator)
    (.getBareDungeon generator)))

(defn generate-world
  [[size-x size-y :as screen-size]]
  (let [map-height size-y
        divider-width (if (odd? size-x) 1 2)
        map-width (/ (- size-x divider-width) 2)
        dungeon (generate-dungeon map-width map-height)]
    {:dungeon dungeon
     :divider-width divider-width
     :screen-size screen-size
     :dungeon-width map-width
     :dungeon-height map-height
     :player-position [(int (/ map-width 2))
                       (int (/ map-height 2))]
     :fov-type FOV/SHADOW
     :seen-left #{}
     :seen-right #{}
     :visible-left #{}
     :visible-right #{}}))

(defn visible-in-light-map
  [light-map]
  (->> (for [x (range (alength light-map))
             y (range (alength (aget light-map x)))]
         (when (pos? (aget light-map x y))
           [x y]))
       (filter identity)
       (into #{})))

(defn render
  [screen world]
  (clear screen)
  (let [{:keys [dungeon
                divider-width
                dungeon-height
                dungeon-width
                visible-left
                visible-right
                seen-left
                fov-type
                seen-right
                player-position]} world
        [player-x player-y] player-position
        map2-offset-x (+ dungeon-width divider-width)]

    (draw-dungeon screen dungeon visible-left seen-left 0)
    (draw-divider screen dungeon-width divider-width dungeon-height)
    (draw-dungeon screen dungeon visible-right seen-right map2-offset-x)

    (draw-player screen player-x player-y)
    (draw-player screen (+ player-x map2-offset-x) player-y)
    (s/put-string screen 0 0 (get fov-types fov-type) {:bg :yellow
                                                       :fg :black})

    (s/put-string screen (+ dungeon-width divider-width) 0
                  "Heckendorf" {:bg :yellow
                                :fg :black})

    (s/move-cursor screen dungeon-width 0)
    (s/redraw screen)))

(defn update-fov
  [world]
  (let [{:keys [dungeon
                fov-type
                player-position]} world
        [player-x player-y] player-position
        fov-radius 10

        ;; field of view for left screen
        ;; cycles though several options offered by squidlib library
        left-fov (let [resistance (DungeonUtility/generateSimpleResistances dungeon)
                       ^FOV fov (FOV. fov-type)
                       light-map (.calculateFOV fov resistance player-x player-y fov-radius)]
                   light-map)
        left-visible (visible-in-light-map left-fov)

        ;; field of view for right screen
        ;; uses code from heckendorf game
        right-visible (heckendorf/line-of-sight dungeon fov-radius [player-x player-y])]
    (-> world
        (update :seen-left into left-visible)
        (update :seen-right into right-visible)
        (assoc :visible-left left-visible)
        (assoc :visible-right right-visible))))

(defn move
  [world dir]
  (case dir
    :up (update-in world [:player-position 1] dec)
    :down (update-in world [:player-position 1] inc)
    :left (update-in world [:player-position 0] dec)
    :right (update-in world [:player-position 0] inc)))

(defn handle-input
  [world k]
  (cond
    (#{\a \h :left} k)
    (move world :left)

    (#{\s \j :down} k)
    (move world :down)

    (#{\d \l :right} k)
    (move world :right)

    (#{\w \k :up} k)
    (move world :up)

    (= \r k)
    (generate-world (:screen-size world))

    (= \t k)
    (update world :fov-type next-fov-type)

    (= \q k)
    (assoc world :quit true)

    :else
    world))

(defn handle-resize
  [cols rows]
  (reset! world (generate-world [cols rows]))
  (render @screen @world))

(defn startup
  [screen-type]
  (reset! screen (s/get-screen screen-type {:resize-listener handle-resize}))
  (s/start @screen)
  (reset! world (generate-world (s/get-size @screen)))
  (swap! world update-fov)
  (render @screen @world))

(defn shutdown
  []
  (s/stop @screen)
  (reset! screen nil)
  (reset! world nil))

(defn run
  [screen-type]
  (startup screen-type)
  (loop []
    (when (not (:quit @world))
      (try
        (let [w @world
              input (s/get-key-blocking @screen)
              w (handle-input w input)
              w (update-fov w)
              _ (render @screen w)]
          ;; only reset world if no exceptions were thrown
          (reset! world w))
        (catch Exception e
          (.printStackTrace e)))
      (recur)))
  (shutdown))

(comment
  (future
    (run :swing)))

(defn -main
  [& args]
  (run :text))
