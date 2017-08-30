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
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.matchesXPath;
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.okMessage;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import com.ca.svcode.protocols.http.fluent.HttpFluentInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Example showing usage of XML payload.
 *
 * @author CA
 */
public class XmlExample {

  private static final String URL = "http://www.ca.com/portfolio/xml";

  private static final String XML_BODY = "<portfolio>\n"
      + "    <id>1</id>\n"
      + "    <year>2016</year>\n"
      + "    <productNameList>\n"
      + "      <name>CA Server Automation</name>\n"
      + "      <name>CA Service Catalog</name>\n"
      + "      <name>CA Service Desk Manager</name>\n"
      + "      <name>CA Service Management</name>\n"
      + "      <name>CA Service Operations Insight</name>\n"
      + "      <name>CA Service Virtualization</name>\n"
      + "    </productNameList>\n"
      + "  </portfolio>";

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void testVirtualizedXmlBody() throws IOException, ParserConfigurationException, SAXException {
    HttpFluentInterface.forGet(URL)
        .doReturn(
            okMessage()
                .withXmlBody(XML_BODY)
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(URL);

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
    assertEquals(XML_BODY.replaceAll("\\s+", ""), body);

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

    InputSource is = new InputSource(new StringReader(XML_BODY));
    Document doc = dBuilder.parse(is);

    doc.getDocumentElement().normalize();

    assertEquals("portfolio", doc.getDocumentElement().getNodeName());

  }

  @Test
  public void testXmlPath() throws Exception {
    HttpFluentInterface.forPost(URL)
        .matchesBodyPayload(matchesXPath("/portfolio"))
        .matchesBodyPayload(matchesXPath("/portfolio/year", contains("16")))
        .matchesBodyPayload(matchesXPath("/portfolio/id", "1"))
        .doReturn(
            okMessage()
        );

    HttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(URL);

    request.setEntity(new StringEntity(XML_BODY));

    HttpResponse response;
    response = client.execute(request);

    assertEquals(200, response.getStatusLine().getStatusCode());
  }
}
