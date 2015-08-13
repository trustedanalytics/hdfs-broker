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

import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;

import static org.mockito.Matchers.any;


@RunWith(MockitoJUnitRunner.class)
public class HdfsServiceInstanceServiceTest {


    private HdfsServiceInstanceService service;

    @Mock
    private ServiceInstanceService instanceService;

    @Mock
    private HdfsClient userspaceHdfsClient;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        service = new HdfsServiceInstanceService(instanceService, userspaceHdfsClient);

    }

    @Test
    public void testCreateServiceInstance_success_delegateCallAndForwardBackReturnedServiceInstance()
            throws Exception {
        ServiceInstance instance = getServiceInstance("id");
        CreateServiceInstanceRequest request = new CreateServiceInstanceRequest(
                getServiceDefinition().getId(), instance.getPlanId(), instance.getOrganizationGuid(), instance.getSpaceGuid()).
                withServiceInstanceId(instance.getServiceInstanceId()).withServiceDefinition(getServiceDefinition());
        when(instanceService.createServiceInstance(request)).thenReturn(instance);
        ServiceInstance returnedInstance = service.createServiceInstance(request);
        verify(instanceService).createServiceInstance(request);
        verify(userspaceHdfsClient).createDir("id");
        assertThat(returnedInstance, equalTo(instance));
    }

    private ServiceInstance getServiceInstance(String id) {
        return new ServiceInstance(
                new CreateServiceInstanceRequest(getServiceDefinition().getId(), "planId", "organizationGuid", "spaceGuid")
                        .withServiceInstanceId(id));
    }


    private ServiceDefinition getServiceDefinition() {
        return new ServiceDefinition("def", "name", "desc", true, Collections.emptyList());
    }




}
