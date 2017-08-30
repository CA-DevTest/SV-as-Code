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
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forAnyRequest;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.forGet;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.unauthorizedMessage;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing basic authorization.
 *
 * @author CA
 */
public class BasicAuthExample {

  private static final String URL = "http://www.ca.com/portfolio";

  private static final String BODY = "Success";

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();


  @Test
  public void testBasicAuth() throws Exception {
    // Our service with basic authorization
    forGet(URL)
        .matchesBasicAuthorization("commonUsername", "bestPasswordEver")
        .matchesBasicAuthorization(contains("common"), is("bestPasswordEver"))
        .doReturn(
            okMessage()
                .withStringBody(BODY)
        );

    // Simulate 401 with Authentication header
    forAnyRequest(URL)
        .doReturn(
            unauthorizedMessage()
                .withHeader("WWW-Authenticate", "Basic")
        );

    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials
        = new UsernamePasswordCredentials("commonUsername", "bestPasswordEver");
    provider.setCredentials(AuthScope.ANY, credentials);

    HttpClient client = HttpClientBuilder.create()
        .setDefaultCredentialsProvider(provider)
        .build();

    HttpGet request = new HttpGet(URL);
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
    assertEquals(BODY, body);
  }
}
