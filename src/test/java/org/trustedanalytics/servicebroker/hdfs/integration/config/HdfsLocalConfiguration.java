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
package org.trustedanalytics.servicebroker.hdfs.integration.config;

import org.trustedanalytics.servicebroker.hdfs.config.ExternalConfiguration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

@Configuration
@Profile("integration-test")
public class HdfsLocalConfiguration {

    @Autowired
    private ExternalConfiguration externalConfiguration;

    @Bean
    public FileSystem getFileSystem() throws IOException, InterruptedException, URISyntaxException {
        File baseDir = new File("./target/hdfs/" + "testName").getAbsoluteFile();
        FileUtil.fullyDelete(baseDir);
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration(false);
        conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
        MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
        MiniDFSCluster cluster = builder.build();
        FileSystem fs = cluster.getFileSystem();

        tryMkdirOrThrowException(fs, externalConfiguration.getMetadataChroot());
        tryMkdirOrThrowException(fs, externalConfiguration.getUserspaceChroot());
        return fs;
    }

    private void tryMkdirOrThrowException(FileSystem fs, String path) throws IOException {
        if (!fs.mkdirs(new Path(path))) {
            throw new RuntimeException("Failure when try to create test root dir: " + path);
        }
    }
}
