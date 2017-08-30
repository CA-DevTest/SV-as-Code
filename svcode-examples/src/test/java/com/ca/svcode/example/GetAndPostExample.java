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

import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.aMessage;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forGet;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forPost;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import com.ca.svcode.protocols.http.HttpConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing basic usage of SVasCode.
 *
 * @author CA
 */
public class GetAndPostExample {

  private static final String URL = "http://www.ca.com/portfolio";

  private static String RESPONSE_BODY_GET = "Response body from virtualized service.";
  private static String RESPONSE_BODY_POST =
      "Response for id ${argument.id} with configured filter for ${argument.filter}.";
  private static int CUSTOM_STATUS_CODE = 258;

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void testSimpleHttpGetWithResponseCodeAndStringBody() throws IOException {
    forGet(URL).doReturn(
        aMessage(CUSTOM_STATUS_CODE)
            .withStringBody(RESPONSE_BODY_GET)
    );

    HttpGet httpGet = new HttpGet(URL);
    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpGet);

    assertEquals(CUSTOM_STATUS_CODE, httpResponse.getStatusLine().getStatusCode());

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(httpResponse.getEntity().getContent()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = reader.readLine()) != null) {
      response.append(inputLine);
    }
    reader.close();

    assertEquals(response.toString(), RESPONSE_BODY_GET);
  }

  @Test
  public void testSimpleHttpPost() throws IOException {
    forPost(URL).doReturn(
        okMessage()
            .withStringBody(RESPONSE_BODY_POST)
            .withContentType(HttpConstants.PLAIN_TEXT)
            .enableMagicStrings()
    );

    HttpPost httpPost = new HttpPost(URL);
    List parameters = new ArrayList(2);
    parameters.add(new BasicNameValuePair("id", "5"));
    parameters.add(new BasicNameValuePair("filter", "ALL"));
    httpPost.setEntity(new UrlEncodedFormEntity(parameters));

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse httpResponse = httpClient.execute(httpPost);

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(httpResponse.getEntity().getContent()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = reader.readLine()) != null) {
      response.append(inputLine);
    }
    reader.close();

    assertTrue(response.toString().contains("Response for id 5"));
    assertTrue(response.toString().contains("filter for ALL."));
  }
}


