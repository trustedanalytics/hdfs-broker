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
        return defaultUserAcl(TestUtil.HIVE_USER);
    }

    public static AclEntry hiveUserAcl(){
        return userAcl(TestUtil.HIVE_USER);
    }

    public static AclEntry defaultUserAcl(String user){
        return build(user).setScope(AclEntryScope.DEFAULT).build();
    }

    public static AclEntry userAcl(String user){
        return build(user).setScope(AclEntryScope.ACCESS).build();
    }

    private static AclEntry.Builder build(String user){
        return new AclEntry.Builder()
            .setType(AclEntryType.GROUP)
            .setPermission(FsAction.ALL)
            .setName(user);
    }
}
