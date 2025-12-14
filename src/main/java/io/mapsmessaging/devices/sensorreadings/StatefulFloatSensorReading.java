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

package io.mapsmessaging.devices.sensorreadings;

import io.mapsmessaging.devices.util.RollingComputation;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class StatefulFloatSensorReading extends NumericSensorReading<Float> {

  @Getter
  private final int precision;

  private final ReadingSupplier<Float> sourceSupplier;
  private final int maxSamples;
  private final long windowMillis;
  private final RollingComputation computation;

  private final Object lock;
  private final Deque<TimedFloatSample> samples;

  public StatefulFloatSensorReading(
      String name,
      String unit,
      String description,
      Float example,
      boolean readOnly,
      float min,
      float max,
      int precision,
      ReadingSupplier<Float> sourceSupplier,
      int maxSamples,
      long windowMillis,
      RollingComputation computation) {

    super(name, unit, description, example, readOnly, min, max, new SelfSupplier());
    SelfSupplier selfSupplier = (SelfSupplier) getSupplier();

    this.precision = precision;
    this.sourceSupplier = sourceSupplier;
    this.maxSamples = Math.max(1, maxSamples);
    this.windowMillis = Math.max(1L, windowMillis);
    this.computation = computation;

    this.lock = new Object();
    this.samples = new ArrayDeque<>(this.maxSamples);

    selfSupplier.setOwner(this);
  }

  public static float roundToDecimalPlaces(float value, int places) {
    float scale = (float) Math.pow(10, places);
    return Math.round(value * scale) / scale;
  }

  @Override
  protected Float format(Float val) {
    if (val == null) {
      return null;
    }
    if (Float.isNaN(val) || Float.isInfinite(val)) {
      return val;
    }
    if (precision >= 0) {
      return roundToDecimalPlaces(val, precision);
    }
    return val;
  }

  private Float computeValue() {
    float latest;
    try {
      latest = sourceSupplier.get();
    }catch (IOException ioException) {
      return Float.NaN;
    }

    long now = System.currentTimeMillis();

    synchronized (lock) {
      if (!Float.isNaN(latest) && !Float.isInfinite(latest)) {
        samples.addLast(new TimedFloatSample(now, latest));
      }

      purgeExpired(now);
      purgeOversize();

      if (samples.isEmpty()) {
        return Float.NaN;
      }

      return computation.compute(samples);
    }
  }

  private void purgeExpired(long now) {
    long cutoff = now - windowMillis;
    while (!samples.isEmpty()) {
      TimedFloatSample first = samples.peekFirst();
      if (first.epochMillis() >= cutoff) {
        break;
      }
      samples.removeFirst();
    }
  }

  private void purgeOversize() {
    while (samples.size() > maxSamples) {
      samples.removeFirst();
    }
  }

  private static final class SelfSupplier implements ReadingSupplier<Float> {

    private StatefulFloatSensorReading owner;

    private void setOwner(StatefulFloatSensorReading owner) {
      this.owner = owner;
    }

    @Override
    public Float get() {
      return owner.computeValue();
    }
  }
}
