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

import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forPost;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing possibility of using import functionality for matching requests.
 *
 * @author CA
 */
public class ImportExample {

  private static final String URL = "http://www.ca.com/portfolio";

  private static final String JSON_EXAMPLES_PORTFOLIO = "{"
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

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void testImportOfRequestDifferentWay() throws Exception {
    forPost(URL)
        .matchesBodyFromFile(getClass().getClassLoader()
            .getResource("requests/txt_request.txt").getPath())
        .matchesHeaderFromFile("requests/txt_request.txt", "CustomHeader")
        .matchesHeaderFromFile("requests/txt_request.txt", "Accept-Language")
        .doReturn(
            okMessage()
                .withJsonBody(JSON_EXAMPLES_PORTFOLIO)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(URL);
    /*
    Both header and values are the same as in referenced file
     */
    request.addHeader("Accept-Language", "zh-cn");
    request.addHeader("CustomHeader", "Header Value");
    request.setEntity(new StringEntity("Success"));
    HttpResponse response;

    response = client.execute(request);
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.getEntity().getContent()));
    StringBuffer result = new StringBuffer();
    String line;
    while ((line = reader.readLine()) != null) {
      result.append(line);
    }

    String body = result.toString().replaceAll("\\s+", "");

    assertEquals(200, response.getStatusLine().getStatusCode());
    assertNotNull(body);
    assertEquals(JSON_EXAMPLES_PORTFOLIO.replaceAll("\\s+", ""), body);
  }

  @Test
  public void testImportOfRequest() throws Exception {
    forPost(URL)
        .matchesBodyFromFile("requests/txt_request.txt")
        .matchesHeaderFromFile("requests/txt_request.txt", "CustomHeader")
        .matchesHeaderFromFile("requests/txt_request.txt", "Accept-Language")
        .doReturn(
            okMessage()
                .withJsonBody(JSON_EXAMPLES_PORTFOLIO)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(URL);
    /*
    Both header and values are the same as in referenced file
     */
    request.addHeader("Accept-Language", "zh-cn");
    request.addHeader("CustomHeader", "Header Value");
    request.setEntity(new StringEntity("Success"));

    HttpResponse response = client.execute(request);

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.getEntity().getContent()));
    StringBuffer result = new StringBuffer();
    String line;
    while ((line = reader.readLine()) != null) {
      result.append(line);
    }

    String body = result.toString().replaceAll("\\s+", "");

    assertEquals(200, response.getStatusLine().getStatusCode());
    assertNotNull(body);
    assertEquals(JSON_EXAMPLES_PORTFOLIO.replaceAll("\\s+", ""), body);
  }

}
