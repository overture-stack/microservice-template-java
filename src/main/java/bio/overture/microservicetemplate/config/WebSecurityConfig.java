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

package bio.overture.microservicetemplate.config;

import bio.overture.microservicetemplate.jwt.JWTAuthorizationFilter;
import bio.overture.microservicetemplate.jwt.JWTTokenConverter;
import lombok.SneakyThrows;
import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@EnableWebSecurity
@EnableResourceServer
public class WebSecurityConfig extends ResourceServerConfigurerAdapter {

  @Autowired
  private ResourceLoader resourceLoader;

  @Value("${auth.jwt.publicKeyUrl}")
  private String publicKeyUrl;

  @Override
  @SneakyThrows
  public void configure(HttpSecurity http) {
    http
      .authorizeRequests()
        .antMatchers("/health").permitAll()
        .antMatchers("/isAlive").permitAll()
        .antMatchers("/upload/**").permitAll()
        .antMatchers("/download/**").permitAll()
        .antMatchers("/entities/**").permitAll()
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

  @Override
  public void configure(ResourceServerSecurityConfigurer config) {
    config.tokenServices(tokenServices());
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Bean
  @SneakyThrows
  public JwtAccessTokenConverter accessTokenConverter() {
    return new JWTTokenConverter(fetchJWTPublicKey());
  }


  @Bean
  @Primary
  public DefaultTokenServices tokenServices() {
    val defaultTokenServices = new DefaultTokenServices();
    defaultTokenServices.setTokenStore(tokenStore());
    return defaultTokenServices;
  }

  /**
   * Call EGO server for public key to use when verifying JWTs
   * Pass this value to the JWTTokenConverter
   */
  @SneakyThrows
  private String fetchJWTPublicKey() {
    val publicKeyResource = resourceLoader.getResource(publicKeyUrl);

    val stringBuilder = new StringBuilder();
    val reader = new BufferedReader(
      new InputStreamReader(publicKeyResource.getInputStream()));

    reader.lines().forEach(stringBuilder::append);
    return stringBuilder.toString();
  }

}
