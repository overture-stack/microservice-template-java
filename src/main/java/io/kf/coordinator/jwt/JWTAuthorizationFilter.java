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

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Slf4j
public class JWTAuthorizationFilter extends GenericFilterBean {

  @Override
  @SneakyThrows
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    val authentication = SecurityContextHolder.getContext().getAuthentication();
    if(authentication != null) {

      DecodedJWT details = (DecodedJWT) authentication.getDetails();

      if(!validateTokenDetails(details)) {
        SecurityContextHolder.clearContext();
      }
    }

    chain.doFilter(request, response);
  }

  private boolean validateTokenDetails(@NonNull DecodedJWT jwtDetails) {
    Claim gty = jwtDetails.getClaim("gty");
    return !gty.isNull() && gty.asString().equals("client-credentials");
  }

}
