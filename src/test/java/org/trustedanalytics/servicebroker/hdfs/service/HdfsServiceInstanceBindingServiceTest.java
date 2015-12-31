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

import com.google.common.collect.ImmutableMap;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HdfsServiceInstanceBindingServiceTest {

    private static final String CREDENTIALS_URI = "hdfs://namenode:port";
    private static final String USERSPACE_ROOT_TEMPLATE = "/orgs/%{organization}/userspace/%{instance}";

    @Mock
    private ServiceInstanceBindingService instanceBindingService;

    @Mock
    private ServiceInstanceService instanceService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HdfsServiceInstanceBindingService service;

    @Before
    public void init() {
        service =
                new HdfsServiceInstanceBindingService(instanceBindingService,
                        ImmutableMap.of(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS, CREDENTIALS_URI),
                        instanceService, USERSPACE_ROOT_TEMPLATE);
    }

    private ServiceInstanceBinding getServiceInstanceBinding(String id, String instanceId) {
        return new ServiceInstanceBinding(id, instanceId, Collections.emptyMap(), null, "guid");
    }

    private ServiceInstance getServiceInstance(String plan) {
        UUID id = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        return new ServiceInstance(new CreateServiceInstanceRequest(id.toString(), plan, orgId.toString(), "spaceGuid")
                .withServiceInstanceId(id.toString()));
    }

    private CreateServiceInstanceBindingRequest getBindingRequest(ServiceInstance instance) {
        return new CreateServiceInstanceBindingRequest(instance.getServiceDefinitionId(), instance.getPlanId(), "appGuid")
                .withBindingId("bindingId").withServiceInstanceId(instance.getServiceInstanceId());
    }

    @Test
    public void testCreateServiceInstanceBinding_simplePlan_returnsNewServiceInstanceBindingWithCredentials()
            throws Exception {
        ServiceInstance instance = getServiceInstance("simple");
        CreateServiceInstanceBindingRequest request = getBindingRequest(instance);
        ServiceInstanceBinding binding = getServiceInstanceBinding("id", instance.getServiceInstanceId());
        when(instanceBindingService.createServiceInstanceBinding(request)).thenReturn(binding);
        when(instanceService.getServiceInstance(instance.getServiceInstanceId())).thenReturn(instance);

        ServiceInstanceBinding result = service.createServiceInstanceBinding(request);

        String expected = "hdfs://namenode:port/orgs/" + instance.getOrganizationGuid() + "/userspace/"
                + instance.getServiceInstanceId() + "/";
        assertEquals(expected, result.getCredentials().get("uri"));
    }

    @Test
    public void testCreateServiceInstanceBinding_templatePlan_returnsNewServiceInstanceBindingWithCredentials()
            throws Exception {
        ServiceInstance instance = getServiceInstance("simple-multitenant");
        CreateServiceInstanceBindingRequest request = getBindingRequest(instance);
        ServiceInstanceBinding binding = getServiceInstanceBinding("id", instance.getServiceInstanceId());
        when(instanceBindingService.createServiceInstanceBinding(request)).thenReturn(binding);
        when(instanceService.getServiceInstance(instance.getServiceInstanceId())).thenReturn(instance);

        ServiceInstanceBinding result = service.createServiceInstanceBinding(request);

        String expected = CREDENTIALS_URI + "/orgs/%{organization}/userspace/" + instance.getServiceInstanceId() + "/";
        assertEquals(expected, result.getCredentials().get("uri"));
    }
}
