package org.col.es.query;

/**
 * Represents the "query" part of an Elasticsearch search quest. Since queries can take very different forms, the only aspects of a query
 * this interface defines are its name and its boost value.
 */
public interface Query {

  /**
   * Turns the query into a named query. Fluent interface (returns the instance on which it is called). Named queries are especially useful
   * with compound queries like {@link BoolQuery}: if you name the constraints within a <code>BoolQuery</code>, Elasticsearch will tell you
   * for each document in the query result which constraints it satisfied.
   * 
   * @param name
   * @return
   */
  <Q extends Query> Q withName(String name);

  /**
   * Sets a boost value for documents matching this query. Fluent interface (returns the instance on which it is called).
   * 
   * @param boost
   * @return
   */
  <Q extends Query> Q withBoost(Double boost);

}
