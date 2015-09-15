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

import org.trustedanalytics.hadoop.config.ConfigurationHelper;
import org.trustedanalytics.hadoop.config.ConfigurationHelperImpl;
import org.trustedanalytics.hadoop.config.PropertyLocator;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;
import org.trustedanalytics.servicebroker.hdfs.service.HdfsServiceInstanceBindingService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;

@Profile(Profiles.CLOUD)
@org.springframework.context.annotation.Configuration
public class HdfsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsConfiguration.class);

    private static final String AUTHENTICATION_METHOD = "kerberos";

    private static final String AUTHENTICATION_METHOD_PROPERTY = "hadoop.security.authentication";

    private ConfigurationHelper confHelper = ConfigurationHelperImpl.getInstance();

    @Autowired
    private ExternalConfiguration configuration;

    @Autowired
    private Configuration hadoopConf;

    @Bean
    public FileSystem getFileSystem() throws InterruptedException,
            URISyntaxException, LoginException, IOException {

        if(AUTHENTICATION_METHOD.equals(hadoopConf.get(AUTHENTICATION_METHOD_PROPERTY))) {
            return getSecureFileSystem();
        } else {
            return getInsecureFileSystem();
        }
    }

    private FileSystem getSecureFileSystem() throws InterruptedException,
            URISyntaxException, LoginException, IOException {
        LOGGER.info("Trying kerberos auth");

        KrbLoginManager loginManager =
                KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
                        getPropertyFromCredentials(PropertyLocator.KRB_KDC),
                        getPropertyFromCredentials(PropertyLocator.KRB_REALM));

        loginManager.loginInHadoop(loginManager.loginWithCredentials(
                getPropertyFromCredentials(PropertyLocator.USER),
                getPropertyFromCredentials(PropertyLocator.PASSWORD).toCharArray()), hadoopConf);
        LOGGER.info("Creating filesytem with kerberos auth");
        return FileSystem.get(hadoopConf);
    }

    /**
     * TODO: This method instead of configuration.getBrokerUserName() should have something like :
     * OAuthTicket.getUserName()
     */
    private FileSystem getInsecureFileSystem() throws InterruptedException,
            URISyntaxException, LoginException, IOException {
        LOGGER.info("Creating filesytem without kerberos auth");
        return FileSystem.get(
            new URI(hadoopConf.getRaw(HdfsServiceInstanceBindingService.HADOOP_DEFAULT_FS)),
            hadoopConf, getPropertyFromCredentials(PropertyLocator.USER));
    }

    private String getPropertyFromCredentials(PropertyLocator property) throws IOException{
        return confHelper.getPropertyFromEnv(property)
                .orElseThrow(() -> new IllegalStateException(property.name() + " not found in VCAP_SERVICES"));
    }
}
