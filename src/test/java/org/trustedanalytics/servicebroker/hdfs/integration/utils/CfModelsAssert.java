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
package org.trustedanalytics.servicebroker.hdfs.integration.utils;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class CfModelsAssert {
    public static void serviceInstancesAreEqual(ServiceInstance actual,
        ServiceInstance expected) {

        assertThat(actual.getDashboardUrl(),
            equalTo(expected.getDashboardUrl()));
        assertThat(actual.getOrganizationGuid(),
            equalTo(expected.getOrganizationGuid()));
        assertThat(actual.getPlanId(),
            equalTo(expected.getPlanId()));
        assertThat(actual.getServiceDefinitionId(),
            equalTo(expected.getServiceDefinitionId()));
        assertThat(actual.getServiceInstanceId(),
            equalTo(expected.getServiceInstanceId()));
        assertThat(actual.getSpaceGuid(),
            equalTo(expected.getSpaceGuid()));
    }

    public static void bindingRequestsAreEqual(CreateServiceInstanceBindingRequest actual,
        CreateServiceInstanceBindingRequest expected) {

        assertThat(actual.getAppGuid(),
            equalTo(expected.getAppGuid()));
        assertThat(actual.getPlanId(),
            equalTo(expected.getPlanId()));
        assertThat(actual.getServiceDefinitionId(),
            equalTo(expected.getServiceDefinitionId()));
    }
}
