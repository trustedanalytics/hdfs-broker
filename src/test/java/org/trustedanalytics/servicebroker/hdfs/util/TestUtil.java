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
package org.trustedanalytics.servicebroker.hdfs.util;


import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.FsAction;

public class TestUtil {
    public static final String HIVE_USER = "hive";

    public static AclEntry hiveDefaultUserAcl(){
        AclEntry.Builder builder = new AclEntry.Builder()
                .setType(AclEntryType.GROUP)
                .setPermission(FsAction.ALL)
                .setName(TestUtil.HIVE_USER);

        return builder.setScope(AclEntryScope.DEFAULT).build();
    }

    public static AclEntry hiveUserAcl(){
        AclEntry.Builder builder = new AclEntry.Builder()
                .setType(AclEntryType.GROUP)
                .setPermission(FsAction.ALL)
                .setName(TestUtil.HIVE_USER);
        return builder.setScope(AclEntryScope.ACCESS).build();
    }
}
