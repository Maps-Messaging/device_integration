/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.pn532;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.BooleanSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Pn532Sensor extends I2CDevice implements Sensor {

  // I2C 7-bit address from DFRobot: (0x48 >> 1) = 0x24
  public static final int I2C_ADDR = 0x24;
  private static final int DATA_PORT = 0x00;

  // PN532 frame constants
  private static final byte PREAMBLE = 0x00;
  private static final byte STARTCODE1 = 0x00;
  private static final byte STARTCODE2 = (byte) 0xFF;
  private static final byte POSTAMBLE = 0x00;
  private static final byte HOST_TO_PN = (byte) 0xD4;
  private static final byte PN_TO_HOST = (byte) 0xD5;

  // Commands
  private static final byte CMD_SAM_CONFIGURATION = 0x14;
  private static final byte CMD_INLIST_PASSIVE = 0x4A;
  private static final byte CMD_INDATA_EXCHANGE = 0x40;

  // Card commands
  private static final byte CARD_READ = 0x30;       // MIFARE/NTAG read
  private static final byte CARD_WRITE_CLASSIC = (byte) 0xA0; // MIFARE Classic write 16B
  private static final byte CARD_WRITE_NTAG = (byte) 0xA2; // NTAG/Ultralight write 4B
  private static final byte CARD_AUTH_A = 0x60;       // MIFARE Classic auth A

  private static final byte[] ACK = new byte[]{0x00, 0x00, (byte) 0xFF, 0x00, (byte) 0xFF, 0x00};

  // State exposed as readings
  @Getter
  private volatile boolean tagPresent = false;
  @Getter
  private volatile String lastUid = "";

  private final List<SensorReading<?>> readings;
  private volatile boolean connected = false;

  public Pn532Sensor(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Pn532Sensor.class));
    readings = List.of(
        new BooleanSensorReading("tag_present", "", "True when a tag is detected", true, true, this::isTagPresent),
        new StringSensorReading("last_uid", "", "Last detected tag UID (hex)", "", true, this::getLastUid)
    );
  }

  /**
   * Call once after mount.
   */
  public void begin() throws IOException {
    call(new byte[]{CMD_SAM_CONFIGURATION, 0x01, 0x14, 0x01}, 16); // normal, timeout, use IRQ
    connected = true;
  }

  /**
   * Poll once for ISO14443A; updates readings, returns UID hex or "" if none.
   */
  public String poll() throws IOException {
    byte[] payload = call(new byte[]{CMD_INLIST_PASSIVE, 0x01, 0x00}, 40); // MaxTg=1, 106 kbps
    if (payload.length < 6) {
      tagPresent = false;
      return "";
    }

    int nbTargets = payload[0] & 0xFF;
    if (nbTargets < 1) {
      tagPresent = false;
      return "";
    }

    // Layout: NbTg, Tg, SENS_RES(2), SEL_RES, NFCIDLen, NFCID...
    int uidLen = payload[5] & 0xFF;
    int uidStart = 6;
    if ((uidLen != 4 && uidLen != 7 && uidLen != 10) || uidStart + uidLen > payload.length) {
      tagPresent = false;
      return "";
    }
    byte[] uid = Arrays.copyOfRange(payload, uidStart, uidStart + uidLen);
    lastUid = toHex(uid);
    tagPresent = true;
    return lastUid;
  }

  /**
   * Convenience: returns current UID (poll inside).
   */
  public String getUid() throws IOException {
    return poll();
  }

  // --- NTAG/Ultralight simple ops ---

  /**
   * READ (0x30): returns 16 bytes (4 pages).
   */
  public byte[] readNTAG(int page) throws IOException {
    if (page < 0 || page > 231) throw new IOException("page out of range");
    ensureTag();
    byte[] p = call(new byte[]{CMD_INDATA_EXCHANGE, 0x01, CARD_READ, (byte) page}, 32);
    if (p.length < 18 || p[0] != 0x41 || p[1] != 0x00) throw new IOException("NTAG read error");
    return Arrays.copyOfRange(p, 2, 18);
  }

  /**
   * Helper: return only the first 4 bytes (the requested page).
   */
  public byte[] readNTAGPage4(int page) throws IOException {
    byte[] sixteen = readNTAG(page);
    return Arrays.copyOfRange(sixteen, 0, 4);
  }

  /**
   * WRITE (0xA2): write 4 bytes to a page.
   */
  public void writeNTAG(int page, byte[] data4) throws IOException {
    if (page < 4 || page > 225) throw new IOException("page out of range");
    if (data4 == null || data4.length != 4) throw new IOException("need 4 bytes");
    ensureTag();
    byte[] cmd = new byte[]{CMD_INDATA_EXCHANGE, 0x01, CARD_WRITE_NTAG, (byte) page,
        data4[0], data4[1], data4[2], data4[3]};
    call(cmd, 16);
  }

  // --- MIFARE Classic 16-byte block ops (Auth A with FF..FF) ---

  public byte[] readMifareBlock(int block) throws IOException {
    if (block < 0 || block > 255) throw new IOException("block out of range");
    ensureTag();
    if (!mifareAuthA(block)) throw new IOException("auth failed");
    byte[] p = call(new byte[]{CMD_INDATA_EXCHANGE, 0x01, CARD_READ, (byte) block}, 40);
    if (p.length < 18 || p[0] != 0x41 || p[1] != 0x00) throw new IOException("block read error");
    return Arrays.copyOfRange(p, 2, 18);
  }

  public void writeMifareBlock(int block, byte[] data16) throws IOException {
    if (block < 0 || block > 255) throw new IOException("block out of range");
    if (data16 == null || data16.length != 16) throw new IOException("need 16 bytes");
    // optional safety: avoid trailer blocks (every 4th)
    if (block % 4 == 3) throw new IOException("refusing to write trailer block");
    ensureTag();
    if (!mifareAuthA(block)) throw new IOException("auth failed");
    byte[] p = new byte[20];
    p[0] = CMD_INDATA_EXCHANGE;
    p[1] = 0x01;
    p[2] = CARD_WRITE_CLASSIC;
    p[3] = (byte) block;
    System.arraycopy(data16, 0, p, 4, 16);
    call(p, 24);
  }

  // --- Sensor impl ---

  @Override
  public String getName() {
    return "PN532";
  }

  @Override
  public String getDescription() {
    return "PN532 NFC Reader (I2C)";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public List<SensorReading<?>> getReadings() {
    return readings;
  }

  @Override
  public boolean isConnected() {
    if (connected) return true;
    try {
      begin();
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  // --- Low-level PN532 framing over I2CDevice ---

  private byte[] call(byte[] cmdData, int maxResp) throws IOException {
    sendCommandFrame(cmdData);
    readAck();
    return readResponseFrame(cmdData[0], maxResp);
  }

  private void sendCommandFrame(byte[] data) throws IOException {
    int len = 1 + data.length; // TFI + data
    byte lcs = (byte) (0x100 - len);
    byte sum = HOST_TO_PN;

    write(DATA_PORT, PREAMBLE);
    write(DATA_PORT, STARTCODE1);
    write(DATA_PORT, STARTCODE2);
    write(DATA_PORT, (byte) len);
    write(DATA_PORT, lcs);
    write(DATA_PORT, HOST_TO_PN);
    for (byte b : data) {
      write(DATA_PORT, b);
      sum += b;
    }
    byte dcs = (byte) (0x100 - (sum & 0xFF));
    write(DATA_PORT, dcs);
    write(DATA_PORT, POSTAMBLE);
  }

  private void readAck() throws IOException {
    // Read up to 7 bytes; PN532 I2C often prefixes a 0x01 status
    byte[] pre = readBytes(7, 100);
    int start = (pre[0] == 0x01) ? 1 : 0;

    // ensure we have 6 bytes from 'start'
    if (pre.length - start < 6) {
      byte[] need = readBytes(6 - (pre.length - start), 50);
      byte[] merged = new byte[start + 6];
      System.arraycopy(pre, start, merged, 0, pre.length - start);
      System.arraycopy(need, 0, merged, pre.length - start, need.length);
      pre = merged;
      start = 0;
    }

    for (int i = 0; i < 6; i++) {
      if (pre[start + i] != ACK[i]) throw new IOException("PN532 no-ACK");
    }
  }

  private byte[] readResponseFrame(byte expectCmd, int max) throws IOException {
    // Header (maybe leading status 0x01)
    byte[] hdr = readBytes(7, 500); // try to include possible status + 6 header bytes
    int ofs = (hdr[0] == 0x01) ? 1 : 0;
    if (ofs + 5 >= hdr.length) { // not enough header yet → read remaining
      byte[] more = readBytes(ofs + 6 - hdr.length, 100);
      byte[] merged = new byte[hdr.length + more.length];
      System.arraycopy(hdr, 0, merged, 0, hdr.length);
      System.arraycopy(more, 0, merged, hdr.length, more.length);
      hdr = merged;
    }

    if (hdr[ofs] != 0x00 || hdr[ofs + 1] != 0x00 || hdr[ofs + 2] != (byte) 0xFF) throw new IOException("Bad preamble");
    int len = hdr[ofs + 3] & 0xFF;
    if (((hdr[ofs + 3] + hdr[ofs + 4]) & 0xFF) != 0x00) throw new IOException("LEN/LCS mismatch");
    if (len + 2 > max) throw new IOException("Resp too long");

    // Payload (TFI+CMD+DATA) + DCS + POST
    byte[] body = readBytes(len + 2, 500);
    if (body.length < len + 2) throw new IOException("Short body");

    byte tfi = body[0];
    byte cmd = body[1];
    if (tfi != PN_TO_HOST || cmd != (byte) (expectCmd + 1)) throw new IOException("TFI/CMD mismatch");

    byte sum = 0;
    for (int i = 0; i < len - 1; i++) sum += body[i];
    byte dcs = body[len - 1];
    if (((sum + dcs) & 0xFF) != 0x00) throw new IOException("DCS mismatch");

    if (body[len] != 0x00) throw new IOException("Bad postamble");

    return Arrays.copyOfRange(body, 2, len); // strip TFI,CMD; return DATA
  }

  private void ensureTag() throws IOException {
    if (poll().isEmpty()) throw new IOException("no tag present");
  }

  private boolean mifareAuthA(int block) throws IOException {
    byte[] key = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    String uidHex = poll();
    if (uidHex.length() < 8) return false;
    byte[] uid = hexToBytes(uidHex);

    byte[] p = new byte[4 + 6 + 4];
    p[0] = CMD_INDATA_EXCHANGE;
    p[1] = 0x01;
    p[2] = CARD_AUTH_A;
    p[3] = (byte) block;
    System.arraycopy(key, 0, p, 4, 6);
    System.arraycopy(uid, 0, p, 10, 4);

    byte[] resp = call(p, 16);
    return resp.length >= 2 && resp[0] == 0x41 && resp[1] == 0x00;
  }

  private byte[] readBytes(int n, int timeoutMs) throws IOException {
    byte[] out = new byte[n];
    int off = 0;
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (off < n) {
      int r = readRegister(DATA_PORT, out, off, n - off);
      if (r > 0) {
        off += r;
      } else if (System.currentTimeMillis() > deadline) {
        throw new IOException("I2C timeout");
      } else {
        delay(2);
      }
    }
    return out;
  }

  private static String toHex(byte[] b) {
    StringBuilder sb = new StringBuilder(b.length * 2);
    for (byte v : b) sb.append(String.format(Locale.ROOT, "%02X", v));
    return sb.toString();
  }

  private static byte[] hexToBytes(String s) {
    int n = s.length() / 2;
    byte[] b = new byte[n];
    for (int i = 0; i < n; i++) b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
    return b;
  }
}