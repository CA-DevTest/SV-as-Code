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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing magic dates possibilities of generating magic date based on information from
 * request.
 *
 * @author CA
 */
public class SourceMagicDateExample {

  private static final String JSON_EXAMPLES_PORTFOLIO_SOURCE_DATE = "{"
      + "\"portfolio\": {\n"
      + "   \"id\": \"1\",\n"
      + "   \"dateOfRequest\": \"${metadata.time:\"HH:mm:ss\":+4h:ISO_LOCAL_TIME}\",\n"
      + "   \"productNamesList\": [\n"
      + "    \"CA Server Automation\",\n"
      + "    \"CA Service Catalog\",\n"
      + "    \"CA Service Desk Manager\",\n"
      + "    \"CA Service Management\",\n"
      + "    \"CA Service Operations Insight\",\n"
      + "    \"CA Service Virtualization\"\n"
      + "  ]\n"
      + "}}";

  private static final String URL = "http://www.ca.com/portfolio/{date}?requestTime=16%3A00%3A01";

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void testSourceMagicDate() throws Exception {
    forGet(URL)
        .doReturn(
            okMessage()
                // Reference a date/time from arguments of the request (query) and format it to default format (yyyy-MM-dd'T'HH:mm:ss.sssX)
                .withHeader("requestTime", "${argument.requestTime:ISO_LOCAL_TIME}")
                // Reference a date/time from attributes of the request (URL parameters), apply offset minut 2 weeks and format it to default format (yyyy-MM-dd'T'HH:mm:ss.sssX)
                .withHeader("lastModification", "${attribute.date:\"yyyyMMdd\":-2w}")
                // Reference a date/time from metadata of the request (headers) and format it to ISO_OFFSET_DATE_TIME format (yyyy-MM-dd'T'HH:mm:ssXXX)
                .withHeader("dateOfCreation",
                    "${metadata.currentDate:\"yyyy-MM-dd'T'HH:mm:ss\":ISO_OFFSET_DATE_TIME}")
                .withJsonBody(JSON_EXAMPLES_PORTFOLIO_SOURCE_DATE)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet("http://www.ca.com/portfolio/20170814?requestTime=16%3A00%3A01");
    request.addHeader("time", "15:30:49");
    request.addHeader("currentDate", "2017-08-14T14:50:10");
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
    assertTrue(body.contains("19:30:49"));

    // Outputs are dependant on your timezone offset so asserting created values is pointless
    System.out.println(
        "${argument.requestTime:ISO_LOCAL_TIME} generated " + response.getFirstHeader("requestTime")
            .getValue());
    System.out.println("${attribute.date:\"yyyyMMdd\":-2w} generated " + response
        .getFirstHeader("lastModification").getValue());
    System.out.println(
        "${metadata.currentDate:\"yyyy-MM-dd'T'HH:mm:ss\":ISO_OFFSET_DATE_TIME} generated "
            + response.getFirstHeader("dateOfCreation").getValue());
  }


  @Test
  public void testDisableMagicStrings() throws Exception {
    forGet(URL)
        .doReturn(
            okMessage()
                // Reference a date/time from arguments of the request (query) and format it to default format (yyyy-MM-dd'T'HH:mm:ss.sssX)
                .withHeader("requestTime", "${argument.requestTime:ISO_LOCAL_TIME}")
                // Reference a date/time from attributes of the request (URL parameters), apply offset minut 2 weeks and format it to default format (yyyy-MM-dd'T'HH:mm:ss.sssX)
                .withHeader("lastModification", "${attribute.date:\"yyyyMMdd\":-2w}")
                // Reference a date/time from metadata of the request (headers) and format it to ISO_OFFSET_DATE_TIME format (yyyy-MM-dd'T'HH:mm:ssXXX)
                .withHeader("dateOfCreation",
                    "${metadata.currentDate:\"yyyy-MM-dd'T'HH:mm:ss\":ISO_OFFSET_DATE_TIME}")
                .withJsonBody(JSON_EXAMPLES_PORTFOLIO_SOURCE_DATE)
                .disableMagicDates()
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet("http://www.ca.com/portfolio/20170814?requestTime=16%3A00%3A01");
    request.addHeader("time", "15:30:49");
    request.addHeader("currentDate", "2017-08-14T14:50:10");

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

    //Remove whitespaces and formatting
    assertEquals(JSON_EXAMPLES_PORTFOLIO_SOURCE_DATE.replaceAll("\\s+", ""), body);
  }
}
