package io.mapsmessaging.server.i2c;

import java.util.concurrent.locks.LockSupport;

public class Delay {
  public static void pause(long delay) {
    LockSupport.parkNanos(delay * 1000000);
  }
}