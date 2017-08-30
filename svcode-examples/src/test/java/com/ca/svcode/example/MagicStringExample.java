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
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.isEqualIgnoringCaseTo;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.is;
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
 * Example showing magic strings possibilities.
 *
 * @author CA
 */
public class MagicStringExample {

  private static final String BASE_URL = "http://www.ca.com/portfolio";
  private static final String QUERY = "?year=2016&tokenQuery=X4sPhj15WQE";

  // This will be create this URL- > http://www.ca.com/portfolio/{id}?year=2016&tokenQuery=X4sPhj15WQE
  private static final String URL = BASE_URL + "/{id}" + QUERY;

  private static final String JSON_EXAMPLES_PORTFOLIO = "{"
      + "\"portfolio\": {\n"
      + "   \"id\": \"${attribute.id}\",\n"
      + "   \"year\": \"${argument.year}\",\n"
      + "   \"language\": \"${metadata.Language}\",\n"
      + "   \"productNamesList\": [\n"
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
  public void testMagicString() throws Exception {
    forGet(URL)
        .matchesHeader("Language", is("en_us"))
        .matchesQuery("tokenQuery", isEqualIgnoringCaseTo("x4sphj15wqe"))
        .matchesQuery("year", "2016")
        .doReturn(
            okMessage()
                .withJsonBody(JSON_EXAMPLES_PORTFOLIO)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(BASE_URL + "/1" + QUERY);

    // 2 headers with the same name and different values
    request.addHeader("Language", "en_us");
    request.addHeader("Language", "en_uk");

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

    assertTrue(body.contains("\"year\":\"2016\""));
    assertFalse(body.contains("${argument.year}"));

    assertTrue(body.contains("\"id\":\"1\""));
    assertFalse(body.contains("${attribute.id}"));

    // Only the first header value will be replace
    assertTrue(body.contains("\"language\":\"en_us\""));
    assertFalse(body.contains("${metadata.Language}"));
  }
}
