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

package io.kf.coordinator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.IGNORE_UNKNOWN;

public class TypeUtils {

  public static  <T> T convertType(Object fromObject, Class<T> tClass){
    val mapper = new ObjectMapper();
    mapper.configure(IGNORE_UNKNOWN, true);
    return mapper.convertValue(fromObject, tClass);
  }

}
