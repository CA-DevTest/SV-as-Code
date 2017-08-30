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

import com.ca.svcode.engine.junit4.VirtualServerRule;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing magic dates possibilities of generating magic date based on current date/time.
 *
 * @author CA
 */
public class CurrentMagicDateExample {

  private static final String URL = "http://www.ca.com/portfolio/";

  private static final String JSON_EXAMPLES_PORTFOLIO_CURRENT_DATE = "{"
      + "\"portfolio\": {\n"
      + "   \"id\": \"1\",\n"
      + "   \"dateOfRequest\": \"${currentDate:+7d:\"yyyy-MM-dd\"}\",\n"
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
  public void testCurrentDateMagicDate() throws Exception {
    forGet(URL)
        .doReturn(
            okMessage()
                // Format current date to ISO_LOCAL_DATE_TIME format (yyyy-MM-dd)
                .withHeader("lastRequest", "${currentDate:ISO_LOCAL_DATE_TIME}")
                // Create current date with offset minus 1 week and format it to default format (yyyy-MM-dd'T'HH:mm:ss.sssX)
                .withHeader("lastModification", "${currentDate:-1w}")
                // Create current date with offset minus 1 hour and format it to ISO_OFFSET_DATE_TIME format (yyyy-MM-dd'T'HH:mm:ssXXX)
                .withHeader("dateOfCreation", "${currentDate:-1h:ISO_OFFSET_DATE_TIME}")
                .withJsonBody(JSON_EXAMPLES_PORTFOLIO_CURRENT_DATE)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(URL);
    request.addHeader("Language", "en_us");

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

    LocalDateTime nextWeek = LocalDateTime.now().plusDays(7);
    assertTrue(body.contains(nextWeek.format(DateTimeFormatter.ISO_DATE)));

    // Asserting time of the creation of the response is impossible, just check different formats and offsets
    System.out.println(
        "${currentDate:ISO_LOCAL_DATE_TIME} generated " + response.getFirstHeader("lastRequest")
            .getValue());
    System.out
        .println("${currentDate:-1w} generated " + response.getFirstHeader("lastModification")
            .getValue());
    System.out.println("${currentDate:-1h:ISO_OFFSET_DATE_TIME} generated " + response
        .getFirstHeader("dateOfCreation").getValue());
  }
}
