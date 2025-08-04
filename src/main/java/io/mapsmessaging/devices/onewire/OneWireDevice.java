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

package io.mapsmessaging.devices.onewire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class OneWireDevice {

  private final File myDataFile;

  protected OneWireDevice(File path) {
    myDataFile = path;
  }

  public String getName() {
    return myDataFile.getParentFile().getName();
  }


  public abstract void process(List<String> data);

  public void update() {
    List<String> ret = new ArrayList<>();
    try (Scanner scanner = new Scanner(myDataFile)) {
      String line;
      do {
        line = scanner.nextLine();
        if (!line.isEmpty()) ret.add(line);
      }
      while (!line.isEmpty());
    } catch (Exception e) {
      // ignore
    }
    process(ret);
  }
}