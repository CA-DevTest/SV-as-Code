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
 * Example showing usage of Java 8 features.
 *
 * @author CA
 */
public class LambdaExample {

  private static final String URL = "http://www.ca.com/portfolio?year=2016&tokenQuery=X4sPhj15WQE";

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();


  @Test
  public void testLambda() throws Exception {
    forGet(URL)
        .matchesHeader("Custom-Header", s -> s.equals("CustomValue"))
        .doReturn(
            okMessage()
                .withJsonBody("Success"));

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(URL);
    request.addHeader("Custom-Header", "CustomValue");

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
  }
}
