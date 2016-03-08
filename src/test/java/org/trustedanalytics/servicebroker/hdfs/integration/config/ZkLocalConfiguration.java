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
package org.trustedanalytics.servicebroker.hdfs.integration.config;

import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClientBuilder;
import org.trustedanalytics.servicebroker.framework.Qualifiers;
import org.trustedanalytics.servicebroker.test.zookeeper.ZooKeeperCredentials;
import org.trustedanalytics.servicebroker.test.zookeeper.ZooKeeperTestOperations;

@Configuration
public class ZkLocalConfiguration {

  @Value("${store.user}")
  @NotNull
  private String user;

  @Value("${store.password}")
  @NotNull
  private String password;

  @Value("${store.path}")
  @NotNull
  private String brokerStoreNode;

  @Bean
  public TestingServer initEmbededZKServer() throws Exception {
    TestingServer zkServer = new TestingServer();
    zkServer.start();
    ZooKeeperCredentials credendials = new ZooKeeperCredentials(zkServer.getConnectString(), user, password);
    ZooKeeperTestOperations.createSecuredNode(credendials, brokerStoreNode);
    return zkServer;
  }

  @Bean(initMethod = "init", destroyMethod = "destroy")
  @Qualifier(Qualifiers.BROKER_STORE)
  @Profile("integration-test")
  public ZookeeperClient getZkClient(TestingServer zkServer) throws IOException {
    ZookeeperClient client =
        new ZookeeperClientBuilder(zkServer.getConnectString(), user, password, brokerStoreNode).build();
    return client;
  }
}
