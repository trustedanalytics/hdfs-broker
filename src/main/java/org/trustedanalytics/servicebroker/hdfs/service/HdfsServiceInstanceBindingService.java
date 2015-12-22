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

import com.google.common.base.Preconditions;
import org.trustedanalytics.cfbroker.store.hdfs.helper.DirHelper;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceBindingServiceStore;
import org.trustedanalytics.servicebroker.hdfs.config.ExternalConfiguration;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;

import java.util.HashMap;
import java.util.Map;


public class HdfsServiceInstanceBindingService extends ForwardingServiceInstanceBindingServiceStore {

    public static final String HADOOP_DEFAULT_FS = "fs.defaultFS";

    private ExternalConfiguration configuration;

    private final Map<String, Object> credentials;

    public HdfsServiceInstanceBindingService(ServiceInstanceBindingService instanceBindingService,
                                             Map<String, Object> credentials,
                                             ExternalConfiguration configuration) {
        super(instanceBindingService);
        this.credentials = credentials;
        this.configuration = configuration;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        return withCredentials(super.createServiceInstanceBinding(request));
    }


    private ServiceInstanceBinding withCredentials(ServiceInstanceBinding serviceInstanceBinding) {
        return new ServiceInstanceBinding(serviceInstanceBinding.getId(),
                serviceInstanceBinding.getServiceInstanceId(),
                getCredentialsFor(serviceInstanceBinding.getServiceInstanceId()),
                serviceInstanceBinding.getSyslogDrainUrl(),
                serviceInstanceBinding.getAppGuid());
    }

    Map<String, Object> getCredentialsFor(String serviceInstanceId) {
        Map<String, Object> credentialsCopy = new HashMap<>(credentials);
        Preconditions.checkArgument(configuration.getUserspaceChroot().startsWith("/"));

        String dir =
                DirHelper.removeTrailingSlashes(
                        credentialsCopy.get(HADOOP_DEFAULT_FS).toString())
                        + DirHelper.addLeadingSlash(DirHelper.removeTrailingSlashes(configuration
                        .getUserspaceChroot())) + "/" + serviceInstanceId + "/";
        credentialsCopy.put("uri", dir);
        return credentialsCopy;
    }

}
