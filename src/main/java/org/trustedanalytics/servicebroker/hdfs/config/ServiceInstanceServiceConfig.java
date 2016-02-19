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

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.fs.FileSystem;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;
import org.trustedanalytics.cfbroker.store.hdfs.service.SimpleHdfsClient;
import org.trustedanalytics.cfbroker.store.impl.ServiceInstanceServiceStore;
import org.trustedanalytics.servicebroker.hdfs.config.catalog.BrokerPlans;
import org.trustedanalytics.servicebroker.hdfs.service.HdfsServiceInstanceService;

@Configuration
public class ServiceInstanceServiceConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceServiceConfig.class);

  @Autowired
  private ExternalConfiguration configuration;

  @Autowired
  private BrokerPlans brokerPlans;

  @Autowired
  @Qualifier(Qualifiers.USER)
  private FileSystem fs;

  @Autowired
  @Qualifier(Qualifiers.SUPER_USER)
  private FileSystem adminFs;

  @Autowired
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE)
  private BrokerStore<ServiceInstance> store;

  @Bean
  public ServiceInstanceService getServiceInstanceService() throws IOException, LoginException {
    LOGGER.info("ChRoot : " + configuration.getUserspaceChroot());
    HdfsClient hdfsClient = new SimpleHdfsClient(fs);
    HdfsClient hdfsAdminClient = new SimpleHdfsClient(adminFs);
    return new HdfsServiceInstanceService(new ServiceInstanceServiceStore(store), hdfsClient,
        hdfsAdminClient, brokerPlans, configuration.getUserspaceChroot());
  }
}
