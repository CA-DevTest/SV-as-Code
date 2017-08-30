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

import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.verifyGet;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import com.ca.svcode.protocols.http.fluent.HttpFluentInterface;
import java.io.IOException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing post verifications of requests.
 *
 * @author CA
 */
public class PostVerificationExample {

  private static final String BASE_URL = "http://www.ca.com/portfolio";
  private static final String QUERY = "?year=2016";
  private static final String URL = BASE_URL + "/{id}" + QUERY;


  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void testPostVerification() throws IOException {
    HttpFluentInterface.forGet(URL)
        .doReturn(
            okMessage()
                .withStringBody("Success")
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request1 = new HttpGet(BASE_URL + "/1" + QUERY);
    client.execute(request1);

    HttpClient client2 = HttpClientBuilder.create().build();
    HttpGet request2 = new HttpGet(BASE_URL + "/2" + QUERY);
    client2.execute(request2);
    client2.execute(request2);

    verifyGet(URL).invoked(3);
    verifyGet(BASE_URL + "/1").invoked(1);
    verifyGet(BASE_URL + "/2").invoked(2);
    verifyGet(URL).matchesQuery("year", "2016").invoked(3);
  }
}
