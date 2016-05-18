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

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
public class HgmConfiguration {

  @Value("${group.mapping.url}")
  @NotNull
  @Getter @Setter
  private String url;

  @Value("${group.mapping.https.username}")
  @Getter @Setter
  private String username;

  @Value("${group.mapping.https.password}")
  @Getter @Setter
  private String password;

  @Value("${group.mapping.kerberos.principal}")
  @Getter @Setter
  private String principal;

  @Value("${group.mapping.kerberos.principalKeyTab}")
  @Getter @Setter
  private String principalKeyTab;

}
