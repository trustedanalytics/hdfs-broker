/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.servicebroker.hdfs.config.hgm;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.servicebroker.hdfs.config.hgm.HgmConfiguration;

@Profile("!kerberos")
@Configuration
public class HgmHttpsConfiguration {

  @Autowired
  private HgmConfiguration configuration;

  @Bean
  @Qualifier("hgmRestTemplate")
  public RestTemplate getHgmHttpsRestTemplate() throws KeyStoreException, NoSuchAlgorithmException,
      KeyManagementException {
    SSLContext sslContext =
        SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useTLS()
            .build();
    SSLConnectionSocketFactory connectionFactory =
        new SSLConnectionSocketFactory(sslContext, new AllowAllHostnameVerifier());
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(configuration.getUsername(),
        configuration.getPassword()));

    HttpClient httpClient =
        HttpClientBuilder.create().setSSLSocketFactory(connectionFactory)
            .setDefaultCredentialsProvider(credentialsProvider).build();

    ClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);
    return new RestTemplate(requestFactory);
  }
}
