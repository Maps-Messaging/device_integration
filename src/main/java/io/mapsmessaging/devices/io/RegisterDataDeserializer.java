/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.io;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;

import java.io.IOException;

public class RegisterDataDeserializer extends StdDeserializer<RegisterData> {

  private final ObjectMapper mapper;

  public RegisterDataDeserializer(ObjectMapper objectMapper) {
    super(RegisterData.class);
    mapper = objectMapper;
  }

  @Override
  public RegisterData deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return deserialize(node);
  }

  @Override
  public RegisterData deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                                          TypeDeserializer typeDeserializer) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return deserialize(node);
  }

  private RegisterData deserialize(JsonNode node) {
    String className = PackageNameProcessor.getInstance().getPackage(node.get("className").asText());
    try {
      Class<?> cls = Class.forName(className);
      return (RegisterData) mapper.treeToValue(node, cls);
    } catch (ClassNotFoundException | JsonProcessingException e) {
      throw new RuntimeException("Could not find class " + className, e);
    }
  }
}
