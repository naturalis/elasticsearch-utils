package org.col.es.query;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"filter", "aggs"})
public class FilterAggregation extends BucketAggregation {

  final Query filter;

  public FilterAggregation(Query filter) {
    this.filter = filter;
  }

}
