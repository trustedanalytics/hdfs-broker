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
package org.trustedanalytics.servicebroker.hdfs.config.catalog;

import com.google.common.base.Strings;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerPlans {

  @Autowired
  private Catalog catalog;

  public static final String ENCRYPTED_KEY = "-encrypted";

  public static final String TEMPLATE_KEY = "-multitenant";

  public boolean getPlanProvisioning(String planId) {
    ServiceDefinition serviceDefinition = catalog.getServiceDefinitions().get(0);
    Plan catalogPlan = serviceDefinition.getPlans().stream().filter(plan -> plan.getId().equals(planId)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Trying to create instance with unsupported plan"));

    return (boolean)catalogPlan.getMetadata().get("isProvisioning");
  }

  public static boolean isEncrypted(String planId) {
    return Strings.nullToEmpty(planId).toLowerCase().contains(ENCRYPTED_KEY);
  }

  public static boolean isMultitenant(String planId) {
    return Strings.nullToEmpty(planId).toLowerCase().contains(TEMPLATE_KEY);
  }

}
