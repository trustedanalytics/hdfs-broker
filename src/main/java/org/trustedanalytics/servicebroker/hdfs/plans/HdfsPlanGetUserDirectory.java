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

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trustedanalytics.servicebroker.framework.service.ServicePlanDefinition;
import org.trustedanalytics.servicebroker.framework.store.CredentialsStore;
import org.trustedanalytics.servicebroker.hdfs.path.HdfsBrokerInstancePath;
import org.trustedanalytics.servicebroker.hdfs.plans.binding.HdfsSpecificOrgBindingOperations;

import com.google.common.collect.ImmutableMap;

@Component("get-user-directory")
class HdfsPlanGetUserDirectory implements ServicePlanDefinition {

  private static final Logger LOGGER = LoggerFactory.getLogger(HdfsPlanCreateUserDirectory.class);
  private static final String URI_KEY = "uri";

  private final HdfsSpecificOrgBindingOperations bindingOperations;
  private final CredentialsStore credentialsStore;

  @Autowired
  public HdfsPlanGetUserDirectory(HdfsSpecificOrgBindingOperations bindingOperations,
      CredentialsStore zookeeperCredentialsStore) {
    this.bindingOperations = bindingOperations;
    this.credentialsStore = zookeeperCredentialsStore;
  }

  @Override
  public void provision(ServiceInstance serviceInstance, Optional<Map<String, Object>> parameters)
      throws ServiceInstanceExistsException, ServiceBrokerException {
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    if (isMapNotNullAndNotEmpty(parameters)) {
      String uri =
          getParameterUri(parameters.get(), URI_KEY).orElseThrow(
              () -> new ServiceBrokerException("No required parameter uri")).toString();
      LOGGER.info("Detected parameter path: " + uri);

      HdfsBrokerInstancePath instance = HdfsBrokerInstancePath.createInstance(uri);
      credentialsStore.save(
          ImmutableMap.<String, Object>builder()
              .putAll(credentialsStore.get(instance.getInstanceId())).put(URI_KEY, uri).build(),
          instanceId);
    }
    else {
      LOGGER.info("Creating instance without parameter. Only configuration will be provided");
      credentialsStore.save(ImmutableMap.of(), instanceId);
    }
  }

  @Override
  public Map<String, Object> bind(ServiceInstance serviceInstance) throws ServiceBrokerException {
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    UUID orgId = UUID.fromString(serviceInstance.getOrganizationGuid());
    Map<String, Object> configurationMap =
        bindingOperations.createCredentialsMap(instanceId, orgId);
    Map<String, Object> storedCredentials = credentialsStore.get(instanceId);
    Map<String, Object> credentials = new HashMap<>();

    configurationMap.remove(URI_KEY);
    credentials.putAll(configurationMap);
    credentials.putAll(storedCredentials);
    return credentials;
  }

  private boolean isMapNotNullAndNotEmpty(Optional<Map<String, Object>> map) {
    return map.isPresent() && !map.get().isEmpty();
  }

  private Optional<Object> getParameterUri(Map<String, Object> parameters, String key) {
    return Optional.ofNullable(parameters.get(key));
  }
}
