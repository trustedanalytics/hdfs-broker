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
package org.trustedanalytics.servicebroker.hdfs.integration.config.store;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.security.MessageDigest;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.springframework.security.crypto.codec.Base64;

import com.google.common.collect.ImmutableList;

public class ZkStoreTestUtils {

  private ZkStoreTestUtils() {}

  private static CuratorFramework getNewTempClient(String connectionString) {
    CuratorFramework tempClient =
        CuratorFrameworkFactory.builder().connectString(connectionString)
            .retryPolicy(new RetryOneTime(100)).build();
    tempClient.start();
    return tempClient;
  }

  private static CuratorFramework getNewTempClient(ZookeeperCredentials credentials) {
    CuratorFramework tempClient =
        CuratorFrameworkFactory
            .builder()
            .connectString(credentials.getConnectionString())
            .retryPolicy(new RetryOneTime(100))
            .authorization(
                "digest",
                String.format("%s:%s", credentials.getUsername(), credentials.getPassword())
                    .getBytes()).build();
    tempClient.start();
    return tempClient;
  }

  public static void createDir(ZookeeperCredentials credentials, String path) throws Exception {
    CuratorFramework tempClient = getNewTempClient(credentials.getConnectionString());

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] authDigest =
        md.digest(String.format("%s:%s", credentials.getUsername(), credentials.getPassword())
            .getBytes());
    String authEncoded = new String(Base64.encode(authDigest));
    ImmutableList<ACL> acl =
        ImmutableList.of(new ACL(ZooDefs.Perms.ALL, new Id("digest", String.format("%s:%s",
            credentials.getUsername(), authEncoded))));

    tempClient.create().creatingParentsIfNeeded().withACL(acl).forPath(path);

    tempClient.close();
  }

  public static void assertZNodeEquals(ZookeeperCredentials credentials, String path,
      byte[] expected) throws Exception {
    CuratorFramework tempClient = getNewTempClient(credentials);

    byte[] actual = tempClient.getData().forPath(path);
    assertThat(expected, equalTo(actual));

    tempClient.close();
  }

  public static void assertZNodeNotExist(ZookeeperCredentials credentials, String path)
      throws Exception {
    CuratorFramework tempClient = getNewTempClient(credentials);

    Stat stat = tempClient.checkExists().forPath(path);
    assertThat(stat, is(nullValue()));

    tempClient.close();
  }

  public static class ZookeeperCredentials {
    private String connectionString;
    private String username;
    private String password;

    public ZookeeperCredentials(String connectionString, String username, String password) {
      this.connectionString = connectionString;
      this.username = username;
      this.password = password;
    }

    public String getConnectionString() {
      return connectionString;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }
}
