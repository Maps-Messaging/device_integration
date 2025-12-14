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

public class RollingBucketAccumulator {

  private final long bucketSizeMillis;
  private final int bucketCount;

  private final float[] bucketValues;
  private final long[] bucketKeys;

  private float rollingSum;
  private long currentBucketKey;
  private int currentIndex;

  public RollingBucketAccumulator(long windowMillis, long bucketSizeMillis) {
    if (windowMillis <= 0L) {
      throw new IllegalArgumentException("windowMillis must be > 0");
    }
    if (bucketSizeMillis <= 0L) {
      throw new IllegalArgumentException("bucketSizeMillis must be > 0");
    }
    if (bucketSizeMillis > windowMillis) {
      throw new IllegalArgumentException("bucketSizeMillis must be <= windowMillis");
    }

    this.bucketSizeMillis = bucketSizeMillis;
    this.bucketCount = (int) Math.ceil((double) windowMillis / (double) bucketSizeMillis);

    this.bucketValues = new float[bucketCount];
    this.bucketKeys = new long[bucketCount];
    clear();
  }

  public synchronized void clear() {
    for (int i = 0; i < bucketCount; i++) {
      bucketValues[i] = 0.0f;
      bucketKeys[i] = Long.MIN_VALUE;
    }
    rollingSum = 0.0f;
    currentBucketKey = Long.MIN_VALUE;
    currentIndex = 0;
  }

  public synchronized void add(long epochMillis, float value) {
    if (Float.isNaN(value) || Float.isInfinite(value)) {
      return;
    }

    long bucketKey = epochMillis / bucketSizeMillis;

    if (currentBucketKey == Long.MIN_VALUE) {
      currentBucketKey = bucketKey;
      currentIndex = 0;
      bucketKeys[currentIndex] = currentBucketKey;
      bucketValues[currentIndex] = 0.0f;
    }

    long diff = bucketKey - currentBucketKey;

    if (diff < 0L) {
      // Time went backwards. Humans do that. Reset to avoid corrupting the window.
      clear();
      currentBucketKey = bucketKey;
      currentIndex = 0;
      bucketKeys[currentIndex] = currentBucketKey;
      bucketValues[currentIndex] = 0.0f;
    } else if (diff > 0L) {
      advanceBuckets(diff, bucketKey);
    }

    // Ensure the slot is valid for this key (covers wrap-around)
    if (bucketKeys[currentIndex] != bucketKey) {
      float removed = bucketValues[currentIndex];
      rollingSum -= removed;

      bucketKeys[currentIndex] = bucketKey;
      bucketValues[currentIndex] = 0.0f;
    }

    bucketValues[currentIndex] += value;
    rollingSum += value;
  }

  public synchronized float getSum() {
    return rollingSum;
  }

  private void advanceBuckets(long diff, long newBucketKey) {
    if (diff >= bucketCount) {
      // Jumped forward beyond the entire window: everything expires.
      clear();
      currentBucketKey = newBucketKey;
      currentIndex = 0;
      bucketKeys[currentIndex] = currentBucketKey;
      bucketValues[currentIndex] = 0.0f;
      return;
    }

    for (long i = 0; i < diff; i++) {
      currentIndex++;
      if (currentIndex >= bucketCount) {
        currentIndex = 0;
      }

      float removed = bucketValues[currentIndex];
      rollingSum -= removed;

      bucketValues[currentIndex] = 0.0f;
      bucketKeys[currentIndex] = currentBucketKey + i + 1;
    }

    currentBucketKey = newBucketKey;
  }
}