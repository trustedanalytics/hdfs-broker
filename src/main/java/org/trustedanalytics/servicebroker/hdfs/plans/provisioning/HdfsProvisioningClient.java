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
package org.trustedanalytics.servicebroker.hdfs.plans.provisioning;

import java.io.IOException;
import java.util.UUID;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.*;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;

@Component
class HdfsProvisioningClient implements HdfsDirectoryProvisioningOperations,
    HdfsPlanEncryptedDirectoryProvisioningOperations{

  private static final FsPermission FS_PERMISSION = new FsPermission(FsAction.ALL, FsAction.ALL,
      FsAction.NONE);
  private static final String TECH_GROUP_POSTFIX = "_sys";

  private final HdfsClient hdfsClient;
  private final HdfsClient superUserHdfsClient;
  private final String userspacePathTemplate;

  @Autowired
  public HdfsProvisioningClient(HdfsClient hdfsClient, HdfsClient encryptedHdfsClient,
      String userspacePathTemplate) {
    this.hdfsClient = hdfsClient;
    this.superUserHdfsClient = encryptedHdfsClient;
    this.userspacePathTemplate = userspacePathTemplate;
  }

  @Override
  public void provisionDirectory(UUID instanceId, UUID orgId) throws ServiceBrokerException {
    try {
      String path = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
      hdfsClient.createDir(path);
      hdfsClient.setPermission(path, FS_PERMISSION);
    } catch (IOException e) {
      throw new ServiceBrokerException("Unable to provision directory for: " + instanceId, e);
    }
  }

  @Override
  public void provisionDirectory(UUID instanceId, UUID orgId, UUID owner) throws ServiceBrokerException {
    this.provisionDirectory(instanceId, orgId);
    try {
      String path = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
      superUserHdfsClient.setOwner(path, owner.toString(), orgId.toString());
    } catch (IOException e) {
      throw new ServiceBrokerException("Unable to provision directory for: " + instanceId, e);
    }
  }

  @Override
  public void addSystemUsersGroupAcl(String path, UUID orgId) throws ServiceBrokerException {
    try {
      AclEntry.Builder builder = new AclEntry.Builder()
          .setType(AclEntryType.GROUP)
          .setPermission(FsAction.ALL)
          .setName(orgId.toString() + TECH_GROUP_POSTFIX);

      AclEntry systemDefaultUserAcl = builder.setScope(AclEntryScope.DEFAULT).build();
      AclEntry systemUserAcl = builder.setScope(AclEntryScope.ACCESS).build();

      superUserHdfsClient.addAclEntry(path, systemUserAcl);
      superUserHdfsClient.addAclEntry(path, systemDefaultUserAcl);
    } catch (IOException e) {
      throw new ServiceBrokerException("Unable to add system users groups ACL for path: " + path, e);
    }
  }

  @Override
  public void createEncryptedZone(UUID instanceId, UUID orgId) throws ServiceBrokerException {
    try {
      String path = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
      superUserHdfsClient.createKeyAndEncryptedZone(instanceId.toString(), new Path(path));
    } catch (IOException e) {
      throw new ServiceBrokerException(
          "Unable to provision encrypted directory for: " + instanceId, e);
    }
  }
}
