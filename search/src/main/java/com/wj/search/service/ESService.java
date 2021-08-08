package com.wj.search.service;


import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @Date 2021/8/8 11:37
 * @Created by wj
 */
@Service
public class ESService {
	@Autowired
	private RestHighLevelClient restHighLevelClient;


	public String searchES(String keyword) throws IOException {
		Map<String, Object> ressult = new HashMap<>();
		SearchRequest searchRequest = new SearchRequest("shop");
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchQuery("name", keyword));
		searchRequest.source(sourceBuilder);

		SearchResponse res = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

		return "";
	}


	public String fixSearch(String keyword, double lat, double log) throws IOException {
		Request request = new Request("GET", "shop");
		String reqJson = "{\n" +
				"  \"_source\": [\n" +
				"    \"*\"\n" +
				"  ],\n" +
				"  \"script_fields\": {\n" +
				"    \"distance\": {\n" +
				"      \"script\": {\n" +
				"        \"source\": \"haversin(lat, lon, doc['location'].lat,doc['location'].lon)\",\n" +
				"        \"lang\": \"expression\",\n" +
				"        \"params\": {\n" +
				"          \"lat\": " + lat + "\n" +
				"          \"lon\": " + log + "\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  },\n" +
				"  \"query\": {\n" +
				"    \"function_score\": {\n" +
				"      \"query\": {\n" +
				"        \"bool\": {\n" +
				"          \"must\": [\n" +
				"            {\n" +
				"              \"match\": {\n" +
				"                \"name\": {\n" +
				"                  \"query\": \"" + keyword + "\"\n" +
				"                }\n" +
				"              }\n" +
				"            },\n" +
				"            {\n" +
				"              \"term\": {\n" +
				"                \"seller_distabled_flag\": 0\n" +
				"              }\n" +
				"            }\n" +
				"          ]\n" +
				"        }\n" +
				"      },\n" +
				"      \"functions\": [\n" +
				"        {\n" +
				"          \"gauss\": {\n" +
				"            \"location\": {\n" +
				"              \"origin\": \"" + lat + "," + log + "\",\n" +
				"              \"scale\": \"100km\",\n" +
				"              \"offset\": \"0km\",\n" +
				"              \"decay\": 0.5\n" +
				"            }\n" +
				"          },\n" +
				"          \"weight\": 9\n" +
				"      \n" +
				"        },\n" +
				"        {\n" +
				"          \"field_value_factor\": {\n" +
				"            \"field\": \"remark_score\"\n" +
				"          },\n" +
				"          \"weight\": 0.2\n" +
				"        },\n" +
				"        {\n" +
				"          \"field_value_factor\": {\n" +
				"            \"field\": \"seller_remark_score\"\n" +
				"          },\n" +
				"          \"weight\": 0.1\n" +
				"        }\n" +
				"      ],\n" +
				"      \"score_mode\": \"sum\",\n" +
				"      \"boost_mode\": \"sum\"\n" +
				"    }\n" +
				"  }\n" +
				"  , \"sort\": [\n" +
				"    {\n" +
				"      \"_score\": {\n" +
				"        \"order\": \"desc\"\n" +
				"      }\n" +
				"    }\n" +
				"  ]\n" +
				"}";

		request.setJsonEntity(reqJson);
		Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
		String responseJson = EntityUtils.toString(response.getEntity());


		return "";
	}


}
