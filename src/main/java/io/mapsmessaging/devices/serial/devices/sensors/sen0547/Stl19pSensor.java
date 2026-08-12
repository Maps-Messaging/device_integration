/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Stl19pSensor implements Device {

  static final int FRAME_LENGTH = 47;
  static final int POINTS_PER_FRAME = 12;
  static final int FRAME_HEADER = 0x54;
  static final int FRAME_VER_LEN = 0x2C;
  static final int MAX_QUEUED_SCANS = 20;

  private final SerialDevice serialPort;
  private final byte[] frameBuffer = new byte[FRAME_LENGTH];
  private final byte[] readBuffer = new byte[1024];
  private final List<Point> currentScan = new ArrayList<>(600);
  private final ArrayDeque<Scan> scanQueue = new ArrayDeque<>(MAX_QUEUED_SCANS);
  private final Thread readerThread;

  private int frameIndex;
  private boolean scanStarted;
  private double previousAngleDegrees = Double.NaN;
  private String currentScanTimestamp;
  private Duration responseTimeout = Duration.ofSeconds(1);
  private volatile boolean running;
  private volatile IOException readerException;

  public Stl19pSensor(SerialDevice serialPort) throws IOException {
    this.serialPort = Objects.requireNonNull(serialPort, "serialPort");
    open();
    running = true;
    readerThread = new Thread(this::readLoop, "stl19p-" + serialPort.getSystemPortName());
    readerThread.setDaemon(true);
    readerThread.start();
  }

  @Override
  public String getName() {
    return "SEN0547";
  }

  @Override
  public String getDescription() {
    return "DFRobot SEN0547 STL-19P 360 degree LiDAR";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  public void open() throws IOException {
    if (serialPort.isOpen()) {
      return;
    }
    if (!serialPort.openPort()) {
      throw new IOException("Failed to open serial port: " + serialPort.getSystemPortName());
    }
  }

  public void close() {
    running = false;
    if (serialPort.isOpen()) {
      serialPort.closePort();
    }
    synchronized (scanQueue) {
      scanQueue.notifyAll();
    }
    readerThread.interrupt();
  }

  public void setResponseTimeout(Duration responseTimeout) {
    this.responseTimeout = Objects.requireNonNull(responseTimeout, "responseTimeout");
  }

  public Scan readScan() throws IOException {
    long deadline = System.nanoTime() + responseTimeout.toNanos();
    synchronized (scanQueue) {
      while (scanQueue.isEmpty()) {
        IOException exception = readerException;
        if (exception != null) {
          throw exception;
        }
        if (!running) {
          throw new IOException("STL-19P sensor is closed");
        }

        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
          throw new IOException("Timeout waiting for complete STL-19P scan");
        }

        long millis = TimeUnit.NANOSECONDS.toMillis(remaining);
        int nanos = (int) (remaining - TimeUnit.MILLISECONDS.toNanos(millis));
        try {
          scanQueue.wait(millis, nanos);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IOException("Interrupted waiting for complete STL-19P scan", e);
        }
      }
      return scanQueue.pollFirst();
    }
  }

  int getQueuedScanCount() {
    synchronized (scanQueue) {
      return scanQueue.size();
    }
  }

  private void readLoop() {
    while (running) {
      int count;
      try {
        count = serialPort.readBytes(readBuffer, readBuffer.length);
      } catch (RuntimeException e) {
        setReaderException(new IOException("Error reading STL-19P serial port", e));
        return;
      }

      if (!running) {
        return;
      }
      if (count < 0) {
        setReaderException(new IOException("Error reading from serial port, readBytes=" + count));
        return;
      }
      if (count == 0) {
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        continue;
      }

      processBytes(readBuffer, count);
    }
  }

  private void setReaderException(IOException exception) {
    readerException = exception;
    synchronized (scanQueue) {
      scanQueue.notifyAll();
    }
  }

  private void processBytes(byte[] data, int length) {
    for (int index = 0; index < length; index++) {
      processByte(data[index] & 0xFF);
    }
  }

  private void processByte(int value) {
    if (frameIndex == 0) {
      if (value == FRAME_HEADER) {
        frameBuffer[frameIndex++] = (byte) value;
      }
      return;
    }

    if (frameIndex == 1) {
      if (value == FRAME_VER_LEN) {
        frameBuffer[frameIndex++] = (byte) value;
      } else if (value == FRAME_HEADER) {
        frameBuffer[0] = (byte) value;
        frameIndex = 1;
      } else {
        frameIndex = 0;
      }
      return;
    }

    frameBuffer[frameIndex++] = (byte) value;
    if (frameIndex == FRAME_LENGTH) {
      if (crc8(frameBuffer, FRAME_LENGTH - 1) == (frameBuffer[FRAME_LENGTH - 1] & 0xFF)) {
        parseFrame();
      }
      frameIndex = 0;
    }
  }

  private void parseFrame() {
    double startAngleDegrees = unsignedShortLittleEndian(frameBuffer, 4) / 100.0;
    double endAngleDegrees = unsignedShortLittleEndian(frameBuffer, 42) / 100.0;
    double angleSpanDegrees = (endAngleDegrees + 360.0 - startAngleDegrees) % 360.0;
    double angleStepDegrees = angleSpanDegrees / (POINTS_PER_FRAME - 1);

    for (int pointIndex = 0; pointIndex < POINTS_PER_FRAME; pointIndex++) {
      int offset = 6 + (pointIndex * 3);
      int distanceMm = unsignedShortLittleEndian(frameBuffer, offset);
      int intensity = frameBuffer[offset + 2] & 0xFF;
      double angleDegrees = startAngleDegrees + (pointIndex * angleStepDegrees);
      if (angleDegrees >= 360.0) {
        angleDegrees -= 360.0;
      }
      processPoint(new Point(angleDegrees, distanceMm, intensity));
    }
  }

  private void processPoint(Point point) {
    boolean wrapped = !Double.isNaN(previousAngleDegrees) && point.angleDegrees < 20.0 && previousAngleDegrees > 340.0;
    if (wrapped) {
      if (scanStarted && !currentScan.isEmpty()) {
        enqueueScan(new Scan(currentScanTimestamp, currentScan));
      }
      currentScan.clear();
      scanStarted = true;
      currentScanTimestamp = Instant.now().toString();
    }

    if (scanStarted) {
      currentScan.add(point);
    }
    previousAngleDegrees = point.angleDegrees;
  }

  private void enqueueScan(Scan scan) {
    synchronized (scanQueue) {
      if (scanQueue.size() == MAX_QUEUED_SCANS) {
        scanQueue.pollFirst();
      }
      scanQueue.offerLast(scan);
      scanQueue.notifyAll();
    }
  }

  static int crc8(byte[] data, int length) {
    int crc = 0;
    for (int index = 0; index < length; index++) {
      crc ^= data[index] & 0xFF;
      for (int bit = 0; bit < 8; bit++) {
        if ((crc & 0x80) != 0) {
          crc = ((crc << 1) ^ 0x4D) & 0xFF;
        } else {
          crc = (crc << 1) & 0xFF;
        }
      }
    }
    return crc;
  }

  private static int unsignedShortLittleEndian(byte[] data, int offset) {
    return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
  }

  public static final class Scan {
    private final String timestamp;
    private final List<Point> points;

    private Scan(String timestamp, List<Point> points) {
      this.timestamp = timestamp;
      this.points = List.copyOf(points);
    }

    public String getTimestamp() {
      return timestamp;
    }

    public List<Point> getPoints() {
      return points;
    }
  }

  public static final class Point {
    private final double angleDegrees;
    private final int distanceMm;
    private final int intensity;

    private Point(double angleDegrees, int distanceMm, int intensity) {
      this.angleDegrees = angleDegrees;
      this.distanceMm = distanceMm;
      this.intensity = intensity;
    }

    public double getAngleDegrees() {
      return angleDegrees;
    }

    public int getDistanceMm() {
      return distanceMm;
    }

    public int getIntensity() {
      return intensity;
    }
  }
}
