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

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;

import lombok.Getter;
import org.trustedanalytics.servicebroker.hdfs.plans.provisioning.HdfsDirectoryProvisioningOperations;

public final class HdfsBrokerInstancePath {
  private static final String UUID_REGEX = "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}";
  private static final String INSTANCE = "instance";
  private static final String NAMESPACE = "namespace";
  private static final String ORG = "org";
  private static final String DATASET = "dataset";
  private static final String HDFS_URI_REGEX = String.format(
      "^hdfs://(?<%s>\\w+)/org/(?<%s>%s)/brokers/userspace/(?<%s>%s)/", NAMESPACE, ORG, UUID_REGEX,
      INSTANCE, UUID_REGEX);
  private static final String UPLOADER_URI_REGEX = String.format("%s(?<%s>%s/)?", HDFS_URI_REGEX,
      DATASET, UUID_REGEX);

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

  public static Optional<HdfsBrokerInstancePath> getUploaderPath(String hdfsUri)
      throws ServiceBrokerException {
    Matcher matcher = validatePattern(hdfsUri, UPLOADER_URI_REGEX);
    HdfsBrokerInstancePath hdfsBrokerInstancePath = null;
    if (matcher.group(DATASET) != null) {
      hdfsBrokerInstancePath =
          new HdfsBrokerInstancePath(matcher.group(), matcher.group(NAMESPACE),
              UUID.fromString(matcher.group(ORG)), UUID.fromString(matcher.group(INSTANCE)));
    }
    return Optional.ofNullable(hdfsBrokerInstancePath);
  }

  public static HdfsBrokerInstancePath getInstancePath(String hdfsUri)
      throws ServiceBrokerException {
    Matcher matcher = validatePattern(hdfsUri, HDFS_URI_REGEX);
    return new HdfsBrokerInstancePath(matcher.group(), matcher.group(NAMESPACE),
        UUID.fromString(matcher.group(ORG)), UUID.fromString(matcher.group(INSTANCE)));
  }

  private static Matcher validatePattern(String hdfsUri, String regex)
      throws ServiceBrokerException {
    Matcher matcher = Pattern.compile(regex).matcher(hdfsUri);
    if (!matcher.find()) {
      throw new ServiceBrokerException(
          "Invalid hdfs path, doesn't match template: hdfs://<namespace>/org/<uuid>/brokers/userspace/<uuid>/ - "
              + hdfsUri);
    }
    return matcher;
  }
}
