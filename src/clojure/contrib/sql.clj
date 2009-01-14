;;  Copyright (c) Stephen C. Gilardi. All rights reserved.  The use and
;;  distribution terms for this software are covered by the Eclipse Public
;;  License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;;  be found in the file epl-v10.html at the root of this distribution.  By
;;  using this software in any fashion, you are agreeing to be bound by the
;;  terms of this license.  You must not remove this notice, or any other,
;;  from this software.
;;
;;  sql.clj
;;
;;  A Clojure interface to sql databases via jdbc
;;
;;  See clojure.contrib.sql.test for an example
;;
;;  scgilardi (gmail)
;;  Created 2 April 2008

(ns clojure.contrib.sql
  (:use [clojure.contrib.def :only (defvar)])
  (:use clojure.contrib.sql.internal))

(defvar connection connection*
  "Returns the current database connection (or throws if there is none)")

(defmacro with-connection
  "Evaluates body in the context of a new connection to a database then
  closes the connection. db-spec is a map containing string values for
  these required keys:
    :classname     the jdbc driver class name
    :subprotocol   the jdbc subprotocol
    :subname       the jdbc subname
  If db-spec contains additional keys (such as :user, :password, etc.) and
  associated values, they will be passed along to the driver as properties."
  [db-spec & body]
  `(with-connection* ~db-spec (fn [] ~@body)))

(defmacro transaction
  "Evaluates body as a transaction on the open database connection. Any
  nested transactions are absorbed into the outermost transaction. All
  database updates are committed together as a group after evaluating the
  outermost body, or rolled back on any uncaught exception."
  [& body]
  `(transaction* (fn [] ~@body)))

(defn do-commands
  "Executes SQL commands on the open database connection."
  [& commands]
  (with-open [stmt (.createStatement (connection))]
    (doseq [cmd commands]
      (.addBatch stmt cmd))
    (into [] (.executeBatch stmt))))

(defn do-prepared
  "Executes an (optionally parameterized) SQL prepared statement on the
  open database connection. Each param-group is a seq of values for all of
  the parameters."
  [sql & param-groups]
  (with-open [stmt (.prepareStatement (connection) sql)]
    (doseq [param-group param-groups]
      (doseq [[index value] (map vector (iterate inc 1) param-group)]
        (.setObject stmt index value))
      (.addBatch stmt))
    (into [] (.executeBatch stmt))))

(defn create-table
  "Creates a table on the open database connection given a table name and
  specs. Each spec is either a column spec: a vector containing a column
  name and optionally a type and other constraints, or a table-level
  constraint: a vector containing words that express the constraint. All
  words used to describe the table may be supplied as strings or keywords."
  [name & specs]
  (do-commands
   (format "CREATE TABLE %s (%s)"
           (the-str name)
           (apply str
             (map the-str
              (apply concat
               (interpose [", "]
                (map (partial interpose " ") specs))))))))

(defn drop-table
  "Drops a table on the open database connection given its name, a string
  or keyword"
  [name]
  (do-commands
   (format "DROP TABLE %s" (the-str name))))

(defn insert-values
  "Inserts rows into a table with values for specified columns only.
  column-names is a vector of strings or keywords identifying columns. Each
  value-group is a vector containing a values for each column in
  order. When inserting complete rows (all columns), consider using
  insert-rows instead."
  [table column-names & value-groups]
  (let [column-strs (map the-str column-names)
        n (count (first value-groups))
        template (apply str (interpose "," (replicate n "?")))
        columns (if (seq column-names)
                  (format "(%s)" (apply str (interpose "," column-strs)))
                  "")]
    (apply do-prepared
           (format "INSERT INTO %s %s VALUES (%s)"
                   (the-str table) columns template)
           value-groups)))

(defn insert-rows
  "Inserts complete rows into a table. Each row is a vector of values for
  each of the table's columns in order."
  [table & rows]
  (apply insert-values table nil rows))

(defn delete-rows
  "Deletes rows from a table. where-params is a vector containing a string
  providing the (optionally parameterized) selection criteria followed by
  values for any parameters."
  [table where-params]
  (let [[where & params] where-params]
    (do-prepared
     (format "DELETE FROM %s WHERE %s"
             (the-str table) where)
     params)))

(defn update-values
  "Updates values on selected rows in a table. where-params is a vector
  containing a string providing the (optionally parameterized) selection
  criteria followed by values for any parameters. record is a map from
  strings or keywords (identifying columns) to updated values."
  [table where-params record]
  (let [[where & params] where-params
        column-strs (map the-str (keys record))
        columns (apply str (concat (interpose "=?, " column-strs) "=?"))]
    (do-prepared
     (format "UPDATE %s SET %s WHERE %s"
             (the-str table) columns where)
     (concat (vals record) params))))

(defn update-or-insert-values
  "Updates values on selected rows in a table, or inserts a new row when no
  existing row matches the selection criteria. where-params is a vector
  containing a string providing the (optionally parameterized) selection
  criteria followed by values for any parameters. record is a map from
  strings or keywords (identifying columns) to updated values."
  [table where-params record]
  (transaction
   (let [result (update-values table where-params record)]
     (if (zero? (first result))
       (insert-values table (keys record) (vals record))
       result))))

(defmacro with-query-results
  "Executes a query, then evaluates body with results bound to a seq of the
  results. sql-params is a vector containing a string providing
  the (optionally parameterized) SQL query followed by values for any
  parameters."
  [results sql-params & body]
  `(with-query-results* ~sql-params (fn [~results] ~@body)))
