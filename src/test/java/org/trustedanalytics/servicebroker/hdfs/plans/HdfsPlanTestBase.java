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
package org.trustedanalytics.servicebroker.hdfs.plans;

import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.trustedanalytics.servicebroker.framework.Credentials;

import com.google.common.collect.ImmutableMap;

abstract class HdfsPlanTestBase {

  static final String USERSPACE_PATH_TEMPLATE = "/org/%{organization}/brokers/userspace/%{instance}";

  String getDirectoryPathToProvision(ServiceInstance serviceInstance) {
    String orgId = serviceInstance.getOrganizationGuid();
    String instanceId = serviceInstance.getServiceInstanceId();
    return "/org/" + orgId + "/brokers/userspace/" + instanceId;
  }

  String getFullDirectoryPathToProvision(ServiceInstance serviceInstance) {
    String orgId = serviceInstance.getOrganizationGuid();
    String instanceId = serviceInstance.getServiceInstanceId();
    return "hdfs://namespace/org/" + orgId + "/brokers/userspace/" + instanceId + "/";
  }

  Credentials getInputCredentials() {
    //@formatter:off
    return new Credentials(ImmutableMap.of(
        "key1", ImmutableMap.of(
            "innerKey1", "innerValue1",
            "innerKey2", "innerValue2"
        ),
        "key2", "value2",
        "fs.defaultFS", "hdfs://name1"
    ));
    //@formatter:on
  }

  ImmutableMap<String, Object> getExpectedOutputCredentialsMap(ServiceInstance serviceInstance) {
    String orgId = serviceInstance.getOrganizationGuid();
    String instanceId = serviceInstance.getServiceInstanceId();
    //@formatter:off
    return ImmutableMap.of(
        "key1", ImmutableMap.of(
            "innerKey1", "innerValue1",
            "innerKey2", "innerValue2"
        ),
        "key2", "value2",
        "fs.defaultFS", "hdfs://name1",
        "uri", "hdfs://name1/org/" + orgId + "/brokers/userspace/" + instanceId + "/"
    );
    //@formatter:on
  }
}
