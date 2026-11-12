package io.mapsmessaging.devices.queue;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class HardwareRingBufferTest {
  @Test void fifoAndDestructiveReads() {
    HardwareRingBuffer<TimedDataSet<String>> fifo = new HardwareRingBuffer<>(2);
    TimedDataSet<String> first = new TimedDataSet<>(Instant.EPOCH, "first");
    fifo.offer(first);
    assertTrue(fifo.hasData());
    assertEquals(1, fifo.queueDepth());
    assertSame(first, fifo.getData());
    assertNull(fifo.getData());
    assertFalse(fifo.hasData());
    assertFalse(fifo.hasOverrun());
  }

  @Test void oldestIsDiscardedAndOverrunIsReadClear() {
    HardwareRingBuffer<Integer> fifo = new HardwareRingBuffer<>(2);
    fifo.offer(1);
    fifo.offer(2);
    fifo.offer(3);
    assertEquals(2, fifo.queueDepth());
    assertTrue(fifo.hasOverrun());
    assertFalse(fifo.hasOverrun());
    assertEquals(2, fifo.getData());
    assertEquals(3, fifo.getData());
    assertNull(fifo.getData());
  }

  @Test void rejectInvalidProducerInput() {
    assertThrows(IllegalArgumentException.class, () -> new HardwareRingBuffer<>(0));
    HardwareRingBuffer<String> fifo = new HardwareRingBuffer<>(1);
    assertThrows(NullPointerException.class, () -> fifo.offer(null));
  }
}
