package org.col.es.query;

import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonValue;

import static org.col.es.query.SortOptions.Order.ASC;
import static org.col.es.query.SortOptions.Order.DESC;

public class SortField {

  public static final SortField DOC = new SortField("_doc");
  public static final SortField SCORE = new SortField("_score");

  final String field;
  final SortOptions options;

  public SortField(String field) {
    this(field, null);
  }

  public SortField(String field, boolean ascending) {
    this(field, new SortOptions(ascending ? ASC : DESC));
  }

  public SortField(String field, SortOptions options) {
    this.field = field;
    this.options = options;
  }

  /*
   * The ES query DSL allows you to either provide a simple string being the field you want to sort on, or the full-blown
   * object modeled by this class. In case of sorting on score ("_score") or index order ("_doc"), you MUST use a simple
   * string. Hence this jsonValue method.
   */
  @JsonValue
  public Object jsonValue() {
    return options == null ? field : Collections.singletonMap(field, options);
  }

}
