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

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.hadoop.config.ConfigurationHelper;
import org.trustedanalytics.hadoop.config.ConfigurationHelperImpl;
import org.trustedanalytics.hadoop.config.PropertyLocator;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;
import org.trustedanalytics.servicebroker.hdfs.service.HdfsServiceInstanceBindingService;
import org.trustedanalytics.cfbroker.config.HadoopZipConfiguration;
import sun.security.krb5.KrbException;

import javax.security.auth.login.LoginException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Profile(Profiles.CLOUD)
@org.springframework.context.annotation.Configuration
public class HdfsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsConfiguration.class);

    private static final String AUTHENTICATION_METHOD = "kerberos";

    private static final String AUTHENTICATION_METHOD_PROPERTY = "hadoop.security.authentication";

    private static final String HDFS_PRINCIPAL = "hdfs";

    private static final String KEYTAB_FILE_PATH = "/tmp/superuser.keytab";

    private ConfigurationHelper confHelper = ConfigurationHelperImpl.getInstance();

    @Autowired
    private ExternalConfiguration configuration;

    @Bean
    @Qualifier(Qualifiers.USER)
    public FileSystem getUserFileSystem() throws InterruptedException,
        URISyntaxException, LoginException, IOException {
        if (isKerberosEnabled()) {
            return getUserSecureFileSystem();
        } else {
            return getInsecureFileSystem(getPropertyFromCredentials(PropertyLocator.USER));
        }
    }

    @Bean
    @Qualifier(Qualifiers.SUPER_USER)
    public FileSystem getAdminFileSystem() throws InterruptedException,
        URISyntaxException, LoginException, IOException, KrbException {
        if (isKerberosEnabled()) {
            return getAdminSecureFileSystem();
        } else {
            return getInsecureFileSystem(HDFS_PRINCIPAL);
        }
    }

    private FileSystem getUserSecureFileSystem() throws InterruptedException,
        URISyntaxException, LoginException, IOException {
        LOGGER.info("Trying kerberos auth");

        KrbLoginManager loginManager =
                KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
                        getPropertyFromCredentials(PropertyLocator.KRB_KDC),
                        getPropertyFromCredentials(PropertyLocator.KRB_REALM));

        Configuration hadoopConf = getHadoopConfiguration();
        loginManager.loginInHadoop(loginManager.loginWithCredentials(
                getPropertyFromCredentials(PropertyLocator.USER),
                getPropertyFromCredentials(PropertyLocator.PASSWORD).toCharArray()), hadoopConf);
        return getFileSystemForUser(hadoopConf, getPropertyFromCredentials(PropertyLocator.USER));
    }


    private FileSystem getAdminSecureFileSystem() throws InterruptedException,
        URISyntaxException, LoginException, IOException, KrbException {
        LOGGER.info("Trying kerberos auth for admin");

        byte[] keytabFile = Base64.decodeBase64(configuration.getKeytab());
        try (FileOutputStream fileOutputStream =
                     new FileOutputStream(KEYTAB_FILE_PATH)) {
            fileOutputStream.write(keytabFile);
        }
        KrbLoginManager loginManager =
                KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
                        getPropertyFromCredentials(PropertyLocator.KRB_KDC),
                        getPropertyFromCredentials(PropertyLocator.KRB_REALM));

        Configuration hadoopConf = getHadoopConfiguration();
        loginManager.loginInHadoop(loginManager.loginWithKeyTab(HDFS_PRINCIPAL, KEYTAB_FILE_PATH), hadoopConf);
        return getFileSystemForUser(hadoopConf, HDFS_PRINCIPAL);
    }

    /**
     * TODO: This method instead of configuration.getBrokerUserName() should have something like :
     * OAuthTicket.getUserName()
     */
    private FileSystem getInsecureFileSystem(String user) throws InterruptedException,
        URISyntaxException, LoginException, IOException {
        LOGGER.info("Creating filesytem without kerberos auth");

        Configuration hadoopConf = getHadoopConfiguration();
        return getFileSystemForUser(hadoopConf, user);
    }

    private FileSystem getFileSystemForUser(Configuration config, String user)
            throws URISyntaxException, IOException, InterruptedException {
        LOGGER.info("Creating filesytem with kerberos auth for user: " + user);
        return FileSystem.get(new URI(config.getRaw(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS)),
                config, user);
    }

    private boolean isKerberosEnabled() throws LoginException, IOException {
        return AUTHENTICATION_METHOD.equals(getHadoopConfiguration().get(AUTHENTICATION_METHOD_PROPERTY));
    }

    private String getPropertyFromCredentials(PropertyLocator property) throws IOException {
        return confHelper.getPropertyFromEnv(property)
                .orElseThrow(() -> new IllegalStateException(
                        property.name() + " not found in VCAP_SERVICES"));
    }

    private Configuration getHadoopConfiguration() throws LoginException, IOException {
        return HadoopZipConfiguration.createHadoopZipConfiguration(
            configuration.getHdfsProvidedZip()).getAsHadoopConfiguration();
    }
}
