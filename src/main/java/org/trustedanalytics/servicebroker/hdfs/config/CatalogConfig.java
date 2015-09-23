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
package org.trustedanalytics.servicebroker.hdfs.config;

import com.google.common.collect.Lists;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class CatalogConfig {

    @Autowired
    private ExternalConfiguration configuration;

    private static final String SYSLOG_DRAIN = "syslog_drain";

    private static final String IMAGE_URL = "imageUrl";

    @Bean
    public Catalog catalog() {
        return new Catalog(Arrays.asList(new ServiceDefinition(configuration.getCfServiceId(), configuration.getCfServiceName(),
                "A simple hdfs broker", true, true, getSharedPlans(), null, getServiceDefinitionMetadata(), Arrays.asList(SYSLOG_DRAIN), null)));
    }

    private List<Plan> getSharedPlans() {
        return Lists.newArrayList(new Plan(configuration.getCfBaseId() + "-shared-plan", "shared",
                        "This is a default hdfs plan.", null, true),
                new Plan(configuration.getCfBaseId() + "-encrypted-plan", "encrypted",
                        "This is a encrypted hdfs plan.", null, true));
    }

    private Map<String, Object> getServiceDefinitionMetadata() {
        Map<String, Object> serviceMetadata = new HashMap<>();

        serviceMetadata.put(IMAGE_URL, configuration.getImageUrl());

        return serviceMetadata;
    }

}
