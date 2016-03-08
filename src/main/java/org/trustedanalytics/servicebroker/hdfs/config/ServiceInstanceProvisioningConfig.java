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

import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;
import org.trustedanalytics.cfbroker.store.hdfs.service.SimpleHdfsClient;

@Configuration
public class ServiceInstanceProvisioningConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceProvisioningConfig.class);

  @Autowired
  @Qualifier(HdfsConstants.USER_QUALIFIER)
  private FileSystem fs;

  @Autowired
  @Qualifier(HdfsConstants.SUPER_USER_QUALIFIER)
  private FileSystem adminFs;

  @Autowired
  private ExternalConfiguration configuration;

  @Bean
  public HdfsClient hdfsClient() {
    return new SimpleHdfsClient(fs);
  }

  @Bean
  public HdfsClient encryptedHdfsClient() {
    return new SimpleHdfsClient(adminFs);
  }

  @Bean
  public String userspacePathTemplate() {
    String chroot = configuration.getUserspaceChroot();
    LOGGER.info("ChRoot : " + chroot);
    return chroot;
  }
}
