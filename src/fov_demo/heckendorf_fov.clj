;; This file contains code from https://github.com/uosl/heckendorf
;; heckendorf is licensed with the Eclipse Public License - v 1.0

(ns fov-demo.heckendorf-fov)

(defn- add-y [[y x] n] [(+ y n) x])
(defn- add-x [[y x] n] [y (+ x n)])
(defn- sub-y [[y x] n] [(- y n) x])
(defn- sub-x [[y x] n] [y (- x n)])

(defn- pos-line-y [len [y x]]
  (map (fn [rising-y] [rising-y x])
       (range y (+ y len))))
(defn- pos-line-x [len [y x]]
  (map (fn [rising-x] [y rising-x])
       (range x (+ x len))))
(defn- neg-line-y [len [y x]]
  (map (fn [falling-y] [falling-y x])
       (range y (- y len) -1)))
(defn- neg-line-x [len [y x]]
  (map (fn [falling-x] [y falling-x])
       (range x (- x len) -1)))

(defn eighth-pivot [n]
  "Creates a list of vectors of numbers from origo to n distance,
  which will create a gradual 45 degree pivot from origo when
  applied to coordinates"
  (reductions #(update %1 %2 inc)
              (vec (repeat n 0))
              (mapcat #(-> % (range n) reverse)
                      (range 1 n))))


(defn apply-pivot-yx
  "Takes multiple functions to apply pivot-f to a line from origo-yx"
  [pivot-f line-f add-coord-f len origo-yx]
  (mapv (partial mapv add-coord-f)
        (repeat (line-f len origo-yx))
        (pivot-f len)))

(defn complete-pivot [len origo-yx]
  "Creates nested vectors of a complete 360 degree pivot.
  Every other 45 degree section will have its last path vector
  popped, as it will overlap with the previous section."
  (let [apply-eighth-pivot (partial apply-pivot-yx eighth-pivot)]
    (concat
      (apply-eighth-pivot neg-line-y add-x len origo-yx)
      (pop (apply-eighth-pivot pos-line-x sub-y len origo-yx))
      (apply-eighth-pivot pos-line-x add-y len origo-yx)
      (pop (apply-eighth-pivot pos-line-y add-x len origo-yx))
      (apply-eighth-pivot pos-line-y sub-x len origo-yx)
      (pop (apply-eighth-pivot neg-line-x add-y len origo-yx))
      (apply-eighth-pivot neg-line-x sub-y len origo-yx)
      (pop (apply-eighth-pivot neg-line-y sub-x len origo-yx)))))

(defn trace-path-until [pred yxs]
  "Walks through the yxs vector of coordinates until either
  exhausting it or until pred returns true for a tile map. It
  will then return a vector of the elements up to and including
  that element."
  (reduce (fn [acc yx]
            (let [next-acc (conj acc yx)]
              (if (pred yx)
                (reduced next-acc)
                next-acc)))
          [] yxs))

(defn prune-paths [pred paths]
  "Uses trace-path-until to prune all paths according to predicate."
  (mapv (partial trace-path-until pred)
        paths))

(defn solid-yx? [area [y x]]
  "Checks whether a coordinate is a wall tile or out of bounds"
  (= \# (aget area y x)))

(defn line-of-sight [area len origo-yx]
  "Returns a set of coordinates that are within the line of sight
  of origo-yx with a range of len in area"
  (->> (complete-pivot len origo-yx)
       (prune-paths (partial solid-yx? area))
       (apply concat)
       set))


