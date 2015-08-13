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

import org.trustedanalytics.servicebroker.hdfs.config.Application;
import org.trustedanalytics.servicebroker.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.config.HdfsLocalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.utils.CfModelsFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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

import java.io.FileNotFoundException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, HdfsLocalConfiguration.class})
@WebAppConfiguration
@IntegrationTest("server.port=0")
@ActiveProfiles("integration-test")
public class CreateDeleteThenGetTest {

    @Autowired
    private FileSystem fileSystem;

    @Autowired
    private ExternalConfiguration conf;

    @Autowired
    private ServiceInstanceService serviceBean;

    @Autowired
    private ServiceInstanceBindingService bindingBean;

    @Test
    public void deleteServiceInstance_instanceCreated_getReturnsNull() throws Exception {

        String serviceInstanceId = "id3";

        //arrange
        ServiceInstance instance = CfModelsFactory.getServiceInstance(serviceInstanceId);
        CreateServiceInstanceRequest request = new CreateServiceInstanceRequest(
            CfModelsFactory.getServiceDefinition().getId(),
            instance.getPlanId(),
            instance.getOrganizationGuid(),
            instance.getSpaceGuid()).withServiceInstanceId(
            instance.getServiceInstanceId()).withServiceDefinition(
            CfModelsFactory.getServiceDefinition());

        serviceBean.createServiceInstance(request);

        //act
        DeleteServiceInstanceRequest deleteRequest =
            new DeleteServiceInstanceRequest(instance.getServiceInstanceId(),
                instance.getServiceDefinitionId(), instance.getPlanId());
        serviceBean.deleteServiceInstance(deleteRequest);

        //assert
        ServiceInstance serviceInstance = serviceBean.getServiceInstance(serviceInstanceId);
        assertThat(serviceInstance, is(nullValue()));
    }

    @Test(expected = FileNotFoundException.class)
    public void deleteBinding_bindingCreated_bindingDeletedFromFileSystem() throws Exception {

        String bindingId = "bindingId3";
        String serviceInstanceId = "serviceInstanceId";

        //arrange
        CreateServiceInstanceBindingRequest bindReq = new CreateServiceInstanceBindingRequest(
            CfModelsFactory.getServiceInstance(serviceInstanceId).getServiceDefinitionId(),
            "planId", "appGuid")
            .withBindingId(bindingId);
        bindingBean.createServiceInstanceBinding(bindReq);

        //act
        DeleteServiceInstanceBindingRequest deleteRequest =
            new DeleteServiceInstanceBindingRequest(bindReq.getBindingId(),
                CfModelsFactory.getServiceInstance(serviceInstanceId),
                bindReq.getServiceDefinitionId(), bindReq.getPlanId());
        bindingBean.deleteServiceInstanceBinding(deleteRequest);

        //assert
        fileSystem.getXAttrs(
            new Path(conf.getMetadataChroot() + "/" + serviceInstanceId + "/" + bindingId));
    }
}
