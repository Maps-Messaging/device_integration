package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.impl.AddressableDevice;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

final class MotionStartupTestDevice implements AddressableDevice {

  final byte[] image = new byte[256];
  final Queue<byte[]> frames = new ArrayDeque<>();
  final List<byte[]> rawWrites = new ArrayList<>();
  boolean shortRead;
  int resets;
  @Override public void close() { }
  @Override
  public int getBus() {
    return 1;
  }
  @Override
  public int getDevice() {
    return 0x24;
  }
  @Override
  public int write(int value) {
    return 1;
  }
  @Override public int write(byte[] data, int offset, int length) {
    rawWrites.add(Arrays.copyOfRange(data, offset, offset + length));
    return length;
  }
  @Override public int writeRegister(int register, byte[] data) {
    System.arraycopy(data, 0, image, register, data.length);
    if (register == 0x21 && (data[0] & 4) != 0) {
      resets++;
    }
    if (register == 0x11 && (data[0] & 4) != 0) {
      image[0x10] = 0;
      image[0x11] = 0;
      image[0x27] = 0;
    }
    return data.length;
  }
  @Override public int read(byte[] data, int offset, int length) {
    byte[] frame = frames.poll();
    if (frame == null) {
      return -1;
    }
    int count = Math.min(frame.length, length);
    System.arraycopy(frame, 0, data, offset, count);
    return count;
  }
  @Override
  public int readRegister(int register) {
    return image[register] & 255;
  }
  @Override public int readRegister(int register, byte[] data, int offset, int length) {
    if (!frames.isEmpty()) {
      return read(data, offset, length);
    }
    int count = shortRead ? 1 : length;
    System.arraycopy(image, register, data, offset, count);
    return count;
  }
  @Override
  public int read() {
    return 1;
  }

  static byte[] ack() {
    return new byte[]{1, 0, 0, (byte) 255, 0, (byte) 255, 0};
  }

  static byte[] frame(int command, int... payload) {
    int length = payload.length + 2;
    byte[] frame = new byte[length + 8];
    frame[0] = 1;
    frame[3] = (byte) 255;
    frame[4] = (byte) length;
    frame[5] = (byte) -length;
    frame[6] = (byte) 213;
    frame[7] = (byte) command;
    int checksum = 213 + command;
    for (int index = 0; index < payload.length; index++) {
      frame[index + 8] = (byte) payload[index];
      checksum += payload[index];
    }
    frame[length + 6] = (byte) -checksum;
    return frame;
  }

}
