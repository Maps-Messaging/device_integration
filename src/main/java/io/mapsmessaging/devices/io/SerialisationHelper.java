/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;

import java.util.Map;

public class SerialisationHelper {

  private final ObjectMapper mapper;

  public SerialisationHelper() {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    SimpleModule module = new SimpleModule();
    module.addDeserializer(RegisterData.class, new RegisterDataDeserializer(mapper));
    mapper.registerModule(module);
  }

  public Map<Integer, RegisterData> deserialise(byte[] val) throws JsonProcessingException {
    RegisterDataWrapper wrapper2 = mapper.readValue(new String(val), RegisterDataWrapper.class);
    return wrapper2.getMap();
  }

  public byte[] serialise(Map<Integer, RegisterData> map) throws JsonProcessingException {
    RegisterDataWrapper wrapper = new RegisterDataWrapper(map);
    String json = mapper.writeValueAsString(wrapper);
    return json.getBytes();
  }

}
