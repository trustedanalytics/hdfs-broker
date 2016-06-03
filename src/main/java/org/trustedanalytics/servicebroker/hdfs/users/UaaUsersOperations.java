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

import static java.util.Collections.singletonList;

import java.util.UUID;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.cloud.auth.HeaderAddingHttpInterceptor;
import org.trustedanalytics.cloud.uaa.UaaClient;
import org.trustedanalytics.servicebroker.hdfs.config.uaa.UaaConfiguration;

@Component
class UaaUsersOperations {

  private final UaaConfiguration uaaConfiguration;
  private final UaaClientTokenRetriver tokenRetriver;

  @Autowired
  public UaaUsersOperations(UaaClientTokenRetriver tokenRetriver, UaaConfiguration configuration) {
    this.tokenRetriver = tokenRetriver;
    this.uaaConfiguration = configuration;
  }

  public UUID createUser(UUID user, String password) throws ServiceBrokerException {
    UaaClient uaaClient = new UaaClient(createRestTemplate(), uaaConfiguration.getUri());
    uaaClient.createUser(user.toString(), password);
    return getUserId(user);
  }

  private UUID getUserId(UUID user) throws ServiceBrokerException {
    UaaClient uaaClient = new UaaClient(createRestTemplate(), uaaConfiguration.getUri());
    return uaaClient.findUserIdByName(user.toString())
        .orElseThrow(() -> new ServiceBrokerException("Can't create user " + user)).getGuid();
  }

  private RestTemplate createRestTemplate() throws ServiceBrokerException {
    ClientHttpRequestInterceptor interceptor =
        new HeaderAddingHttpInterceptor("Authorization", "bearer " + tokenRetriver.getToken());
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    RestTemplate restTemplate = new RestTemplate(factory);
    restTemplate.setInterceptors(singletonList(interceptor));
    return restTemplate;
  }
}
