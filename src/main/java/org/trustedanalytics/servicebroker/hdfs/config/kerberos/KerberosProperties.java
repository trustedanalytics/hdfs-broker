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
package org.trustedanalytics.servicebroker.hdfs.config.kerberos;

import com.google.common.base.Strings;

public final class KerberosProperties {

  private String kdc = "";
  private String realm = "";
  private String user = "";
  private String password = "";

  public KerberosProperties(String kdc, String realm, String user, String password) {
    this.kdc = kdc;
    this.realm = realm;
    this.user = user;
    this.password = password;
  }

  public String getKdc() {
    return kdc;
  }

  public String getRealm() {
    return realm;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public boolean isValid() {
    if (Strings.isNullOrEmpty(kdc) || Strings.isNullOrEmpty(realm) ||
        Strings.isNullOrEmpty(user) || Strings.isNullOrEmpty(password)) {
      return false;
    }
    return true;
  }
}
