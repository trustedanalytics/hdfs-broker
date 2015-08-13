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
package org.trustedanalytics.servicebroker.hdfs.config;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalConfiguration {

    @Value("${metadata.imageUrl}")
    @NotNull
    private String imageUrl;

    @Value("${hdfs.brokerusername}")
    @NotNull
    private String brokerUserName;

    @Value("${hdfs.brokeruserpass}")
    @NotNull
    private String brokerUserPassword;

    @Value("${hdfs.binding.xattr}")
    @NotNull
    private String bindingXattr;

    @Value("${hdfs.instance.xattr}")
    @NotNull
    private String instanceXattr;

    @Value("${hdfs.metadata.chroot}")
    @NotNull
    private String metadataChroot;

    @Value("${hdfs.userspace.chroot}")
    @NotNull
    private String userspaceChroot;

    @Value("${kerberos.kdc}")
    @NotNull
    private String kerberosKdc;

    @Value("${kerberos.realm}")
    @NotNull
    private String kerberosRealm;

    @Value("${cf.servicename}")
    @NotNull
    private String cfServiceName;

    @Value("${cf.serviceid}")
    @NotNull
    private String cfServiceId;

    @Value("${cf.baseId}")
    @NotNull
    private String cfBaseId;

    @Value("${hdfs.provided.params}")
    private String hadoopProvidedParams;

    public String getBrokerUserName() {
        return brokerUserName;
    }

    public void setBrokerUserName(String brokerUserName) {
        this.brokerUserName = brokerUserName;
    }

    public String getBindingXattr() {
        return bindingXattr;
    }

    public void setBindingXattr(String bindingXattr) {
        this.bindingXattr = bindingXattr;
    }

    public String getInstanceXattr() {
        return instanceXattr;
    }

    public void setInstanceXattr(String instanceXattr) {
        this.instanceXattr = instanceXattr;
    }

    public String getMetadataChroot() {
        return metadataChroot;
    }

    public void setMetadataChroot(String metadataChroot) {
        this.metadataChroot = metadataChroot;
    }

    public String getUserspaceChroot() {
        return userspaceChroot;
    }

    public void setUserspaceChroot(String userspaceChroot) {
        this.userspaceChroot = userspaceChroot;
    }

    public String getKerberosKdc() {
        return kerberosKdc;
    }

    public void setKerberosKdc(String kerberosKdc) {
        this.kerberosKdc = kerberosKdc;
    }

    public String getKerberosRealm() {
        return kerberosRealm;
    }

    public void setKerberosRealm(String kerberosRealm) {
        this.kerberosRealm = kerberosRealm;
    }

    public String getCfServiceName() {
      return cfServiceName;
    }

    public void setCfServiceName(String cfServiceName) {
      this.cfServiceName = cfServiceName;
    }

    public String getCfServiceId() {
     return cfServiceId;
    }

    public void setCfServiceId(String cfServiceId) {
     this.cfServiceId = cfServiceId;
    }

    public String getHadoopProvidedParams() {
     return hadoopProvidedParams;
    }

    public void setHadoopProvidedParams(String hadoopProvidedParams) {
     this.hadoopProvidedParams = hadoopProvidedParams;
    }

    public String getBrokerUserPassword() {
       return brokerUserPassword;
    }

    public void setBrokerUserPassword(String brokerUserPassword) {
       this.brokerUserPassword = brokerUserPassword;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCfBaseId() {
        return cfBaseId;
    }

    public void setCfBaseId(String cfBaseId) {
        this.cfBaseId = cfBaseId;
    }
}
