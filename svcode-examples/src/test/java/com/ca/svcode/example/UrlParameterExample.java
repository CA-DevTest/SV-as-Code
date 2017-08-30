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

import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forGet;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing of using URL parameters.
 *
 * @author CA
 */
public class UrlParameterExample {

  private static final String URL = "http://www.ca.com/portfolio/{year}";

  private static final String JSON_EXAMPLES_PORTFOLIO = "{"
      + "\"portfolio\": {\n"
      + "   \"id\": \"1\",\n"
      + "   \"year\": \"${attribute.year}\",\n"
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
  public void testAdvancedHttpUsage() throws Exception {
    forGet(URL)
        .doReturn(
            okMessage()
                .withJsonBody(JSON_EXAMPLES_PORTFOLIO)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet("http://www.ca.com/portfolio/2017");
    HttpResponse response;

    response = client.execute(request);

    BufferedReader rd = new BufferedReader(
        new InputStreamReader(response.getEntity().getContent()));
    StringBuffer result = new StringBuffer();
    String line;
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }

    String body = result.toString();

    assertEquals(200, response.getStatusLine().getStatusCode());
    assertNotNull(body);
    assertTrue(body.contains("2017"));
    assertFalse(body.contains("${attribute.year}"));
  }
}
