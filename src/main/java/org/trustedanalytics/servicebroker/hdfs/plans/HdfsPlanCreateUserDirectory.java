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
package org.trustedanalytics.servicebroker.hdfs.plans;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trustedanalytics.servicebroker.framework.service.ServicePlanDefinition;
import org.trustedanalytics.servicebroker.framework.store.CredentialsStore;
import org.trustedanalytics.servicebroker.hdfs.plans.binding.HdfsSpecificOrgBindingOperations;
import org.trustedanalytics.servicebroker.hdfs.plans.provisioning.HdfsDirectoryProvisioningOperations;
import org.trustedanalytics.servicebroker.hdfs.users.GroupMappingOperations;

import com.google.common.collect.ImmutableMap;

@Component("create-user-directory")
class HdfsPlanCreateUserDirectory implements ServicePlanDefinition {

  private static final String USER = "user";
  private static final String PASSWORD = "password";

  private final HdfsDirectoryProvisioningOperations hdfsOperations;
  private final HdfsSpecificOrgBindingOperations bindingOperations;
  private final CredentialsStore credentialsStore;
  private final GroupMappingOperations groupMappingOperations;

  @Autowired
  public HdfsPlanCreateUserDirectory(HdfsDirectoryProvisioningOperations hdfsOperations,
      HdfsSpecificOrgBindingOperations bindingOperations,
      GroupMappingOperations groupMappingOperations, CredentialsStore zookeeperCredentialsStore) {
    this.hdfsOperations = hdfsOperations;
    this.bindingOperations = bindingOperations;
    this.credentialsStore = zookeeperCredentialsStore;
    this.groupMappingOperations = groupMappingOperations;
  }

  @Override
  public void provision(ServiceInstance serviceInstance, Optional<Map<String, Object>> parameters)
      throws ServiceInstanceExistsException, ServiceBrokerException {
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    UUID orgId = UUID.fromString(serviceInstance.getOrganizationGuid());
    String password = RandomStringUtils.randomAlphanumeric(32);

    UUID sysUser = groupMappingOperations.createSysUser(orgId, instanceId, password);
    String path = hdfsOperations.provisionDirectory(instanceId, orgId, sysUser);
    hdfsOperations.addSystemUsersGroupAcl(path, orgId);

    credentialsStore.save(ImmutableMap.of(USER, instanceId.toString(), PASSWORD, password),
        instanceId);
  }

  @Override
  public Map<String, Object> bind(ServiceInstance serviceInstance) throws ServiceBrokerException {
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    UUID orgId = UUID.fromString(serviceInstance.getOrganizationGuid());
    Map<String, Object> configurationMap =
        bindingOperations.createCredentialsMap(instanceId, orgId);
    Map<String, Object> storedCredentials = credentialsStore.get(instanceId);
    Map<String, Object> credentials = new HashMap<>();

    credentials.putAll(configurationMap);
    credentials.putAll(storedCredentials);
    return credentials;
  }

}
