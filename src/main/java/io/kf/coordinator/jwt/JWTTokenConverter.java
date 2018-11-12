/*
 * Copyright (c) 2017. The Ontario Institute for Cancer Research. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kf.coordinator.jwt;

import io.kf.coordinator.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class JWTTokenConverter extends JwtAccessTokenConverter {

  public JWTTokenConverter(String publicKey) {
    super();
    this.setVerifierKey(publicKey);

  }

  @Override
  public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
    OAuth2Authentication authentication = super.extractAuthentication(map);

    val jwtContext = new JWTDetails();
    val authContext = (Map<String, ?>)map.get("context");

    if (authContext.containsKey("user")){
      val user = (Map<String, ?>)authContext.get("user");
      val jwtUser = TypeUtils.convertType(user, JWTUser.class);
      jwtContext.setUser(Optional.of(jwtUser));
    }
    else if (authContext.containsKey("application")) {
      val application = (Map<String, ?>)authContext.get("application");
      val jwtApplication = TypeUtils.convertType(application, JWTApplication.class);
      jwtContext.setApplication(Optional.of(jwtApplication));
    }

    authentication.setDetails(jwtContext);
    return authentication;

  }

}
