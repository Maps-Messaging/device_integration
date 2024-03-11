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
    return sendAndWaitForResponse(command);
  }

  protected void setValue(int val){
    byte[] buf = new byte[5];
    buf[0] = getCommand()[0];
    buf[1] = getCommand()[1];
    buf[2] = (byte)(val >> 8 & 0xff);
    buf[3] = (byte)(val & 0xff);
    buf[4] = generateCrc(buf, 2);
    sendAndWaitForResponse(buf);
  }

  protected byte[] sendAndWaitForResponse(byte[] buf){
    device.write(buf);
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
    if(generateCrc(response, 0) == response[2]){
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
   * @return The computed CRC value.
   */
  protected byte generateCrc(byte[] data, int start) {
    int currentByte;
    byte crc = CRC8_INIT;
    int crcBit;

    // calculates 8-Bit checksum with given polynomial
    for (currentByte = start; currentByte < (start + 2); ++currentByte) {
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
