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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.trustedanalytics.servicebroker.hdfs.config.Application;
import org.trustedanalytics.servicebroker.hdfs.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.config.HdfsLocalConfiguration;
import org.trustedanalytics.servicebroker.hdfs.integration.utils.CfModelsAssert;
import org.trustedanalytics.servicebroker.hdfs.integration.utils.CfModelsFactory;
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
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, HdfsLocalConfiguration.class})
@WebAppConfiguration
@IntegrationTest("server.port=0")
@ActiveProfiles("integration-test")
public class CreateTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private FileSystem fileSystem;

    @Autowired
    private ExternalConfiguration conf;

    @Autowired
    private ServiceInstanceService serviceBean;

    @Autowired
    private ServiceInstanceBindingService bindingBean;

    @Test
    public void createServiceInstance_validRequest_metadataSavedAndUserDirProvisioned()
        throws Exception {

        String testId = "id1";

        //arrange
        ServiceInstance instance = CfModelsFactory.getServiceInstance(testId);
        CreateServiceInstanceRequest request = new CreateServiceInstanceRequest(
            CfModelsFactory.getServiceDefinition().getId(),
            instance.getPlanId(),
            instance.getOrganizationGuid(),
            instance.getSpaceGuid()).withServiceInstanceId(
            instance.getServiceInstanceId()).withServiceDefinition(
            CfModelsFactory.getServiceDefinition());

        //act
        serviceBean.createServiceInstance(request);

        //assert
        assertTrue(userDirectoryProvisioned(testId));
        ServiceInstance savedInstance = getSavedInstanceFromFileSystem(testId);
        CfModelsAssert.serviceInstancesAreEqual(savedInstance, instance);
    }

    @Test
    public void createBinding_validRequest_bindingSavedOnFileSystem() throws Exception {

        String serviceInstanceId = "serviceInstanceId";
        String bindingId = "bindingId1";

        //arrange
        CreateServiceInstanceBindingRequest bindReq = new CreateServiceInstanceBindingRequest(
            CfModelsFactory.getServiceInstance("serviceId").getServiceDefinitionId(), "planId",
            "appGuid").withBindingId(bindingId).withServiceInstanceId(serviceInstanceId);

        //act
        ServiceInstanceBinding serviceInstanceBinding =
            bindingBean.createServiceInstanceBinding(bindReq);
        String userDirUri = (String) serviceInstanceBinding.getCredentials().get("uri");

        //assert
        assertThat(userDirUri, endsWith(getUserDirectoryPath(serviceInstanceId)));
        CreateServiceInstanceBindingRequest savedRequest =
            getSavedBindReqFromFileSystem(serviceInstanceId, bindingId);
        CfModelsAssert.bindingRequestsAreEqual(savedRequest, bindReq);
    }

    private boolean userDirectoryProvisioned(String serviceInstanceId) throws IOException {
        return fileSystem.exists(new Path(getUserDirectoryPath(serviceInstanceId)));
    }

    private String getUserDirectoryPath(String serviceInstanceId) {
        return conf.getUserspaceChroot() + "/" + serviceInstanceId + "/";
    }

    private ServiceInstance getSavedInstanceFromFileSystem(String id) throws IOException {
        byte[] savedBytes = fileSystem.getXAttrs(new Path(conf.getMetadataChroot() + "/" + id))
            .get(conf.getInstanceXattr());
        return mapper.readValue(savedBytes, ServiceInstance.class);
    }

    private CreateServiceInstanceBindingRequest getSavedBindReqFromFileSystem(String serviceId,
        String bindingId) throws IOException {

        byte[] savedBytes = fileSystem
            .getXAttrs(new Path(conf.getMetadataChroot() + "/" + serviceId + "/" + bindingId))
            .get(conf.getBindingXattr());
        return mapper.readValue(savedBytes, CreateServiceInstanceBindingRequest.class);
    }
}
