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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegisterMap {

  private Map<Integer, Register> registerMap;

  public RegisterMap() {
    registerMap = new LinkedHashMap<>();
  }

  public void addRegister(Register register) {
    if (registerMap.containsKey(register.getAddress())) {
      Register existing = registerMap.get(register.getAddress());
      throw new RuntimeException("Register address collision for address " + register.getAddress() + " Existing:" + existing.name + " New:" + register.name);
    }
    registerMap.put(register.getAddress(), register);
  }

  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    List<Integer> addressList = new ArrayList<>(registerMap.keySet());
    addressList.sort(Integer::compareTo);
    for (Integer address : addressList) {
      Register register = registerMap.get(address);
      stringBuilder.append("\t").append(register.getName()).append("::").append(register).append("\n");
    }
    return stringBuilder.toString();
  }
}
