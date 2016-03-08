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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateBindingRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateInstanceRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getDeleteBindingRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getDeleteInstanceRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.io.IOException;
import java.util.UUID;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.servicebroker.hdfs.config.Application;
import org.trustedanalytics.servicebroker.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.config.HdfsLocalConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, HdfsLocalConfiguration.class})
@WebAppConfiguration
@IntegrationTest("server.port=0")
@ActiveProfiles("integration-test")
public class CreateDeleteThenGetTest {

  @Autowired
  private ZookeeperClient zkClient;

  @Autowired
  private ExternalConfiguration conf;

  @Autowired
  private ServiceInstanceService serviceBean;

  @Autowired
  private ServiceInstanceBindingService bindingBean;

  @Test
  public void deleteServiceInstancePlanShared_instanceCreated_getReturnsNull() throws Exception {
    //arrange
    String serviceInstanceId = UUID.randomUUID().toString();
    ServiceInstance instance = getServiceInstance(serviceInstanceId, "fakeBaseGuid-shared-plan");
    CreateServiceInstanceRequest request = getCreateInstanceRequest(instance);
    serviceBean.createServiceInstance(request);

    //act
    DeleteServiceInstanceRequest deleteRequest = getDeleteInstanceRequest(instance);
    serviceBean.deleteServiceInstance(deleteRequest);

    //assert
    ServiceInstance serviceInstance = serviceBean.getServiceInstance(serviceInstanceId);
    assertThat(serviceInstance, is(nullValue()));
  }

  @Test(expected = IOException.class)
  public void deleteBindingPlanShared_bindingCreated_bindingDeletedFromFileSystem() throws Exception {
    //arrange
    String bindingId = UUID.randomUUID().toString();
    String serviceInstanceId = UUID.randomUUID().toString();
    ServiceInstance instance = getServiceInstance(serviceInstanceId, "fakeBaseGuid-shared-plan");
    serviceBean.createServiceInstance(getCreateInstanceRequest(instance));

    CreateServiceInstanceBindingRequest bindReq = getCreateBindingRequest(serviceInstanceId).withBindingId(bindingId);
    bindingBean.createServiceInstanceBinding(bindReq);

    //act
    DeleteServiceInstanceBindingRequest deleteRequest = getDeleteBindingRequest(serviceInstanceId, bindReq);
    bindingBean.deleteServiceInstanceBinding(deleteRequest);

    //assert
    zkClient.getZNode(serviceInstanceId + "/" + bindingId);
  }
}
