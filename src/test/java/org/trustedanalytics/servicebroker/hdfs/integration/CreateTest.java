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
package org.trustedanalytics.servicebroker.hdfs.integration;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsAssert.deeplyEqualTo;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateBindingRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateInstanceRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.io.IOException;
import java.util.UUID;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.servicebroker.hdfs.config.Application;
import org.trustedanalytics.servicebroker.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.config.HdfsLocalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.config.KerberosLocalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.config.ZkLocalConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, HdfsLocalConfiguration.class, ZkLocalConfiguration.class,
    KerberosLocalConfiguration.class})
@WebAppConfiguration
@IntegrationTest("server.port=0")
@ActiveProfiles("integration-test")
public class CreateTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  @Qualifier("user")
  private FileSystem userFileSystem;

  @Autowired
  private ZookeeperClient zkClient;

  @Autowired
  private ExternalConfiguration conf;

  @Autowired
  private ServiceInstanceService serviceBean;

  @Autowired
  private ServiceInstanceBindingService bindingBean;

  @Test
  public void createInstancePlanShared_validRequest_metadataSavedAndUserDirProvisioned() throws Exception {
    //arrange
    String testId = UUID.randomUUID().toString();
    ServiceInstance instance = getServiceInstance(testId, "fakeBaseGuid-shared-plan");
    CreateServiceInstanceRequest request = getCreateInstanceRequest(instance);

    //act
    serviceBean.createServiceInstance(request);

    //assert
    assertTrue(userDirectoryProvisioned(testId, instance.getOrganizationGuid()));
    ServiceInstance savedInstance = getSavedInstanceFromZooKeeper(testId);
    assertThat(savedInstance, deeplyEqualTo(instance));
  }

  @Test
  public void createBindingPlanShared_validRequest_credentialsReturnedBindingSavedOnFileSystem() throws Exception {

    String serviceInstanceId = UUID.randomUUID().toString();
    String bindingId = UUID.randomUUID().toString();
    ServiceInstance instance = getServiceInstance(serviceInstanceId, "fakeBaseGuid-shared-plan");

    CreateServiceInstanceRequest request = getCreateInstanceRequest(instance);
    serviceBean.createServiceInstance(request);

    //arrange
    CreateServiceInstanceBindingRequest bindReq =
        getCreateBindingRequest(serviceInstanceId, instance.getPlanId()).withBindingId(bindingId);

    //act
    ServiceInstanceBinding serviceInstanceBinding = bindingBean.createServiceInstanceBinding(bindReq);
    String userDirUri = (String) serviceInstanceBinding.getCredentials().get("uri");

    //assert
    assertThat(userDirUri, endsWith(getUserDirectoryPath(serviceInstanceId, instance.getOrganizationGuid())));
    CreateServiceInstanceBindingRequest savedRequest = getSavedBindReqFromZooKeeper(serviceInstanceId, bindingId);
    assertThat(savedRequest, equalTo(bindReq));
  }

  private boolean userDirectoryProvisioned(String serviceInstanceId, String organizationId) throws IOException {
    return userFileSystem.exists(new Path(getUserDirectoryPath(serviceInstanceId, organizationId)));
  }

  private String getUserDirectoryPath(String serviceInstanceId, String organizationId) {
    return HdfsPathTemplateUtils.fill(conf.getUserspaceChroot(), serviceInstanceId, organizationId) + "/";
  }

  private ServiceInstance getSavedInstanceFromZooKeeper(String id) throws IOException {
    byte[] savedBytes = zkClient.getZNode(id);
    return mapper.readValue(savedBytes, ServiceInstance.class);
  }

  private CreateServiceInstanceBindingRequest getSavedBindReqFromZooKeeper(String serviceId, String bindingId)
      throws IOException {
    byte[] savedBytes = zkClient.getZNode(serviceId + "/" + bindingId);
    return mapper.readValue(savedBytes, CreateServiceInstanceBindingRequest.class);
  }
}
