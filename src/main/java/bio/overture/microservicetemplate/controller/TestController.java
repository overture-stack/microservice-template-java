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

package bio.overture.microservicetemplate.controller;

import bio.overture.microservicetemplate.jwt.JWTFacadeInterface;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

  @Autowired
  private JWTFacadeInterface jwtFacade;

  @GetMapping
  public String testGet() {
    val user = jwtFacade.getUser();
    val userName = user.isPresent() ? user.get().getFirstName() : "";

    return userName.isEmpty() ? "Hello there!" : "Good Morning " + userName + "!";
  }

  @PostMapping
  public String testPost() {
    val user = jwtFacade.getUser();
    val userName = user.isPresent() ? user.get().getFirstName() : "";

    return userName.isEmpty() ? "Greetings!" : "Good Afternoon " + userName + "!";
  }

}
