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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.servicebroker.framework.store.zookeeper.ZookeeperCredentialsStore;
import org.trustedanalytics.servicebroker.hdfs.plans.binding.HdfsBindingClientFactory;

import com.google.common.collect.ImmutableMap;


@RunWith(MockitoJUnitRunner.class)
public final class HdfsPlanGetUserDirectoryTest extends HdfsPlanTestBase {

  private static final FsPermission FS_PERMISSION = new FsPermission(FsAction.ALL, FsAction.ALL,
      FsAction.NONE);
  private static final String USER = "user";
  private static final String PASSWORD = "password";
  private static final String URI = "uri";

  private HdfsPlanGetUserDirectory planUnderTest;

  @Mock
  private ZookeeperCredentialsStore zookeeperCredentialsStore;

  @Before
  public void setup() {
    planUnderTest =
        new HdfsPlanGetUserDirectory(HdfsBindingClientFactory.create(getInputCredentials(),
            USERSPACE_PATH_TEMPLATE), zookeeperCredentialsStore);
  }

  @Test
  public void provision_templateWithOrgAndInstanceVariables_replaceVariablesWithValuesAndCreateDir()
      throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    planUnderTest.provision(serviceInstance,
        Optional.of(ImmutableMap.of(URI, getFullDirectoryPathToProvision(serviceInstance))));

    verify(zookeeperCredentialsStore).get(instanceId);
  }

  @Test
  public void binding_getCredentialsFromExsitsingInstance_replaceUriVariable() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    Map<String, Object> credentials =
        ImmutableMap.of(USER, "test", PASSWORD, "test", URI, "toReplace");
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    when(zookeeperCredentialsStore.get(instanceId)).thenReturn(credentials);

    Map<String, Object> bind = planUnderTest.bind(serviceInstance);
    assertThat(bind.get(USER), equalTo("test"));
    assertThat(bind.get(PASSWORD), equalTo("test"));
    assertThat(bind.get(URI), equalTo("toReplace"));
  }

  @Test(expected = ServiceBrokerException.class)
  public void binding_withoutParamter_throwServiceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    doThrow(new ServiceBrokerException("znode not exsits")).when(zookeeperCredentialsStore).get(
        instanceId);
    Map<String, Object> bind = planUnderTest.bind(serviceInstance);
  }

  @Test(expected = ServiceBrokerException.class)
  public void provision_notValidPath_throwServiceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    planUnderTest.provision(serviceInstance,
        Optional.of(ImmutableMap.of(URI, getDirectoryPathToProvision(serviceInstance))));
  }

  @Test(expected = ServiceBrokerException.class)
  public void provision_withoutUriParameter_throwServiceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    planUnderTest.provision(serviceInstance, Optional.of(ImmutableMap.of()));
  }

  @Test(expected = ServiceBrokerException.class)
  public void provision_instanceForPathNotExists_throwServiceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    doThrow(new ServiceBrokerException("znode not exsits")).when(zookeeperCredentialsStore).get(
        instanceId);

    planUnderTest.provision(serviceInstance,
        Optional.of(ImmutableMap.of(URI, getDirectoryPathToProvision(serviceInstance))));
  }

}
