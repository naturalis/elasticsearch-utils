package org.col.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.col.es.query.BoolQuery;
import org.col.es.query.EsSearchRequest;
import org.col.es.query.MatchAllQuery;
import org.col.es.query.Query;
import org.col.es.query.TermQuery;
import org.col.es.query.TermsQuery;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

public class EsUtil {

  /**
   * Deletes the index with the provided name. Will silently do nothing if the index did not exist.
   * 
   * @param client
   * @param name
   * @throws IOException
   */
  public static void deleteIndex(RestClient client, String name) throws IOException {
    Request request = new Request("DELETE", name);
    Response response = null;
    try {
      response = client.performRequest(request);
    } catch (ResponseException e) {
      if (e.getResponse().getStatusLine().getStatusCode() == 404) { // That's OK
        return;
      }
    }
    if (response.getStatusLine().getStatusCode() >= 400) {
      throw new EsException(response.getStatusLine().getReasonPhrase());
    }
  }

  /**
   * Whether or not an index with the provided name exists.
   * 
   * @param client
   * @param index
   * @return
   * @throws IOException
   */
  public static boolean indexExists(RestClient client, String index) throws IOException {
    Request request = new Request("HEAD", index);
    Response response = client.performRequest(request);
    return response.getStatusLine().getStatusCode() == 200;
  }

  /**
   * Removes the dataset corresponding to the provided key. You must still refresh the index for the changes to become visible.
   * 
   * @param client
   * @param index
   * @param datasetKey
   * @return
   * @throws IOException
   */
  public static int deleteDataset(RestClient client, String index, int datasetKey) throws IOException {
    return deleteByQuery(client, index, new TermQuery("datasetKey", datasetKey));
  }

  /**
   * Removes the sector corresponding to the provided key. You must still refresh the index for the changes to become visible.
   * 
   * @param client
   * @param index
   * @param sectorKey
   * @return
   * @throws IOException
   */
  public static int deleteSector(RestClient client, String index, int sectorKey) throws IOException {
    return deleteByQuery(client, index, new TermQuery("sectorKey", sectorKey));
  }

  /**
   * Delete the documents corresponding to the provided dataset key and usage IDs. Returns the number of documents actually deleted. You
   * must still refresh the index for the changes to become visible.
   */
  public static int deleteNameUsages(RestClient client, String index, int datasetKey, Collection<String> usageIds) throws IOException {
    if (usageIds.isEmpty()) {
      return 0;
    }
    List<String> ids = (usageIds instanceof List) ? (List<String>) usageIds : new ArrayList<>(usageIds);
    int from = 0;
    int deleted = 0;
    while (from < ids.size()) {
      int to = Math.min(ids.size(), from + 1024); // 1024 is max num terms in terms query
      BoolQuery query = new BoolQuery()
          .filter(new TermQuery("datasetKey", datasetKey))
          .filter(new TermsQuery("usageId", ids.subList(from, to)));
      deleted += deleteByQuery(client, index, query);
      from = to;
    }
    return deleted;
  }

  /**
   * Deletes all documents from the index, but leaves the index itself intact. Very impractical for production code, but nice for testing
   * code.
   * 
   * @param client
   * @param index
   * @throws IOException
   */
  public static void truncate(RestClient client, String index) throws IOException {
    deleteByQuery(client, index, new MatchAllQuery());
  }

  /**
   * Deletes all documents satisfying the provided query constraint(s). You must still refresh the index for the changes to become visible.
   * 
   * @param client
   * @param index
   * @param query
   * @return
   * @throws IOException
   */
  public static int deleteByQuery(RestClient client, String index, Query query) throws IOException {
    Request request = new Request("POST", index + "/_delete_by_query");
    EsSearchRequest esRequest = new EsSearchRequest();
    esRequest.setQuery(query);
    request.setJsonEntity(esRequest.toString());
    Response response = executeRequest(client, request);
    return readFromResponse(response, "total");
  }

  /**
   * Makes all index documents become visible to clients.
   * 
   * @param client
   * @param name
   * @throws IOException
   */
  public static void refreshIndex(RestClient client, String name) throws IOException {
    Request request = new Request("POST", name + "/_refresh");
    executeRequest(client, request);
  }

  /**
   * Simple document count.
   * 
   * @param client
   * @param indexName
   * @return
   * @throws IOException
   */
  public static int count(RestClient client, String indexName) throws IOException {
    Request request = new Request("GET", indexName + "/_count");
    Response response = executeRequest(client, request);
    try {
      return (Integer) EsModule.readIntoMap(response.getEntity().getContent()).get("count");
    } catch (UnsupportedOperationException | IOException e) {
      throw new EsException(e);
    }
  }

  /**
   * Executes the provided HTTP request and returns the HTTP response.
   * 
   * @param client
   * @param request
   * @return
   * @throws IOException
   */
  public static Response executeRequest(RestClient client, Request request) throws IOException {
    Response response = client.performRequest(request);
    if (response.getStatusLine().getStatusCode() >= 400) {
      throw new EsException(response.getStatusLine().getReasonPhrase());
    }
    return response;
  }

  @SuppressWarnings("unchecked")
  public static <T> T readFromResponse(Response response, String property) {
    try {
      return (T) EsModule.readIntoMap(response.getEntity().getContent()).get(property);
    } catch (UnsupportedOperationException | IOException e) {
      throw new EsException(e);
    }
  }

}
