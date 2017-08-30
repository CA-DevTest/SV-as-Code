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

import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.matchesJsonPath;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import com.ca.svcode.protocols.http.fluent.HttpFluentInterface;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing working with encodings.
 *
 * @author CA
 */
public class EncodingExample {

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
  public void testEncodings() throws IOException {
    HttpFluentInterface.forPost(URL)
        .matchesHeader("Content-Encoding", "deflate")
        .matchesBodyPayload(matchesJsonPath("$.portfolio.id"))
        .doReturn(
            okMessage()
                .withHeader("Content-Encoding", "gzip")
                .withBody(JSON_EXAMPLES_PORTFOLIO.getBytes())
        );

    // We need to disable automatic compression/decompression on client
    HttpClient client = HttpClientBuilder.create().disableContentCompression().build();
    HttpPost request = new HttpPost(URL);

    request.setEntity(new ByteArrayEntity(compressPayload(JSON_EXAMPLES_PORTFOLIO)));
    request.setHeader("Content-Type", "application/json");
    request.setHeader("Content-Encoding", "deflate");

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
    assertNotEquals(body, JSON_EXAMPLES_PORTFOLIO);

    // We will execute it one more time and now we will decompress the entity
    response = client.execute(request);

    body = decompressPayload(response);

    assertEquals(JSON_EXAMPLES_PORTFOLIO, body);
  }

  private byte[] compressPayload(String text) throws IOException {
    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
    DeflaterOutputStream deflaterOutputStream = null;
    try {
      deflaterOutputStream = new DeflaterOutputStream(byteOutputStream);
      deflaterOutputStream.write(text.getBytes());
      deflaterOutputStream.finish();
      return byteOutputStream.toByteArray();
    } finally {
      deflaterOutputStream.close();
      byteOutputStream.close();
    }
  }

  private String decompressPayload(HttpResponse response) throws IOException {
    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
    InflaterInputStream inflaterInputStream = new GZIPInputStream(
        response.getEntity().getContent());
    try {
      IOUtils.copy(inflaterInputStream, byteOutputStream);
      return new String(byteOutputStream.toByteArray());
    } finally {
      byteOutputStream.close();
      inflaterInputStream.close();
    }
  }

}
