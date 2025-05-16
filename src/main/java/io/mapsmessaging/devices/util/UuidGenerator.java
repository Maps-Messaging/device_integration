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

package io.mapsmessaging.devices.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@SuppressWarnings("java:S6548") // yes it is a singleton
public class UuidGenerator {

  private final NameBasedGenerator namespaceGenerator;

  private UuidGenerator() {
    NameBasedGenerator uuidGenerator;
    try {
      UUID uuid = UUID.fromString("59d723b0-45c2-4a4e-b337-f47e07abe25f");
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

      uuidGenerator = Generators.nameBasedGenerator(uuid, messageDigest);
    } catch (NoSuchAlgorithmException e) {
      uuidGenerator = null;
    }
    namespaceGenerator = uuidGenerator;
  }

  // Global access point to get the Singleton instance
  public static UuidGenerator getInstance() {
    return UuidGenerator.Holder.INSTANCE;
  }

  public UUID generateUuid(String name) {
    if (namespaceGenerator != null) {
      return namespaceGenerator.generate(name);
    }
    return UUID.randomUUID();
  }

  private static class Holder {
    private static final UuidGenerator INSTANCE = new UuidGenerator();
  }
}
