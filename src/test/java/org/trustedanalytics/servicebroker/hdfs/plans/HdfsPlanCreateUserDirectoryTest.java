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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.hadoop.fs.permission.*;
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
import org.trustedanalytics.servicebroker.hdfs.plans.provisioning.HdfsProvisioningClientFactory;
import org.trustedanalytics.servicebroker.hdfs.users.GroupMappingOperations;

import com.google.common.collect.ImmutableMap;
import org.trustedanalytics.servicebroker.hdfs.util.TestUtil;


@RunWith(MockitoJUnitRunner.class)
public final class HdfsPlanCreateUserDirectoryTest extends HdfsPlanTestBase {
  private static final String TECH_GROUP_POSTFIX = "_sys";

  private static final FsPermission FS_PERMISSION = new FsPermission(FsAction.ALL, FsAction.ALL,
      FsAction.NONE);
  private static final String TEMPLATE = "/test/org/%{organization}/brokers/userspace/%{instance}";
  private static final String USER = "user";
  private static final String PASSWORD = "password";
  private static final String URI = "uri";

  private HdfsPlanCreateUserDirectory planUnderTest;

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
        new HdfsPlanCreateUserDirectory(HdfsProvisioningClientFactory.create(hdfsClient,
            encryptedHdfsClient, USERSPACE_PATH_TEMPLATE), HdfsBindingClientFactory.create(
            getInputCredentials(), USERSPACE_PATH_TEMPLATE), groupMappingOperations,
            zookeeperCredentialsStore);
  }

  @Test
  public void provision_createNewUserWithDirectory_hdfsDirectoryCreated() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    UUID orgId = UUID.fromString(serviceInstance.getOrganizationGuid());
    UUID userId = UUID.randomUUID();
    String path = getDirectoryPathToProvision(serviceInstance);

    when(groupMappingOperations.createSysUser(any(UUID.class), any(UUID.class), anyString()))
        .thenReturn(userId);
    planUnderTest.provision(serviceInstance, Optional.of(ImmutableMap.of()));
    
    verify(encryptedHdfsClient).addAclEntry("/org/"+ serviceInstance.getOrganizationGuid()+"/brokers/userspace/"+serviceInstance.getServiceInstanceId(), TestUtil.hiveUserAcl());
    verify(encryptedHdfsClient).addAclEntry("/org/"+ serviceInstance.getOrganizationGuid()+"/brokers/userspace/"+serviceInstance.getServiceInstanceId(), TestUtil.hiveDefaultUserAcl());

    verify(groupMappingOperations).createSysUser(eq(orgId), eq(instanceId), anyString());
    verify(hdfsClient).createDir(path);
    verify(hdfsClient).setPermission(path, FS_PERMISSION);
    verify(encryptedHdfsClient).setOwner(path, userId.toString(), orgId.toString());
    verifyNoMoreInteractions(hdfsClient, encryptedHdfsClient);
  }

  @Test(expected = ServiceBrokerException.class)
  public void provision_errorDuringUserCreation_serviceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();

    doThrow(new ServiceBrokerException("znode not exsits")).when(groupMappingOperations)
        .createSysUser(any(UUID.class), any(UUID.class), anyString());

    planUnderTest.provision(serviceInstance, Optional.of(ImmutableMap.of()));
  }

  @Test(expected = ServiceBrokerException.class)
  public void provision_errorDuringDirectoryCreation_serviceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    UUID orgId = UUID.fromString(serviceInstance.getOrganizationGuid());
    UUID userId = UUID.randomUUID();
    String path = getDirectoryPathToProvision(serviceInstance);

    when(groupMappingOperations.createSysUser(any(UUID.class), any(UUID.class), anyString()))
        .thenReturn(userId);
    doThrow(new IOException("Can't create directory")).when(hdfsClient)
        .createDir(any(String.class));

    planUnderTest.provision(serviceInstance, Optional.of(ImmutableMap.of()));
  }

  @Test
  public void binding_userWithDirectoryCreated_returnCredentialsMap() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    Map<String, Object> credentials = ImmutableMap.of(USER, "test", PASSWORD, "test", URI, "path");
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    when(zookeeperCredentialsStore.get(instanceId)).thenReturn(credentials);

    Map<String, Object> bind = planUnderTest.bind(serviceInstance);
    assertThat(bind.get(USER), equalTo("test"));
    assertThat(bind.get(PASSWORD), equalTo("test"));
    assertThat(bind.get(URI), equalTo("path"));
  }

  @Test(expected = ServiceBrokerException.class)
  public void binding_zookeeperException_throwsServiceBrokerException() throws Exception {
    ServiceInstance serviceInstance = getServiceInstance();
    Map<String, Object> credentials = ImmutableMap.of();
    UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
    doThrow(new ServiceBrokerException("Can't create directory")).when(zookeeperCredentialsStore).get(instanceId);

    planUnderTest.bind(serviceInstance);
  }

}
