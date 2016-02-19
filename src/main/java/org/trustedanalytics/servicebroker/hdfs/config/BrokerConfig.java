/**
 * Portions Copyright (c) 2015 Intel Corporation
 */
package org.trustedanalytics.servicebroker.hdfs.config;

import org.cloudfoundry.community.servicebroker.model.BrokerApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Force the base spring boot packages to be searched for dependencies.
 * 
 * @author sgreenberg
 *
 */

@Configuration
@ComponentScan(basePackages = "org.trustedanalytics.servicebroker")
public class BrokerConfig {

  @Bean
  public BrokerApiVersion brokerApiVersion() {
    return new BrokerApiVersion();
  }
}
