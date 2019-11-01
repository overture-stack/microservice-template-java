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

package io.kf.coordinator.config;

import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import io.kf.coordinator.jwt.JWTAuthorizationFilter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Slf4j
@EnableWebSecurity
@Profile("!test")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${auth0.apiAudience}")
  private String audience;

  @Value("${auth0.issuer}")
  private String issuer;

  @Override
  @SneakyThrows
  public void configure(HttpSecurity http) {
    JwtWebSecurityConfigurer
            .forRS256(audience, issuer)
            .configure(http)
            .authorizeRequests()
            .antMatchers("/swagger**", "/swagger-resources/**", "/v2/api**", "/webjars/**").permitAll()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST).authenticated()
            .and()
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .addFilterAfter(new JWTAuthorizationFilter(), BasicAuthenticationFilter.class);

  }

}
