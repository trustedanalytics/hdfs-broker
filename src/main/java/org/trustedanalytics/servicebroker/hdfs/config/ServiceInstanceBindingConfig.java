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

import static org.trustedanalytics.servicebroker.hdfs.config.HdfsConstants.HADOOP_DEFAULT_FS;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.trustedanalytics.cfbroker.config.HadoopZipConfiguration;
import org.trustedanalytics.servicebroker.framework.Credentials;

import com.google.common.collect.ImmutableMap;
import org.trustedanalytics.servicebroker.framework.kerberos.KerberosProperties;

@Configuration
public class ServiceInstanceBindingConfig {

  @Autowired
  private ExternalConfiguration configuration;

  @Autowired
  private KerberosProperties kerberosProperties;

  @Bean
  public Credentials getCredentials() throws IOException {
    HadoopZipConfiguration hadoopZipConfiguration =
        HadoopZipConfiguration.createHadoopZipConfiguration(configuration.getHdfsProvidedZip());
    Map<String, String> configParams = hadoopZipConfiguration.getAsParameterMap();
    if (!configParams.containsKey(HADOOP_DEFAULT_FS)) {
      throw new IllegalStateException(HADOOP_DEFAULT_FS + " was not found in hadoop configuration");
    }

    ImmutableMap.Builder<String, Object> credentialsBuilder = new ImmutableMap.Builder<String, Object>()
        .put(HADOOP_DEFAULT_FS, configParams.get(HADOOP_DEFAULT_FS))
        .putAll(hadoopZipConfiguration.getBrokerCredentials());

    if(kerberosProperties.isKerberosEnabled()){
      credentialsBuilder.put("kerberos", kerberosProperties.getCredentials());
    }

    return new Credentials(credentialsBuilder.build());
  }
}
