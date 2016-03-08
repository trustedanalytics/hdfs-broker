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
package org.trustedanalytics.servicebroker.hdfs.plans.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.trustedanalytics.cfbroker.store.hdfs.helper.DirHelper;
import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.servicebroker.framework.Credentials;
import org.trustedanalytics.servicebroker.hdfs.config.HdfsConstants;

@Component
class HdfsBindingClient
    implements HdfsBareBindingOperations, HdfsSimpleBindingOperations, HdfsSpecificOrgBindingOperations {

  private final Credentials credentials;
  private final String userspacePathTemplate;

  @Autowired
  public HdfsBindingClient(Credentials credentials, String userspacePathTemplate) {
    this.credentials = credentials;
    this.userspacePathTemplate = userspacePathTemplate;
  }

  @Override
  public Map<String, Object> createCredentialsMap() {
    return credentials.getCredentialsMap();
  }

  @Override
  public Map<String, Object> createCredentialsMap(UUID instanceId) {
    return createCredentialsMap(instanceId, null);
  }

  @Override
  public Map<String, Object> createCredentialsMap(UUID instanceId, UUID orgId) {
    Map<String, Object> credentialsCopy = new HashMap<>(credentials.getCredentialsMap());

    String dir = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
    String uri = DirHelper.concat(credentialsCopy.get(HdfsConstants.HADOOP_DEFAULT_FS).toString(), dir);
    uri = DirHelper.removeTrailingSlashes(uri) + "/";

    credentialsCopy.put("uri", uri);
    return credentialsCopy;
  }
}
