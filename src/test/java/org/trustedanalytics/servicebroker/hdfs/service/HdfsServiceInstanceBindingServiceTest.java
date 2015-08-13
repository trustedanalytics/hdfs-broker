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
import org.trustedanalytics.servicebroker.hdfs.config.ExternalConfiguration;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HdfsServiceInstanceBindingServiceTest {

    private static final String CREDENTIALS_URI = "hdfs://namenode:port/";

    @Mock
    private ExternalConfiguration configuration;

    @Mock
    private ServiceInstanceBindingService instanceBindingService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HdfsServiceInstanceBindingService service;

    @Before
    public void init() {
        service =
                new HdfsServiceInstanceBindingService(instanceBindingService,
                        ImmutableMap.of(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS, CREDENTIALS_URI),
                        configuration);
    }

    private ServiceInstanceBinding getServiceInstanceBinding(String id) {
        return new ServiceInstanceBinding(id, "serviceInstanceId", Collections.emptyMap(), null, "guid");
    }

    private ServiceInstance getServiceInstance(String id) {
        return new ServiceInstance(new CreateServiceInstanceRequest(id, "planId", "organizationGuid", "spaceGuid"));
    }

    @Test
    public void testCreateServiceInstanceBinding_success_saveAndReturnsNewServiceInstanceBindingWithCredentials()
            throws Exception {
        when(configuration.getUserspaceChroot()).thenReturn("/userspace/root");

        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest(
                getServiceInstance("serviceId").getServiceDefinitionId(), "planId", "appGuid").
                withBindingId("bindingId").withServiceInstanceId("serviceInstanceId");
        when(instanceBindingService.createServiceInstanceBinding(request)).thenReturn(getServiceInstanceBinding("id"));
        ServiceInstanceBinding instance = service.createServiceInstanceBinding(request);

        assertThat(instance.getCredentials().get("uri"), equalTo(CREDENTIALS_URI + "userspace/root/serviceInstanceId/"));
    }
}
