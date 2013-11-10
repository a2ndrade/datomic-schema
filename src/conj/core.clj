;;;;;;;;;;;;;;;;;;;; basic API  ;;;;;;;;;;;;;;;;;;;;

;; require API and create alias
(require '[datomic.api :as d])

;; get a connection to the database
(def uri "datomic:mem://schemas")
(d/create-database uri)
(def conn (d/connect uri))

;; install attributes
(d/transact conn 
            [{:db/id #db/id[:db.part/db] 
              :db/ident :person/name
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}
             {:db/id #db/id[:db.part/db] 
              :db/ident :person/age
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

;; write (datoms) into the database. vector form
(d/transact conn
            [[:db/add #db/id[:db.part/user -1] :person/name "John"]
             [:db/add #db/id[:db.part/user -1] :person/age 25]])

;; write (datoms) into the database. map form
(d/transact conn
            [{:db/id #db/id[:db.part/user] :person/name "John" :person/age 25}])

;;;;;;;;;;;;;;;;;;;; datatype meta-model  ;;;;;;;;;;;;;;;;;;;;

(d/transact conn
            [{:db/id #db/id [:db.part/db]
              :db/ident :dt/dt
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/index true
              :db/doc "This entity's datatype"
              :db.install/_attribute :db.part/db}
             {:db/id #db/id [:db.part/db]
              :db/ident :dt/namespace
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/index true
              :db/doc "Datatype's namespace."
              :db.install/_attribute :db.part/db}
             {:db/id #db/id [:db.part/db]
              :db/ident :dt/name
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/index true
              :db/doc "Datatype's local name. Names within a given namespace are unique"
              :db.install/_attribute :db.part/db}
             {:db/id #db/id [:db.part/db]
              :db/ident :dt/parent
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/index true
              :db/doc "Datatype's parent datatype. Only populated in datatype hierarchies"
              :db.install/_attribute :db.part/db}
             {:db/id #db/id [:db.part/db]
              :db/ident :dt/list
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/index true
              :db/doc "Datatype's list datatype"
              :db.install/_attribute :db.part/db}
             {:db/id #db/id [:db.part/db]
              :db/ident :dt/component
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/index true
              :db/doc "Datatype's component datatype. Only populated for list datatypes"
              :db.install/_attribute :db.part/db}
             {:db/id #db/id [:db.part/db]
              :db/ident :dt/fields
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/index true
              :db/doc "Datatype's fields"
              :db.install/_attribute :db.part/db}])
(d/transact conn
            [{:db/id :dt/dt
              :dt/dt :dt/dt
              :dt/namespace "system"
              :dt/name "Datatype"
              :dt/fields [:dt/dt :dt/namespace :dt/name :dt/parent :dt/list :dt/component :dt/fields]
              :db/doc "The datatype for datatypes"}])


;; person datatype entity identifier. only created
;; to use its ident through the examples
(d/transact conn
            [{:db/id #db/id[:db.part/user]
              :db/ident :person.datatype/entity}])

;; datatype
(d/transact conn
            [{:db/id :person.datatype/entity
              :dt/dt :dt/dt
              :dt/namespace "customer"
              :dt/name "Person"
              :dt/fields [:dt/dt
                          :person/name
                          :person/age]}])

;; typed-entity
(d/transact conn
            [{:db/id #db/id[:db.part/user]
              :dt/dt :person.datatype/entity
              :person/name "John"
              :person/age 25}])

;;;;;;;;;;;;;;;;;;;; datatype hierarchies ;;;;;;;;;;;;;;;;;;;;

;; install :student/school
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :student/school
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

;; student datatype inherits the person datatype
(d/transact conn
           [{:db/id #db/id[:db.part/user]
             :dt/dt :dt/dt
             :dt/namespace "customer"
             :dt/name "Student"
             :dt/parent :person.datatype/entity
             :dt/fields [:student/school]}])

;;;;;;;;;;;;;;;;;;;; multi-dimensional lists ;;;;;;;;;;;;;;;;;;;;

;; install :dt.set/items
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :dt.set/items
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db.install/_attribute :db.part/db}])

;; create multi-dimensional person datatypes
(d/transact conn
            ;; e.g. Person[]
            [{:db/id #db/id[:db.part/user -1]
              :dt/dt :dt/dt
              :dt/component :person.datatype/entity
              :dt/_list :person.datatype/entity
              :dt/fields [:dt.set/items]}
             ;; e.g. Person[][]
             {:db/id #db/id [:db.part/user]
              :dt/dt :dt/dt
              :dt/component #db/id[:db.part/user -1]
              :dt/_list #db/id[:db.part/user -1]
              :dt/fields [:dt.set/items]}])

;;;;;;;;;;;;;;;;;;;; variant types  ;;;;;;;;;;;;;;;;;;;;o

;; install variant attributes
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :dt/any
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc "Built-in types enumeration"
              :db.install/_attribute :db.part/db}
             {:db/id #db/id[:db.part/db]
              :db/ident :dt.any/string
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}
             {:db/id #db/id[:db.part/db]
              :db/ident :dt.any/boolean
              :db/valueType :db.type/boolean
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}
             {:db/id #db/id[:db.part/db]
              :db/ident :dt.any/long
              :db/valueType :db.type/long
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}
             {:db/id #db/id[:db.part/db]
              :db/ident :dt.any/ref
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}
             ;; ... rest of built-in datomic types
             ])

;; any datatype
(d/transact conn
            [{:db/id :dt/any
              :dt/dt :dt/dt
              :dt/namespace "system"
              :dt/name "Any"
              :dt/fields [:dt/dt
                          :dt/any
                          :dt.any/string
                          :dt.any/boolean
                          :dt.any/long
                          ; ...
                          :dt.any/ref]}])


;; install :dt.meta/valueType
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :dt.meta/valueType
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])


;; install attribute of type "any"
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :person/metadata
              :db/valueType :db.type/ref
              :dt.meta/valueType :dt/any
              :db/cardinality :db.cardinality/many
              :db.install/_attribute :db.part/db}
             ])

