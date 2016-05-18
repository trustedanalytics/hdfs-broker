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
package org.trustedanalytics.servicebroker.hdfs.config.hgm;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.kerberos.client.KerberosRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.trustedanalytics.servicebroker.hdfs.config.hgm.HgmConfiguration;

@Profile("kerberos")
@Configuration
public class HgmKerberosConfiguration {

  private static final String KEYTAB_FILE_PATH = "/tmp/hgm.keytab";

  @Autowired
  private HgmConfiguration configuration;

  @Bean
  @Qualifier("hgmRestTemplate")
  public RestTemplate getHgmKerberosRestClient() throws IOException {
    byte[] keytabFile = Base64.decodeBase64(configuration.getPrincipalKeyTab());
    try (FileOutputStream fileOutputStream = new FileOutputStream(KEYTAB_FILE_PATH)) {
      fileOutputStream.write(keytabFile);
    }
    return new KerberosRestTemplate(KEYTAB_FILE_PATH, configuration.getPrincipal());
  }
}
