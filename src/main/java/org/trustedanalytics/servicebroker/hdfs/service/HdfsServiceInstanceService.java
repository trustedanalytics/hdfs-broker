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
package org.trustedanalytics.servicebroker.hdfs.service;

import org.apache.hadoop.fs.Path;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceServiceStore;
import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.servicebroker.hdfs.util.HdfsPlanHelper;

import java.io.IOException;
import java.util.UUID;

public class HdfsServiceInstanceService extends ForwardingServiceInstanceServiceStore {

    private final HdfsClient hdfsClient;
    private final HdfsClient encryptedHdfsClient;
    private final String userspacePathTemplate;

    public HdfsServiceInstanceService(ServiceInstanceService instanceService,
                                      HdfsClient hdfsClient,
                                      HdfsClient encryptedHdfsClient,
                                      String userspacePathTemplate) {
        super(instanceService);
        this.hdfsClient = hdfsClient;
        this.encryptedHdfsClient = encryptedHdfsClient;
        this.userspacePathTemplate = userspacePathTemplate;
    }

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request)
            throws ServiceInstanceExistsException, ServiceBrokerException {

        ServiceInstance serviceInstance = super.createServiceInstance(request);

        if(!HdfsPlanHelper.isMultitenant(request.getPlanId())) {

            UUID instanceId = UUID.fromString(serviceInstance.getServiceInstanceId());
            UUID orgId = UUID.fromString(serviceInstance.getOrganizationGuid());

            provisionDirectory(instanceId, orgId);

            if (HdfsPlanHelper.isEncrypted(request.getPlanId())) {
                createEncryptedZone(instanceId, orgId);
            }
        }
        return serviceInstance;
    }

    private void provisionDirectory(UUID instanceId, UUID orgId) throws ServiceBrokerException {
        try {
            String path = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
            hdfsClient.createDir(path);
        } catch (IOException e) {
            throw new ServiceBrokerException(
                    "Unable to provision directory for: " + instanceId, e);
        }
    }

    private void createEncryptedZone(UUID instanceId, UUID orgId) throws ServiceBrokerException {


        try {
          String path = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);
          encryptedHdfsClient.createKeyAndEncryptedZone(instanceId.toString(), new Path(path));
        } catch (IOException e) {
            throw new ServiceBrokerException(
                    "Unable to provision encrypted directory for: " + instanceId, e);
        }
    }
}
