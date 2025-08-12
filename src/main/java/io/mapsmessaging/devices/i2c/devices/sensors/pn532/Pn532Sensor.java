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

  // DFRobot PN532 I2C 7-bit address: 0x48 >> 1 = 0x24
  public static final int I2C_ADDR = 0x24;
  private static final int DATA_PORT = 0x00;

  private static final byte PREAMBLE = 0x00;
  private static final byte STARTCODE1 = 0x00;
  private static final byte STARTCODE2 = (byte) 0xFF;
  private static final byte POSTAMBLE = 0x00;
  private static final byte HOST_TO_PN = (byte) 0xD4;
  private static final byte PN_TO_HOST = (byte) 0xD5;

  private static final byte CMD_SAM_CONFIGURATION = 0x14;
  private static final byte CMD_INLIST_PASSIVE = 0x4A;
  private static final byte CMD_INDATA_EXCHANGE = 0x40;

  private static final byte CARD_READ = 0x30;                 // READ (16B / 4 pages)
  private static final byte CARD_WRITE_CLASSIC = (byte) 0xA0; // Classic 16B write
  private static final byte CARD_WRITE_NTAG = (byte) 0xA2;    // NTAG/Ultralight 4B write
  private static final byte CARD_AUTH_A = 0x60;               // Mifare Classic auth (Key A)
  private static final int ACK_TIMEOUT_MS  = 1000;
  private static final int RESP_TIMEOUT_MS = 1000;
  private static final int SETTLE_MS = 30;

  private static final byte[] ACK = new byte[]{0x00, 0x00, (byte) 0xFF, 0x00, (byte) 0xFF, 0x00};


  private volatile boolean tagPresent = false;
  @Getter
  private volatile String lastUid = "";

  private volatile boolean connected = false;

  private byte[] mifareKey = new byte[]{(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF};

  private final List<SensorReading<?>> readings = List.of(
      new BooleanSensorReading("tag_present", "", "True when a tag is detected", true, true, this::isTagPresent),
      new StringSensorReading("last_uid", "", "Last detected tag UID (hex)", "", true, this::getLastUid)
  );

  public Pn532Sensor(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(Pn532Sensor.class));
    try {
      begin();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // ---------- Public Sensor API ----------

  @Override public String getName() { return "PN532"; }
  @Override public String getDescription() { return "PN532 NFC Reader (I2C)"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }
  @Override public List<SensorReading<?>> getReadings() { return readings; }

  @Override
  public boolean isConnected() {
    if (connected) return true;
    try {
      begin();
      return connected;
    } catch (IOException e) {
      return false;
    }
  }

  public boolean isTagPresent() {
    try { poll(); } catch (IOException ignore) {}
    return tagPresent;
  }

  public String getUid() throws IOException { return poll(); }

  public void setMifareKey(byte[] sixBytes) {
    if (sixBytes != null && sixBytes.length == 6) {
      this.mifareKey = Arrays.copyOf(sixBytes, 6);
    }
  }

  // ---------- Setup / Poll ----------

  public void begin() throws IOException {
    // SAMConfiguration: normal mode (1), timeout 0x14, use IRQ (1)
    call(new byte[]{CMD_SAM_CONFIGURATION, 0x01, 0x14, 0x01}, 16);
    // Expect command echo handled in readResponseFrame; arriving here means success
    connected = true;
  }

  /** PN532 sets a leading status byte 0x01 when data is ready on I2C. */
  private boolean isReadyOnce() throws IOException {
    byte[] b = new byte[1];
    int r = readRegister(DATA_PORT, b, 0, 1);
    return (r == 1) && (b[0] == 0x01);
  }

  /** Wait until PN532 indicates data ready (status 0x01) or timeout. */
  private void waitReady(int timeoutMs) throws IOException {
    long dl = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < dl) {
      if (isReadyOnce()) return;
      delay(2);
    }
    throw new IOException("PN532 not ready (timeout)");
  }

  /**
   * InListPassiveTarget(ISO14443A 106kbps). Updates tagPresent/lastUid.
   * Returns UID hex or "" when no tag.
   */
  public String poll() throws IOException {
    byte[] p = call(new byte[]{CMD_INLIST_PASSIVE, 0x01, 0x00}, 40);
    // Expected layout (DATA): NbTg, Tg, SENS_RES(2), SEL_RES, NFCIDLen, NFCID...
    if (p.length < 6) {
      tagPresent = false;
      lastUid = "";
      return "";
    }
    int nbTg = p[0] & 0xFF;
    if (nbTg < 1) {
      tagPresent = false;
      lastUid = "";
      return "";
    }
    int uidLen = p[5] & 0xFF;
    int uidStart = 6;
    if ((uidLen != 4 && uidLen != 7 && uidLen != 10) || uidStart + uidLen > p.length) {
      tagPresent = false;
      lastUid = "";
      return "";
    }
    byte[] uid = Arrays.copyOfRange(p, uidStart, uidStart + uidLen);
    lastUid = toHex(uid);
    tagPresent = true;
    return lastUid;
  }

  // ---------- NTAG / Ultralight ----------

  /** READ 0x30, returns 16 bytes (4 pages) for page 0..231 */
  public byte[] readNTAG(int page) throws IOException {
    if (page < 0 || page > 231) throw new IOException("page out of range");
    ensureTag();
    byte[] p = call(new byte[]{CMD_INDATA_EXCHANGE, 0x01, CARD_READ, (byte) page}, 32);
    // Expect status 0x41,0x00 then 16 bytes
    if (p.length < 18 || p[0] != 0x41 || p[1] != 0x00) throw new IOException("NTAG read error");
    return Arrays.copyOfRange(p, 2, 18);
  }

  /** Convenience: first 4 bytes of the 16-byte read */
  public byte[] readNTAGPage4(int page) throws IOException {
    return Arrays.copyOfRange(readNTAG(page), 0, 4);
  }

  /** WRITE 0xA2, page 4..225, exactly 4 bytes */
  public void writeNTAG(int page, byte[] data4) throws IOException {
    if (page < 4 || page > 225) throw new IOException("page out of range");
    if (data4 == null || data4.length != 4) throw new IOException("need 4 bytes");
    ensureTag();
    byte[] cmd = new byte[]{CMD_INDATA_EXCHANGE, 0x01, CARD_WRITE_NTAG, (byte) page,
        data4[0], data4[1], data4[2], data4[3]};
    call(cmd, 16);
  }

  /** Ultralight read: page 0..41, returns 4 bytes */
  public byte[] readUltralight(int page) throws IOException {
    if (page < 0 || page > 41) throw new IOException("page out of range");
    return Arrays.copyOfRange(readNTAG(page), 0, 4);
  }

  /** Ultralight write: page >= 4, 4 bytes */
  public void writeUltralight(int page, byte[] data4) throws IOException {
    if (page < 4) throw new IOException("page < 4 not writable");
    writeNTAG(page, data4);
  }



  // ---------- MIFARE Classic ----------

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
    // Arduino parity:
    // <128: reject trailer ((block+1)%4==0) OR block==0
    // 128..255: reject ((block+1)%16==0)
    if (block < 128) {
      if ((block + 1) % 4 == 0 || block == 0) throw new IOException("refusing to write trailer");
    } else {
      if ((block + 1) % 16 == 0) throw new IOException("refusing to write trailer");
    }
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

  private boolean mifareAuthA(int block) throws IOException {
    String uidHex = poll();
    if (uidHex.length() < 8) return false; // need at least 4 UID bytes
    byte[] uid = hexToBytes(uidHex);
    byte[] payload = new byte[4 + 6 + 4];
    payload[0] = CMD_INDATA_EXCHANGE;
    payload[1] = 0x01;
    payload[2] = CARD_AUTH_A;
    payload[3] = (byte) block;
    System.arraycopy(mifareKey, 0, payload, 4, 6);
    System.arraycopy(uid, 0, payload, 10, 4);
    byte[] resp = call(payload, 16);
    return resp.length >= 2 && resp[0] == 0x41 && resp[1] == 0x00;
  }

  // ---------- Card classification (Ultraversion probes) ----------

  public CardInfo getInformation() throws IOException {
    CardInfo ci = new CardInfo();

    byte[] p = call(new byte[]{CMD_INLIST_PASSIVE, 0x01, 0x00}, 40);
    if (p.length < 6 || (p[0] & 0xFF) < 1) return ci; // unknown

    ci.atqa0 = p[2];
    ci.atqa1 = p[3];
    ci.sak = p[4];
    int uidLen = p[5] & 0xFF;
    ci.uid = Arrays.copyOfRange(p, 6, 6 + Math.min(uidLen, Math.max(0, p.length - 6)));
    ci.uidLength = ci.uid.length;

    // NTAG/Ultralight identification chain
    boolean isTypeA = (ci.atqa0 == 0x00) && (ci.atqa1 == 0x44 || ci.atqa1 == 0x04 || ci.atqa1 == 0x02);
    if (isTypeA) {
      if (ci.atqa1 == 0x04) {
        ci.rfTech = "ISO/IEC14443-3,Type A";
        ci.cardType = "MIFARE Classic 1k";
        ci.manufacturer = "NXP";
        ci.sizeBytes = 1024; ci.userBytes = 752; ci.blockSize = 16; ci.blockNumber = 64; ci.sectorSize = 16;
        return ci;
      }
      if (ci.atqa1 == 0x02) {
        ci.rfTech = "ISO/IEC14443-3,Type A";
        ci.cardType = "MIFARE Classic 4k";
        ci.manufacturer = "NXP";
        ci.sizeBytes = 4096; ci.userBytes = 3440; ci.blockSize = 16; ci.blockNumber = 256; ci.sectorSize = 39;
        return ci;
      }

      // 0x44 → NTAG / Ultralight; probe pages
      if (getUltraversion(230)) {
        fillNTAG(ci, "NTAG 216", 924, 888, 4, 231, 1);
      } else if (getUltraversion(134)) {
        fillNTAG(ci, "NTAG 215", 540, 504, 4, 135, 1);
      } else if (getUltraversion(44)) {
        fillNTAG(ci, "NTAG 213", 180, 144, 4, 45, 1);
      } else if (getUltraversion(40)) {
        fillNTAG(ci, "Ultralight", 164, 128, 4, 41, 1);
      } else if (getUltraversion(19)) {
        fillNTAG(ci, "Ultralight", 80, 48, 4, 20, 1);
      } else if (getUltraversion(14)) {
        // Ultralight C (approx values used by Arduino example)
        fillNTAG(ci, "Ultralight", 164, 320, 4, 41, 1); // usersize 320 in Arduino code (likely typo); kept for parity
      }
    }
    return ci;
  }

  private void fillNTAG(CardInfo ci, String type, int size, int user, int blockSz, int blocks, int sectorSz) {
    ci.rfTech = "ISO/IEC14443-3,Type A";
    ci.cardType = type;
    ci.manufacturer = "NXP";
    ci.sizeBytes = size;
    ci.userBytes = user;
    ci.blockSize = blockSz;
    ci.blockNumber = blocks;
    ci.sectorSize = sectorSz;
  }

  /** Arduino-style "Ultraversion" probe: true if READ on page returns status 0x41,0x00 */
  public boolean getUltraversion(int page) throws IOException {
    ensureTag();
    byte[] p = call(new byte[]{CMD_INDATA_EXCHANGE, 0x01, CARD_READ, (byte) page}, 32);
    return p.length >= 2 && p[0] == 0x41 && p[1] == 0x00;
  }

  // Convenience like Arduino
  public boolean scan(String expectedUidHex) throws IOException {
    String got = poll();
    return !got.isEmpty() && got.equalsIgnoreCase(expectedUidHex);
  }

  public String readUid() {
    try { return poll(); } catch (IOException e) { return ""; }
  }

  // ---------- Low level ----------

  private byte[] call(byte[] cmdData, int maxResp) throws IOException {
    sendCommandFrame(cmdData);
    delay(SETTLE_MS);          // DFRobot no-IRQ path does ~30ms
    readAckNoIRQ();            // <-- new
    delay(SETTLE_MS);
    return readResponseFrameNoIRQ(cmdData[0], maxResp); // <-- new
  }
  private byte[] readResponseFrameNoIRQ(byte expectCmd, int max) throws IOException {
    // Stage 1: header-like chunk (DFRobot requests 8 and discards the first)
    byte[] head = readBytes(8, 1000);
    int ofs = 1; // discard first byte per DFRobot
    if (head.length < ofs + 6) throw new IOException("Short header");

    if (head[ofs] != 0x00 || head[ofs+1] != 0x00 || head[ofs+2] != (byte)0xFF)
      throw new IOException("Bad preamble");

    int len = head[ofs+3] & 0xFF;
    int lcs = head[ofs+4] & 0xFF;
    if (((len + lcs) & 0xFF) != 0x00) throw new IOException("LEN/LCS mismatch");
    if (len + 2 > max) throw new IOException("Resp too long");

    // Stage 2: body (DFRobot then requests x-4 more, discards one, then reads x-6)
    // In our case, just read the whole len+2 (payload+DCS+POST)
    byte[] body = readBytes(len + 2, 1000);
    if (body.length < len + 2) throw new IOException("Short body");

    byte tfi = body[0];
    byte cmd = body[1];
    if (tfi != PN_TO_HOST || cmd != (byte)(expectCmd + 1))
      throw new IOException("TFI/CMD mismatch");

    byte sum = 0;
    for (int i = 0; i < len - 1; i++) sum += body[i];
    if (((sum + body[len - 1]) & 0xFF) != 0x00) throw new IOException("DCS mismatch");
    if (body[len] != 0x00) throw new IOException("Bad postamble");

    return Arrays.copyOfRange(body, 2, len);  // DATA only
  }

  private void readAckNoIRQ() throws IOException {
    byte[] buf = readBytes(8, 1000);     // DFRobot reads 8, discards first
    // buf[0] is a dummy/status; compare next 6 to ACK
    for (int i = 0; i < 6; i++) {
      if (buf[1 + i] != ACK[i]) throw new IOException("PN532 no-ACK");
    }
    // If your platform batches more, it’s fine; we only needed the ACK.
  }


  private void sendCommandFrame(byte[] data) throws IOException {
    int len = 1 + data.length; // TFI + data
    byte lcs = (byte) (0x100 - len);
    byte sum = HOST_TO_PN;
    for (byte b : data) sum += b;
    byte dcs = (byte) (0x100 - (sum & 0xFF));

    byte[] buf = new byte[data.length + 8];
    buf[0] = PREAMBLE;
    buf[1] = STARTCODE1;
    buf[2] = STARTCODE2;
    buf[3] = (byte) len;
    buf[4] = lcs;
    buf[5] = HOST_TO_PN;
    System.arraycopy(data, 0, buf, 6, data.length);
    buf[buf.length - 2] = dcs;
    buf[buf.length - 1] = POSTAMBLE;
    write(DATA_PORT, buf);
  }

  private void readAck() throws IOException {
    byte[] pre = readBytes(7, 1000);           // may include a leading 0x01 status
    int start = (pre[0] == 0x01) ? 1 : 0;

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
    byte[] hdr = readBytes(7, 500);  // may include 0x01 status
    int ofs = (hdr[0] == 0x01) ? 1 : 0;
    if (ofs + 5 >= hdr.length) {
      byte[] more = readBytes(ofs + 6 - hdr.length, 100);
      byte[] merged = new byte[hdr.length + more.length];
      System.arraycopy(hdr, 0, merged, 0, hdr.length);
      System.arraycopy(more, 0, merged, hdr.length, more.length);
      hdr = merged;
    }

    if (hdr[ofs] != 0x00 || hdr[ofs + 1] != 0x00 || hdr[ofs + 2] != (byte) 0xFF)
      throw new IOException("Bad preamble");
    int len = hdr[ofs + 3] & 0xFF;
    if (((hdr[ofs + 3] + hdr[ofs + 4]) & 0xFF) != 0x00)
      throw new IOException("LEN/LCS mismatch");
    if (len + 2 > max) throw new IOException("Resp too long");

    byte[] body = readBytes(len + 2, 500); // (TFI+CMD+DATA) + DCS + POST
    if (body.length < len + 2) throw new IOException("Short body");

    byte tfi = body[0];
    byte cmd = body[1];
    if (tfi != PN_TO_HOST || cmd != (byte) (expectCmd + 1))
      throw new IOException("TFI/CMD mismatch");

    byte sum = 0;
    for (int i = 0; i < len - 1; i++) sum += body[i];
    byte dcs = body[len - 1];
    if (((sum + dcs) & 0xFF) != 0x00) throw new IOException("DCS mismatch");
    if (body[len] != 0x00) throw new IOException("Bad postamble");

    return Arrays.copyOfRange(body, 2, len); // DATA only
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

  private void ensureTag() throws IOException {
    if (poll().isEmpty()) throw new IOException("no tag present");
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

  // ---------- DTO ----------

  public static class CardInfo {
    public String rfTech = "";
    public String cardType = "";
    public String manufacturer = "";
    public int uidLength = 0;
    public byte[] uid = new byte[0];
    public byte atqa0;
    public byte atqa1;
    public byte sak;
    public int sizeBytes = 0;
    public int userBytes = 0;
    public int blockSize = 0;
    public int blockNumber = 0;
    public int sectorSize = 0;

    @Override public String toString() {
      return "CardInfo{type=" + cardType + ", uid=" + (uidLength>0? bytesToHex(uid):"") + "}";
    }
    private static String bytesToHex(byte[] b){ StringBuilder sb=new StringBuilder(); for(byte v:b) sb.append(String.format("%02X",v)); return sb.toString(); }
  }
}