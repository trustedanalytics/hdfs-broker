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
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.trustedanalytics.cfbroker.store.hdfs.helper.DirHelper;
import org.trustedanalytics.cfbroker.store.hdfs.helper.HdfsPathTemplateUtils;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceBindingServiceStore;
import org.trustedanalytics.cfbroker.store.impl.ServiceInstanceServiceStore;
import org.trustedanalytics.servicebroker.hdfs.util.HdfsPlanHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class HdfsServiceInstanceBindingService extends ForwardingServiceInstanceBindingServiceStore {

    public static final String HADOOP_DEFAULT_FS = "fs.defaultFS";

    private final Map<String, Object> credentials;
    private final ServiceInstanceService instanceService;
    private final String userspacePathTemplate;

    public HdfsServiceInstanceBindingService(ServiceInstanceBindingService instanceBindingService,
                                             Map<String, Object> credentials, ServiceInstanceService instanceService,
                                             String userspacePathTemplate) {
        super(instanceBindingService);
        this.credentials = credentials;
        this.instanceService = instanceService;
        this.userspacePathTemplate = userspacePathTemplate;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        ServiceInstance instance = instanceService.getServiceInstance(request.getServiceInstanceId());
        if(instance == null) {
            throw new ServiceBrokerException(String.format("Service instance not found: [%s]",
                    request.getServiceInstanceId()));
        }
        return withCredentials(super.createServiceInstanceBinding(request), instance);
    }


    private ServiceInstanceBinding withCredentials(ServiceInstanceBinding serviceInstanceBinding, ServiceInstance instance) {
        return new ServiceInstanceBinding(serviceInstanceBinding.getId(),
                serviceInstanceBinding.getServiceInstanceId(),
                getCredentialsFor(instance),
                serviceInstanceBinding.getSyslogDrainUrl(),
                serviceInstanceBinding.getAppGuid());
    }

    private Map<String, Object> getCredentialsFor(ServiceInstance instance) {
        Map<String, Object> credentialsCopy = new HashMap<>(credentials);

        UUID instanceId = UUID.fromString(instance.getServiceInstanceId());
        UUID orgId = !HdfsPlanHelper.isMultitenant(instance.getPlanId())
                ? UUID.fromString(instance.getOrganizationGuid()) : null;
        String dir = HdfsPathTemplateUtils.fill(userspacePathTemplate, instanceId, orgId);

        String uri = DirHelper.concat(credentialsCopy.get(HADOOP_DEFAULT_FS).toString(), dir);
        uri = DirHelper.removeTrailingSlashes(uri) + "/";
        credentialsCopy.put("uri", uri);

        return credentialsCopy;
    }

}
