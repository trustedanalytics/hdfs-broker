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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.junit.Test;

import org.trustedanalytics.servicebroker.hdfs.plans.binding.HdfsBindingClientFactory;

public final class HdfsPlanBareTest extends HdfsPlanTestBase {

  @Test
  public void bind_templateWithOrgAndInstanceVariables_replaceVariablesWithValuesAndAppendUriToCredentialsMap()
      throws Exception {
    //arrange
    HdfsPlanBare plan =
        new HdfsPlanBare(HdfsBindingClientFactory.create(getInputCredentials(), USERSPACE_PATH_TEMPLATE));

    //act
    ServiceInstance serviceInstance = getServiceInstance();
    Map<String, Object> actualOutputCredentials = plan.bind(serviceInstance);

    //assert
    assertThat(actualOutputCredentials, equalTo(getExpectedOutputCredentialsMap(serviceInstance)));
  }

  @Override //uri shouldn't be added in this plan
  ImmutableMap<String, Object> getExpectedOutputCredentialsMap(ServiceInstance serviceInstance) {
    //@formatter:off
    return ImmutableMap.of(
        "key1", ImmutableMap.of(
            "innerKey1", "innerValue1",
            "innerKey2", "innerValue2"
        ),
        "key2", "value2",
        "fs.defaultFS", "hdfs://name1"
    );
    //@formatter:on
  }
}
