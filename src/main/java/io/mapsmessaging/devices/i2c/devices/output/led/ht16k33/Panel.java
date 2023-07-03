package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import java.util.Arrays;
import java.util.Base64;

public class Panel {

  private final byte[] display;

  private final boolean hasColon;

  public Panel(int size, boolean hasColon){
    this.hasColon = hasColon;
    display = new byte[hasColon?size+1:size];
  }

  public String pack(){
    return Base64.getEncoder().encodeToString(display);
  }

  public void setDisplay(int position, int mask){
    int actual = hasColon?position+1:position;
    if(actual > display.length){
      return;
    }
    display[actual] = (byte)(mask & 0xff);
  }

  public void enableColon(boolean flag){
    if(!hasColon) {
      return;
    }
    if(flag){
      display[2] = (byte)(0xff);
    }
    display[2] = (byte)(0x0);
  }

  public void clear(){
    Arrays.fill(display, (byte) (0x0));
  }

}
