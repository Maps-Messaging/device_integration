/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegisterMap {

  private final Map<Integer, Register> map;

  public RegisterMap() {
    map = new LinkedHashMap<>();
  }

  public void addRegister(Register register) {
    if (map.containsKey(register.getAddress())) {
      Register existing = map.get(register.getAddress());
      throw new RuntimeException("Register address collision for address " + register.getAddress() + " Existing:" + existing.name + " New:" + register.name);
    }
    map.put(register.getAddress(), register);
  }

  public Map<Integer, RegisterData> getData() throws IOException {
    Map<Integer, RegisterData> data = new LinkedHashMap<>();
    for (Map.Entry<Integer, Register> entry : map.entrySet()) {
      RegisterData res = entry.getValue().toData();
      if (res != null) {
        data.put(entry.getKey(), entry.getValue().toData());
      }
    }
    return data;
  }

  public void setData(Map<Integer, RegisterData> update) throws IOException {
    for (Map.Entry<Integer, RegisterData> entry : update.entrySet()) {
      Register register = map.get(entry.getKey());
      if (register != null) {
        register.fromData(entry.getValue());
      }
    }
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    List<Integer> addressList = new ArrayList<>(map.keySet());
    addressList.sort(Integer::compareTo);
    int maxLength = map.values().stream()
        .mapToInt(register -> register.getName().length())
        .max()
        .orElse(0);


    for (Integer address : addressList) {
      Register register = map.get(address);
      stringBuilder.append("\t").append(register.toString(maxLength)).append("\n");
    }
    return stringBuilder.toString();
  }
}
