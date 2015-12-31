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
package org.trustedanalytics.servicebroker.hdfs.service;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class HdfsServiceInstanceServiceTest {

    public static final String METADATA_PATH = "/org/%{organization}/brokers/metadata/%{instance}";

    private HdfsServiceInstanceService service;

    @Mock
    private ServiceInstanceService instanceService;

    @Mock
    private HdfsClient userHdfsClient;

    @Mock
    private HdfsClient adminHdfsClient;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String instanceId;
    private String orgId;

    @Before
    public void before() throws IOException {
        service = new HdfsServiceInstanceService(instanceService, userHdfsClient, adminHdfsClient, METADATA_PATH);
        instanceId = UUID.randomUUID().toString();
        orgId = UUID.randomUUID().toString();
    }

    @Test
    public void testCreateServiceInstance_successWithSharedPlan_delegateCallAndForwardBackReturnedServiceInstance()
            throws Exception {
        ServiceInstance instance = getServiceInstance(instanceId.toString(), "plan-shared");
        ServiceInstance returnedInstance = createServiceInstanceWithDir(instance);
        assertThat(returnedInstance, equalTo(instance));
    }

    @Test
    public void testCreateServiceInstance_successWithEncryptedPlan_delegateCallAndForwardBackReturnedServiceInstance()
            throws Exception {
        ServiceInstance instance = getServiceInstance(instanceId.toString(), "plan-encrypted");
        ServiceInstance returnedInstance = createServiceInstanceWithDir(instance);
        verify(adminHdfsClient).createEncryptedZone(getExpextedPath());
        assertThat(returnedInstance, equalTo(instance));
    }

    private ServiceInstance createServiceInstanceWithDir(ServiceInstance instance)
            throws ServiceBrokerException, ServiceInstanceExistsException, IOException {
        CreateServiceInstanceRequest request = new CreateServiceInstanceRequest(
                getServiceDefinition().getId(), instance.getPlanId(), instance.getOrganizationGuid(), instance.getSpaceGuid()).
                withServiceInstanceId(instance.getServiceInstanceId()).withServiceDefinition(getServiceDefinition());

        when(instanceService.createServiceInstance(request)).thenReturn(instance);
        ServiceInstance returnedInstance = service.createServiceInstance(request);
        verify(instanceService).createServiceInstance(request);
        verify(userHdfsClient).createDir(getExpextedPath());
        return returnedInstance;
    }

    private ServiceInstance getServiceInstance(String id, String plan) {
        return new ServiceInstance(
                new CreateServiceInstanceRequest(getServiceDefinition().getId(), plan, orgId,
                        UUID.randomUUID().toString()).withServiceInstanceId(id));
    }


    private ServiceDefinition getServiceDefinition() {
        return new ServiceDefinition("def", "name", "desc", true, Collections.emptyList());
    }

    private String getExpextedPath() {
        return HdfsPathTemplateUtils.fill(METADATA_PATH, instanceId, orgId);
    }

}