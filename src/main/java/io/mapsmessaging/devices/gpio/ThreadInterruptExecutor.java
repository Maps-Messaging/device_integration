/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.gpio;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadInterruptExecutor implements InterruptExecutor, Runnable {

  private final AtomicBoolean flag;
  private final InterruptHandler handler;

  public ThreadInterruptExecutor(InterruptHandler handler) {
    flag = new AtomicBoolean(true);
    this.handler = handler;
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void run() {
    while (flag.get()) {
      try {
        Thread.sleep(2);
        handler.interruptFired();
      } catch (IOException e) {
        flag.set(false);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        flag.set(false);
      }
    }
  }

  @Override
  public void close() throws IOException {
    flag.set(false);
  }
}
