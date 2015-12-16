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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
public class ExternalConfiguration {

    @Value("${metadata.imageUrl}")
    @NotNull
    private String imageUrl;

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

    @Value("${cf.servicename}")
    @NotNull
    private String cfServiceName;

    @Value("${cf.serviceid}")
    @NotNull
    private String cfServiceId;

    @Value("${cf.baseId}")
    @NotNull
    private String cfBaseId;

    @Value("${hdfs.provided.zip}")
    private String hdfsProvidedZip;

    @Value("${hdfs.keytab}")
    @NotNull
    private String keytab;

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

    public String getKeytab() {
        return keytab;
    }

    public void setKeytab(String keytab) {
        this.keytab = keytab;
    }

    public String getHdfsProvidedZip() {
        return hdfsProvidedZip;
    }

    public void setHdfsProvidedZip(String hdfsProvidedZip) {
        this.hdfsProvidedZip = hdfsProvidedZip;
    }

}
