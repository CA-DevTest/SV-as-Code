/******************************************************************************
 *
 * Copyright (c) 2017 CA.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated
 * in any way except as authorized by the applicable license agreement,
 * without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT
 * WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN
 * NO EVENT WILL CA BE LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY
 * LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE USE OF THIS SOFTWARE,
 * INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR
 * DAMAGE.
 *
 ******************************************************************************/

package com.ca.svcode.example;

import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.contains;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forGet;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forPost;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.matchesJsonPath;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing usage of JSON payload.
 *
 * @author CA
 */
public class JsonExample {

  private static final String URL = "http://www.ca.com/portfolio";

  private static final String JSON_EXAMPLES_PORTFOLIO = "{\n"
      + "  \"productNamesList\": [\n"
      + "    \"CA Server Automation\",\n"
      + "    \"CA Service Catalog\",\n"
      + "    \"CA Service Desk Manager\",\n"
      + "    \"CA Service Management\",\n"
      + "    \"CA Service Operations Insight\",\n"
      + "    \"CA Service Virtualization\"\n"
      + "  ]\n"
      + "}";

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void testVirtualizeJsonResponse() throws IOException {
    forGet(URL).doReturn(
        okMessage()
            .withJsonBody(JSON_EXAMPLES_PORTFOLIO)
    );

    HttpGet httpGet = new HttpGet(URL);
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpGet);

    assertEquals(200, httpResponse.getStatusLine().getStatusCode());

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(httpResponse.getEntity().getContent()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = reader.readLine()) != null) {
      response.append(inputLine);
    }
    reader.close();

    Gson gson = new Gson();

    CaPortfolio portfolio = gson.fromJson(response.toString(), CaPortfolio.class);

    assertNotNull(portfolio);
    assertNotNull(portfolio.getProductNamesList());
    assertTrue(portfolio.getProductNamesList().size() == 6);
    assertTrue(portfolio.getProductNamesList().contains("CA Service Management"));
  }

  private static final String RESPONSE = "JSON received!";

  private static final String JSON_EXAMPLES_PORTFOLIO_2 = "{"
      + "\"portfolio\": {\n"
      + "   \"id\": \"1\",\n"
      + "   \"year\": \"2017\",\n"
      + "  \"productNamesList\": [\n"
      + "    \"CA Server Automation\",\n"
      + "    \"CA Service Catalog\",\n"
      + "    \"CA Service Desk Manager\",\n"
      + "    \"CA Service Management\",\n"
      + "    \"CA Service Operations Insight\",\n"
      + "    \"CA Service Virtualization\"\n"
      + "  ]\n"
      + "}}";


  @Test
  public void testJsonPath() throws Exception {
    forPost(URL)
        .matchesBodyPayload(matchesJsonPath("$.portfolio.id"))
        .matchesBodyPayload(matchesJsonPath("$.portfolio.year", "2017"))
        .matchesBodyPayload(matchesJsonPath("$.portfolio.year", contains("17")))
        .matchesBodyPayload(
            matchesJsonPath("$.portfolio.productNamesList", hasItem("CA Service Management")))
        .doReturn(
            okMessage().withStringBody(RESPONSE)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(URL);

    StringEntity params = new StringEntity(JSON_EXAMPLES_PORTFOLIO_2);
    request.addHeader("Content-Type", "application/json");
    request.setEntity(params);

    HttpResponse response = client.execute(request);

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.getEntity().getContent()));
    StringBuffer result = new StringBuffer();
    String line;
    while ((line = reader.readLine()) != null) {
      result.append(line);
    }

    String body = result.toString();

    assertEquals(200, response.getStatusLine().getStatusCode());
    assertNotNull(body);
    assertEquals(RESPONSE, body);
  }

  /**
   * POJO for JSON parsing in examples.
   *
   * @author CA
   */
  private class CaPortfolio {

    private List productNamesList;

    /**
     * Create instance with given product names.
     *
     * @param productNamesList product names
     */
    public CaPortfolio(List productNamesList) {
      this.productNamesList = productNamesList;
    }

    public List getProductNamesList() {
      return productNamesList;
    }

    public void setProductNamesList(List productNamesList) {
      this.productNamesList = productNamesList;
    }
  }

}
