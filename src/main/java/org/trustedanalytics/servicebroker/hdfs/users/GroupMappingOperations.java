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

import java.util.UUID;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.servicebroker.hdfs.config.hgm.HgmConfiguration;
import org.trustedanalytics.servicebroker.hdfs.users.entity.User;

@Component
public class GroupMappingOperations {

  private static final String USER_GROUP_ENDPOINT = "/groups/{group}/users";
  private static final String TECH_GROUP_POSTFIX = "_sys";

  @Autowired
  private UaaUsersOperations uaaUsersOperations;

  @Autowired
  private HgmConfiguration configuration;

  @Autowired
  @Qualifier("hgmRestTemplate")
  private RestTemplate restTemplate;

  public UUID createSysUser(UUID groupId, UUID userId, String password) throws ServiceBrokerException {
    UUID uaaUserId = uaaUsersOperations.createUser(userId, password);
    try {
      restTemplate.postForObject(configuration.getUrl().concat(USER_GROUP_ENDPOINT), new User(
          uaaUserId.toString()), String.class, groupId.toString().concat(TECH_GROUP_POSTFIX));
      return uaaUserId;
    } catch (RestClientException e) {
      throw new ServiceBrokerException(String.format("Can't add user %s to group: %s",
          uaaUserId.toString(), groupId), e);
    }
  }

}
