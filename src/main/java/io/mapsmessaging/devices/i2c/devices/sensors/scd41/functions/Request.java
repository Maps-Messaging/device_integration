package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class Request {
  private static final int CRC8_POLYNOMIAL = 0x31;
  private static final byte CRC8_INIT = (byte)0xFF;

  private final byte[] command;
  private final int msDelay;
  private final int responseLength;
  private final AddressableDevice device;

  protected Request(int delay, int command, int responseLength, AddressableDevice device ){
    this.device =device;
    msDelay = delay;
    this.responseLength = responseLength;
    this.command = new byte[2];
    this.command[0] = (byte)(command >> 8 & 0xff);
    this.command[1] = (byte)(command & 0xff);
  }


  public byte[] getResponse(){
    device.write(command);
    if(msDelay > 0) pause();
    byte[] response = new byte[responseLength];
    if(responseLength > 0) {
      device.read(response, 0, responseLength);
    }
    return response;
  }

  protected int readValue(){
    int value = Integer.MIN_VALUE;
    byte[] response = getResponse();
    if(generateCrc(response, 0, 2) == response[2]){
      value = response[0] << 8 | (response[1] & 0xff);
    }
    return value;

  }

  protected void pause(){
    try {
      TimeUnit.MILLISECONDS.sleep(msDelay);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Generates a CRC value for a subsection of a byte array.
   *
   * @param data The input data array.
   * @param start The starting index in the array to compute the CRC.
   * @param count The number of bytes in the array to include in the CRC computation.
   * @return The computed CRC value.
   */
  protected byte generateCrc(byte[] data, int start, int count) {
    int currentByte;
    byte crc = CRC8_INIT;
    int crcBit;

    // calculates 8-Bit checksum with given polynomial
    for (currentByte = start; currentByte < (start + count); ++currentByte) {
      crc ^= (data[currentByte]);
      for (crcBit = 8; crcBit > 0; --crcBit) {
        if ((crc & 0x80) != 0) {
          crc = (byte)((crc << 1) ^ CRC8_POLYNOMIAL);
        } else {
          crc = (byte)(crc << 1);
        }
      }
    }
    return crc;
  }
}
