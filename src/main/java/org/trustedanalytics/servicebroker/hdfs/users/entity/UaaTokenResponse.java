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
package org.trustedanalytics.servicebroker.hdfs.users.entity;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public final class UaaTokenResponse {
  private final String jti;
  private final String scope;
  private final Integer expires;
  private final String token_type;
  private final String access_token;

  @JsonCreator
  public UaaTokenResponse(@JsonProperty("jti") String jti,
                          @JsonProperty("scope") String scope,
                          @JsonProperty("expires_in") Integer expires,
                          @JsonProperty("token_type") String token_type,
                          @JsonProperty("access_token") String access_token){
    this.jti = jti;
    this.scope = scope;
    this.expires = expires;
    this.token_type = token_type;
    this.access_token = access_token;
  }
}
