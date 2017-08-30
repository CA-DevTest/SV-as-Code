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

import static com.ca.svcode.api.matchers.GenericMatchers.matchesTemplate;
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
 * Example showing usage of template matcher.
 *
 * @author CA
 */
public class TemplateReferenceExample {

  private static final String BASE_URL = "http://www.ca.com/portfolio";

  private static final String TEMPLATE = "Hello, {computer}. Do you read me, {computer2}?;"
      + "Affirmative, {user}. I read you.;"
      + "Open the pod bay doors! {plead}!";

  private static final String TEXT = "Hello, HAL. Do you read me, HAL?;"
      + "Affirmative, Dave. I read you.;"
      + "Open the pod bay doors! PLEASE!";

  private static final String REFERENCE =
      "Hello, ${template.computer}. Do you read me, ${template.computer2}?;"
          + "Affirmative, ${template.user}. I read you.;"
          + "Open the pod bay doors! ${template.plead}!";

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void testTemplateMagicString() throws Exception {
    forPost(BASE_URL)
        .matchesHeader("Language", matchesTemplate("Language of Text: {language}"))
        .matchesBody(matchesTemplate(TEMPLATE))
        .doReturn(
            okMessage()
                .withStringBody(REFERENCE)
                .withHeader("Language", "${template.language}")
                // By default magic strings are disabled
                .enableMagicStrings()
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(BASE_URL);

    // 2 headers with the same name and different values
    request.addHeader("Language", "Language of Text: en_us");
    request.setEntity(new StringEntity(TEXT));

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
    assertEquals(TEXT, body);
    assertEquals("en_us", response.getFirstHeader("Language").getValue());
  }
}