;; add field to person datatype
(d/transact conn
            [{:db/id :person.datatype/entity
              :dt/fields [:person/metadata]}])

;;;;;;;;;;;;;;;;;;;; required attributes  ;;;;;;;;;;;;;;;;;;;;

;; install :dt.meta/valueType
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :dt.meta/required
              :db/valueType :db.type/boolean
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

;; install required attribute
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :health/insurance
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :dt.meta/required true
              :db.install/_attribute :db.part/db}])

;; install :dt.meta/migration attribute
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :dt.meta/migration
              :db/valueType :db.type/fn
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

;; attach a migration function, which sets a default value for the new required attribute
(d/transact conn
            [{:db/id :health/insurance
              :dt.meta/migration
              (d/function
               '{:lang :clojure
                 :params [db entity]
                 :code [[:db/add entity :health/insurance "PPACA"]]})}])

;; add field to person datatype
(d/transact conn
            [{:db/id :person.datatype/entity
              :dt/fields [:health/insurance]}])

;;;;;;;;;;;;;;;;;;;; data constraints  ;;;;;;;;;;;;;;;;;;;;

;; install :enum/ns attribute
(d/transact conn [{:db/id #db/id [:db.part/db]
                   :db/ident :enum/ns
                   :db/valueType :db.type/keyword
                   :db/cardinality :db.cardinality/one
                   :db/doc "Enum's namespace. Help enforce fk constraints on :db.type/ref enum references"
                   :db.install/_attribute :db.part/db}])

;; defining enumeration & enum values
(d/transact conn
            [{:db/id #db/id[:db.part/db]
              :db/ident :person/gender
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db
              ;; hint
              :enum/ns :person.gender}
             {:db/id #db/id[:db.part/user]
              :db/ident :person.gender/female}
             {:db/id #db/id[:db.part/user]
              :db/ident :person.gender/male}])


;; "Defines a database function (itself an entity) that enforces a foreign key constraint
;;  on :db.type/ref attributes that reference enum values. Those attributes must add a
;;  :enum/ns attribute holding the namespace of the enum idents, e.g:
;;
;;    [:add-fk person-id :person/gender :person.gender/male]
;;
;;  is equivalent to
;;
;;    [:db/add person-id :person/gender :person.gender/male]
;;
;; but with a foreign-key constraint enforced by the database."
(d/transact conn
            [{:db/id #db/id [:db.part/user]
              :db/ident :add-fk
              :db/fn (d/function
                      '{:lang :clojure
                        :params [db e a v]
                        :code (let [;; get value's symbolic keyword
                                    ident (if (keyword? v) v (d/ident db v))
                                    ;; get enum's constraint
                                    aent (d/entity db a)
                                    aname (:db/ident aent)
                                    enum-ns (:enum/ns aent)
                                    ;; find all possible enum idents
                                    allowed (if (nil? enum-ns)
                                              (throw (Exception. (str "Cannot check fk constraint. " aname " has no :enum/ns attribute")))
                                              (d/q '[:find ?ident
                                                     :in $ ?enum-ns
                                                     :where
                                                     [_ :db/ident ?ident]
                                                     [(namespace ?ident) ?ns]
                                                     [(= ?enum-ns ?ns)]]
                                                   db (name enum-ns)))]
                                ;; enforce constraint
                                (if (contains? allowed [ident])
                                  [[:db/add e a v]]
                                  (throw (Exception. (str v " is not one of " allowed)))))})}])

;; enforcing data constraint; transaction will fail
(d/transact conn [[:add-fk #db/id[:db.part/user] :person/gender :dt.meta/required]])
;; > java.lang.Exception: :dt.meta/required is not one of [[:person.gender/female], [:person.gender/male]]

;; enforcing data constraint; transaction will succeed
(d/transact conn [[:add-fk #db/id[:db.part/user] :person/gender :person.gender/male]])
