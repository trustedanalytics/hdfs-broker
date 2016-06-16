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

import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;
import org.trustedanalytics.servicebroker.framework.store.zookeeper.ZookeeperCredentialsStore;
import org.trustedanalytics.servicebroker.hdfs.plans.binding.HdfsBindingClientFactory;

import com.google.common.collect.ImmutableMap;
import org.trustedanalytics.servicebroker.hdfs.plans.provisioning.HdfsProvisioningClientFactory;
import org.trustedanalytics.servicebroker.hdfs.users.GroupMappingOperations;


@RunWith(MockitoJUnitRunner.class)
public final class HdfsPlanGetUserDirectoryTest extends HdfsPlanTestBase {

  private static final FsPermission FS_PERMISSION = new FsPermission(FsAction.ALL, FsAction.ALL,
      FsAction.NONE);
  private static final String USER = "user";
  private static final String PASSWORD = "password";
  private static final String URI = "uri";

  private HdfsPlanGetUserDirectory planUnderTest;

  @Mock
  private HdfsClient hdfsClient;

  @Mock
  private HdfsClient encryptedHdfsClient;

  @Mock
  private ZookeeperCredentialsStore zookeeperCredentialsStore;

  @Mock
  private GroupMappingOperations groupMappingOperations;

  @Before
  public void setup() {
    planUnderTest =
        new HdfsPlanGetUserDirectory(
            HdfsProvisioningClientFactory.create(hdfsClient, encryptedHdfsClient, USERSPACE_PATH_TEMPLATE),
            HdfsBindingClientFactory.create(getInputCredentials(), USERSPACE_PATH_TEMPLATE),
            groupMappingOperations,
            zookeeperCredentialsStore);
  }

  @Test
  public void provision_correctTemplateGetUserFromCredentials_replaceVariablesWithValuesAndCreateDir()
      throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    when(zookeeperCredentialsStore.exists(instanceId)).thenReturn(true);

    planUnderTest.provision(serviceInstance,
        Optional.of(ImmutableMap.of(URI, getFullDirectoryPathToProvision(serviceInstance))));

    verify(zookeeperCredentialsStore).exists(instanceId);
  }

  @Test
  public void provision_correctUploaderTemplateGetUserFromCredentials_replaceVariablesWithValuesAndCreateDir()
      throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    when(zookeeperCredentialsStore.exists(instanceId)).thenReturn(false);

    String uploaderPath = getFullDirectoryPathToProvision(serviceInstance) + UUID.randomUUID().toString() + "/000000_1";

    planUnderTest.provision(serviceInstance,
        Optional.of(ImmutableMap.of(URI, uploaderPath)));

    verify(zookeeperCredentialsStore).exists(instanceId);
    verify(encryptedHdfsClient, times(4)).addAclEntry(anyString(), any(AclEntry.class));
  }

  @Test
  public void provision_incorrectUploaderTemplateGetUserFromCredentials_replaceVariablesWithValuesAndCreateDir()
      throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    when(zookeeperCredentialsStore.exists(instanceId)).thenReturn(false);

    String uploaderPath = getFullDirectoryPathToProvision(serviceInstance) + "a/000000_1";

    planUnderTest.provision(serviceInstance,
        Optional.of(ImmutableMap.of(URI, uploaderPath)));

    verify(zookeeperCredentialsStore).exists(instanceId);
    verify(encryptedHdfsClient, times(2)).addAclEntry(anyString(), any(AclEntry.class));
  }

  @Test
  public void provision_correctTemplateCreateNewUser_replaceVariablesWithValuesAndCreateDir()
      throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    when(zookeeperCredentialsStore.exists(instanceId)).thenReturn(false);

    planUnderTest.provision(serviceInstance,
        Optional.of(ImmutableMap.of(URI, getFullDirectoryPathToProvision(serviceInstance))));

    verify(zookeeperCredentialsStore).exists(instanceId);
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

  @Test
  public void binding_getCredentialsFromExsitsingInstanceWithoutCredentials_replaceUriVariable() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    Map<String, Object> credentials =
        ImmutableMap.of();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    when(zookeeperCredentialsStore.get(instanceId)).thenReturn(credentials);

    Map<String, Object> bind = planUnderTest.bind(serviceInstance);
    assertThat(bind.containsKey(URI), equalTo(false));
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

  @Test
  public void provision_withoutUriParameter_saveEmtpyCredentialsMap() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    planUnderTest.provision(serviceInstance, Optional.of(ImmutableMap.of()));
    verify(zookeeperCredentialsStore).save(ImmutableMap.of(), instanceId);
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
