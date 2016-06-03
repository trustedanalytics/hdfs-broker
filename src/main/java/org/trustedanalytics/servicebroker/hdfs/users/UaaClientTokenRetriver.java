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
package org.trustedanalytics.servicebroker.hdfs.users;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustedanalytics.servicebroker.hdfs.config.uaa.UaaConfiguration;
import org.trustedanalytics.servicebroker.hdfs.users.entity.UaaTokenResponse;

import com.github.rholder.retry.*;
import com.google.common.base.Predicates;

@Component
class UaaClientTokenRetriver {

  private static final Logger LOGGER = LoggerFactory.getLogger(UaaClientTokenRetriver.class);
  private static final String GRANT_TYPE = "grant_type";
  private static final String GRANT_TYPE_CREDENTIALS = "client_credentials";
  private static final String RESPONSE_TYPE = "response_type";
  private static final String RESPONSE_TYPE_TOKEN = "token";
  private static final String PARAMETERS = "parameters";

  private final UaaConfiguration uaaConfiguration;
  private final RestTemplate uaaRestTemplate;

  @Autowired
  public UaaClientTokenRetriver(UaaConfiguration configuration) {
    this.uaaConfiguration = configuration;
    this.uaaRestTemplate = createRestTemplate();
  }

  public String getToken() throws ServiceBrokerException {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    URI uaaUri =
        UriComponentsBuilder.fromHttpUrl(uaaConfiguration.getTokenUri())
            .queryParam(GRANT_TYPE, GRANT_TYPE_CREDENTIALS)
            .queryParam(RESPONSE_TYPE, RESPONSE_TYPE_TOKEN).build().encode().toUri();
    HttpEntity<String> entity = new HttpEntity<>(PARAMETERS, headers);

    Callable<UaaTokenResponse> callable = new Callable<UaaTokenResponse>() {
      @Override
      public UaaTokenResponse call() throws Exception {
        LOGGER.info("Trying to get uaa client token");
        return uaaRestTemplate.postForObject(uaaUri, entity, UaaTokenResponse.class);
      }
    };

    Retryer<UaaTokenResponse> retryer = RetryerBuilder.<UaaTokenResponse>newBuilder()
        .retryIfResult(Predicates.isNull())
        .retryIfExceptionOfType(IOException.class)
        .retryIfException()
        .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterAttempt(6))
        .build();

    try {
      return retryer.call(callable).getAccessToken();
    } catch (ExecutionException | RetryException e) {
      throw new ServiceBrokerException("Canno't login to Uaa as system cclient", e);
    }
  }

  private RestTemplate createRestTemplate() {
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
        uaaConfiguration.getClientId(), uaaConfiguration.getClientSecret()));
    HttpClient httpClient =
        HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

    return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
  }
}
