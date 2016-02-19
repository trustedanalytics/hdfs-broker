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
package org.trustedanalytics.servicebroker.hdfs.config.store;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.serialization.RepositoryDeserializer;
import org.trustedanalytics.cfbroker.store.serialization.RepositorySerializer;
import org.trustedanalytics.servicebroker.hdfs.config.Qualifiers;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
class SerializationConfig {

  private static final ObjectMapper mapper = new ObjectMapper();

  public <T> RepositorySerializer<T> getSerializer() {
    return mapper::writeValueAsBytes;
  }

  public <T> RepositoryDeserializer<T> getDeserializer(Class<T> type) {
    return t -> mapper.readValue(t, type);
  }

  @Bean
  @Qualifier(Qualifiers.SERVICE_INSTANCE)
  public RepositorySerializer<ServiceInstance> getServiceInstanceSerializer() {
    return getSerializer();
  }

  @Bean
  @Qualifier(Qualifiers.SERVICE_INSTANCE_BINDING)
  public RepositorySerializer<CreateServiceInstanceBindingRequest> getServiceInstanceBindingSerializer() {
    return mapper::writeValueAsBytes;
  }

  @Bean
  @Qualifier(Qualifiers.SERVICE_INSTANCE)
  public RepositoryDeserializer<ServiceInstance> getServiceInstanceDeserializer() {
    return getDeserializer(ServiceInstance.class);
  }

  @Bean
  @Qualifier(Qualifiers.SERVICE_INSTANCE_BINDING)
  public RepositoryDeserializer<CreateServiceInstanceBindingRequest> getServiceInstanceBindingDeserializer() {
    return t -> mapper.readValue(t, CreateServiceInstanceBindingRequest.class);
  }
}
