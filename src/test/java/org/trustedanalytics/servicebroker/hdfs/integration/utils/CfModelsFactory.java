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

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;

import java.util.Collections;

public class CfModelsFactory {
    public static ServiceInstance getServiceInstance(String id) {
        return new ServiceInstance(
            new CreateServiceInstanceRequest(getServiceDefinition().getId(), "planId",
                "organizationGuid", "spaceGuid").withServiceInstanceId(id));
    }

    public static ServiceDefinition getServiceDefinition() {
        return new ServiceDefinition("def", "name", "desc", true, Collections.emptyList());
    }
}
