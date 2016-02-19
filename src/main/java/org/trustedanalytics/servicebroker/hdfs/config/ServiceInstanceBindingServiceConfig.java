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
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.config.HadoopZipConfiguration;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.impl.ServiceInstanceBindingServiceStore;
import org.trustedanalytics.servicebroker.hdfs.config.catalog.BrokerPlans;
import org.trustedanalytics.servicebroker.hdfs.service.HdfsServiceInstanceBindingService;
import org.trustedanalytics.servicebroker.hdfs.service.HdfsServiceInstanceService;

import com.google.common.collect.ImmutableMap;

@Configuration
public class ServiceInstanceBindingServiceConfig {

  @Autowired
  private BrokerPlans brokerPlans;

  @Autowired
  private ExternalConfiguration configuration;

  @Autowired
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE_BINDING)
  private BrokerStore<CreateServiceInstanceBindingRequest> store;

  @Bean
  public ServiceInstanceBindingService getServiceInstanceBindingService(
      ServiceInstanceService serviceInstanceService) throws IOException, LoginException {
    return new HdfsServiceInstanceBindingService(new ServiceInstanceBindingServiceStore(store),
        getCredentials(), serviceInstanceService, brokerPlans, configuration.getUserspaceChroot());
  }

  private Map<String, Object> getCredentials() throws IOException {
    HadoopZipConfiguration hadoopZipConfiguration =
        HadoopZipConfiguration.createHadoopZipConfiguration(configuration.getHdfsProvidedZip());
    Map<String, String> configParams = hadoopZipConfiguration.getAsParameterMap();
    if (!configParams.containsKey(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS)) {
      throw new IllegalStateException(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS
          + " was not found in hadoop configuration");
    }

    return new ImmutableMap.Builder()
        .put(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS,
            configParams.get(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS))
        .putAll(hadoopZipConfiguration.getBrokerCredentials()).build();
  }
}
