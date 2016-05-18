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
package org.trustedanalytics.servicebroker.hdfs.path;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;

public final class HdfsBrokerInstancePath {
  private static final String UUID_REGEX = "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}";
  private static final String INSTANCE = "instance";
  private static final String NAMESPACE = "namespace";
  private static final String ORG = "org";
  private static final String HDFS_URI_REGEX = String.format(
      "^hdfs://(?<%s>\\w+)/org/(?<%s>%s)/brokers/userspace/(?<%s>%s)/", NAMESPACE, ORG, UUID_REGEX,
      INSTANCE, UUID_REGEX);

  @Getter
  private final String hdfsUri;
  @Getter
  private final UUID orgId;
  @Getter
  private final UUID instanceId;
  @Getter
  private final String namespace;

  private HdfsBrokerInstancePath(String hdfsUri, String namespace, UUID orgId, UUID instanceId) {
    this.hdfsUri = hdfsUri;
    this.orgId = orgId;
    this.instanceId = instanceId;
    this.namespace = namespace;
  }

  public static HdfsBrokerInstancePath createInstance(String hdfsUri) throws ServiceBrokerException {
    Matcher matcher = Pattern.compile(HDFS_URI_REGEX).matcher(hdfsUri);
    if (!matcher.find()) {
      throw new ServiceBrokerException(
          "Invalid hdfs path, doesn't match template: hdfs://<namespace>/org/<uuid>/brokers/userspace/<uuid>/ - "
              + hdfsUri);
    }
    return new HdfsBrokerInstancePath(hdfsUri, matcher.group(NAMESPACE), UUID.fromString(matcher
        .group(ORG)), UUID.fromString(matcher.group(INSTANCE)));
  }

}
