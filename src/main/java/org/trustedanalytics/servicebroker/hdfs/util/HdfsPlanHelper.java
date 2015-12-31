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

import com.google.common.base.Strings;

public class HdfsPlanHelper {

    public static final String ENCRYPTED_KEY = "-encrypted";
    public static final String TEMPLATE_KEY = "-multitenant";

    private HdfsPlanHelper() {}

    public static boolean isEncrypted(String planId) {
        return Strings.nullToEmpty(planId).toLowerCase().contains(ENCRYPTED_KEY);
    }

    public static boolean isMultitenant(String planId) {
        return Strings.nullToEmpty(planId).toLowerCase().contains(TEMPLATE_KEY);
    }
}
