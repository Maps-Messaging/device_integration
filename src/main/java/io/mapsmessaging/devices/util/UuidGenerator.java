package io.mapsmessaging.devices.util;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@SuppressWarnings("java:S6548") // yes it is a singleton
public class UuidGenerator {

  private static class Holder {
    private static final UuidGenerator INSTANCE = new UuidGenerator();
  }

  // Global access point to get the Singleton instance
  public static UuidGenerator getInstance() {
    return UuidGenerator.Holder.INSTANCE;
  }


  private final NameBasedGenerator namespaceGenerator;

  public UUID generateUuid(String name) {
    if(namespaceGenerator != null){
      return namespaceGenerator.generate(name);
    }
    return UUID.randomUUID();
  }

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
}
