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

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.cfbroker.config.HadoopZipConfiguration;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;
import org.trustedanalytics.servicebroker.hdfs.config.kerberos.KerberosProperties;
import org.trustedanalytics.servicebroker.hdfs.service.HdfsServiceInstanceBindingService;

import sun.security.krb5.KrbException;

@Profile(Profiles.CLOUD)
@org.springframework.context.annotation.Configuration
public class HdfsConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(HdfsConfiguration.class);

  private static final String AUTHENTICATION_METHOD = "kerberos";

  private static final String AUTHENTICATION_METHOD_PROPERTY = "hadoop.security.authentication";

  private static final String KEYTAB_FILE_PATH = "/tmp/superuser.keytab";

  @Autowired
  private KerberosProperties kerberosProperties;

  @Autowired
  private ExternalConfiguration configuration;

  @Bean
  @Qualifier(Qualifiers.USER)
  public FileSystem getUserFileSystem() throws InterruptedException, URISyntaxException,
      LoginException, IOException {
    if (isKerberosEnabled()) {
      return getUserSecureFileSystem();
    } else {
      return getInsecureFileSystem(kerberosProperties.getUser());
    }
  }

  @Bean
  @Qualifier(Qualifiers.SUPER_USER)
  public FileSystem getAdminFileSystem() throws InterruptedException, URISyntaxException,
      LoginException, IOException, KrbException {
    if (isKerberosEnabled()) {
      return getAdminSecureFileSystem();
    } else {
      return getInsecureFileSystem(configuration.getHdfsSuperuser());
    }
  }

  private FileSystem getUserSecureFileSystem() throws InterruptedException, URISyntaxException,
      LoginException, IOException {
    LOGGER.info("Trying kerberos authentication");
    KrbLoginManager loginManager =
        KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
            kerberosProperties.getKdc(), kerberosProperties.getRealm());

    Configuration hadoopConf = getHadoopConfiguration();
    loginManager.loginInHadoop(loginManager.loginWithCredentials(kerberosProperties.getUser(),
        kerberosProperties.getPassword().toCharArray()), hadoopConf);
    return getFileSystemForUser(hadoopConf, kerberosProperties.getUser());
  }

  private FileSystem getAdminSecureFileSystem() throws InterruptedException, URISyntaxException,
      LoginException, IOException, KrbException {
    byte[] keytabFile = Base64.decodeBase64(configuration.getHdfsSuperuserKeytab());
    try (FileOutputStream fileOutputStream = new FileOutputStream(KEYTAB_FILE_PATH)) {
      fileOutputStream.write(keytabFile);
    }
    KrbLoginManager loginManager =
        KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
            kerberosProperties.getKdc(), kerberosProperties.getRealm());

    Configuration hadoopConf = getHadoopConfiguration();
    loginManager.loginInHadoop(
        loginManager.loginWithKeyTab(configuration.getHdfsSuperuser(), KEYTAB_FILE_PATH),
        hadoopConf);
    return getFileSystemForUser(hadoopConf, configuration.getHdfsSuperuser());
  }

  private FileSystem getInsecureFileSystem(String user) throws InterruptedException,
      URISyntaxException, LoginException, IOException {
    Configuration hadoopConf = getHadoopConfiguration();
    return getFileSystemForUser(hadoopConf, user);
  }

  private FileSystem getFileSystemForUser(Configuration config, String user)
      throws URISyntaxException, IOException, InterruptedException {
    LOGGER.info("Creating filesytem with for user: " + user);
    return FileSystem.get(
        new URI(config.getRaw(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS)), config, user);
  }

  private boolean isKerberosEnabled() throws LoginException, IOException {
    return AUTHENTICATION_METHOD.equals(getHadoopConfiguration()
        .get(AUTHENTICATION_METHOD_PROPERTY));
  }

  private Configuration getHadoopConfiguration() throws LoginException, IOException {
    return HadoopZipConfiguration.createHadoopZipConfiguration(configuration.getHdfsProvidedZip())
        .getAsHadoopConfiguration();
  }
}
