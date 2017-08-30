
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
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.withSecureProtocol;
import static org.junit.Assert.assertEquals;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import com.ca.svcode.protocols.http.HttpConstants;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Example showing HTTPs support.
 *
 * @author CA
 */
public class HttpsExample {

  private static final String KEYSTORE_PATH = HttpsExample.class.getClassLoader()
      .getResource("ssl/keystore.jks").getPath();
  private static final String KEYSTORE_PASSWORD = "password";
  private static final String BODY_PLAIN_TEXT = "Success";
  private static final String BASE_URL_SSL = "https://localhost:8090/";
  private static final int HTTP_STATUS_OK_NUMB = 200;

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  private TrustManager[] trustAllCerts;
  private SSLContext sslContext;

  /**
   * Preparing to trust everything with common name localhost.
   */
  @Before
  public void setUp() throws Exception {
    // using own trust manager
    trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
          }

          public void checkClientTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
            if (certs.length != 1 || !certs[0].getIssuerX500Principal().getName()
                .contains("CN=localhost")) {
              throw new SecurityException("Invalid certificate");
            }
          }
        }
    };

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, trustAllCerts, new SecureRandom());
  }

  @Test(timeout = 10000L)
  public void testHttpsConnection() throws IOException {
    forGet(BASE_URL_SSL)
        .usingHttps(
            withSecureProtocol("TLS")
                .keystorePath(KEYSTORE_PATH)
                .keystorePassword(KEYSTORE_PASSWORD)
                .keyPassword(KEYSTORE_PASSWORD))
        .doReturn(
            okMessage()
                .withStringBody(BODY_PLAIN_TEXT)
                .withContentType(HttpConstants.PLAIN_TEXT));

    URL u = new URL(BASE_URL_SSL);
    HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
    connection.setSSLSocketFactory(sslContext.getSocketFactory());
    assertEquals(HTTP_STATUS_OK_NUMB, connection.getResponseCode());
    assertEquals(BODY_PLAIN_TEXT,
        IOUtils.toString((InputStream) connection.getContent()));
  }

}
