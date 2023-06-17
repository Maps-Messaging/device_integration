package io.mapsmessaging.devices.oneWire;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class OneWireSensor {

  private final File myDataFile;

  protected OneWireSensor(File path) {
    myDataFile = path;
  }

  public String getName() {
    return myDataFile.getParentFile().getName();
  }


  public abstract void process(List<String> data);

  private void readData() {
    List<String> ret = new ArrayList<>();
    try (Scanner scanner = new Scanner(myDataFile)) {
      String line;
      do {
        line = scanner.nextLine();
        if (line.length() > 0) ret.add(line);
      }
      while (line.length() > 0);
    } catch (Exception e) {
    }
    process(ret);
  }
}