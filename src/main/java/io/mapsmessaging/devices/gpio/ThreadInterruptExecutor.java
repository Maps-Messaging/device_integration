package io.mapsmessaging.devices.gpio;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadInterruptExecutor implements InterruptExecutor, Runnable {

  private final AtomicBoolean flag;
  private final InterruptHandler handler;

  public ThreadInterruptExecutor(InterruptHandler handler){
    flag = new AtomicBoolean(true);
    this.handler = handler;
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void run() {
    while(flag.get()){
      try {
        Thread.sleep(2);
        handler.interruptFired();
      } catch (Exception e) {
        flag.set(false);
      }
    }
  }

  @Override
  public void close() throws IOException {
    flag.set(false);
  }
}
