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

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.trustedanalytics.cfbroker.store.hdfs.service.HdfsClient;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceServiceStore;

import java.io.IOException;

public class HdfsServiceInstanceService extends ForwardingServiceInstanceServiceStore {

    private final HdfsClient userspaceHdfsClient;

    public HdfsServiceInstanceService(ServiceInstanceService instanceService,
        HdfsClient userspaceHdfsClient) {
        super(instanceService);
        this.userspaceHdfsClient = userspaceHdfsClient;
    }

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request)
        throws ServiceInstanceExistsException, ServiceBrokerException {
        ServiceInstance serviceInstance = super.createServiceInstance(request);
        provisionDirectory(serviceInstance.getServiceInstanceId(), request.getPlanId());
        return serviceInstance;
    }

    private void provisionDirectory(String serviceInstanceId, String plan) throws ServiceBrokerException {
        try {
            if(plan.contains("-encrypted")){
                userspaceHdfsClient.createEncryptedDir(serviceInstanceId);
            }
            else{
                userspaceHdfsClient.createDir(serviceInstanceId);
            }
        } catch (IOException e) {
            throw new ServiceBrokerException(
                "Unable to provision directory for: " + serviceInstanceId, e);
        }
    }

}
