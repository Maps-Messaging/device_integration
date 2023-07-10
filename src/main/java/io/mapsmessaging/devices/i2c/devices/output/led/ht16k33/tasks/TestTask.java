package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks;

import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.HT16K33Controller;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.Panel;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.SevenSegmentLed;
import io.mapsmessaging.devices.util.Delay;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestTask implements Task {

    private final HT16K33Controller controller;
    private final AtomicBoolean runFlag;

    public TestTask(HT16K33Controller controller) {
        this.controller = controller;
        runFlag = new AtomicBoolean(true);
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stop() {
        runFlag.set(false);
    }

    private void displayMask(Panel panel, int mask, long delay) {
        panel.setAllDisplay(mask);
        controller.rawWrite(panel.pack());
        Delay.pause(delay);

    }

    @Override
    public void run() {
        Panel panel = new Panel(4, true);
        boolean hasColon = false;
        while (runFlag.get()) {
            panel.enableColon(hasColon);
            hasColon = !hasColon;
            for (int x = 0; x < 10; x++) {
                displayMask(panel, SevenSegmentLed.BOTTOM.getMask(), 200);
                displayMask(panel, SevenSegmentLed.MIDDLE.getMask(), 200);
                displayMask(panel, SevenSegmentLed.TOP.getMask(), 200);
                displayMask(panel, SevenSegmentLed.MIDDLE.getMask(), 200);
                displayMask(panel, SevenSegmentLed.BOTTOM.getMask(), 200);
            }
            panel.clear();
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 4; x++) {
                    panel.setDisplay(x, SevenSegmentLed.TOP_LEFT.getMask() | SevenSegmentLed.BOTTOM_LEFT.getMask());
                    controller.rawWrite(panel.pack());
                    Delay.pause(200);
                    panel.clear();
                    panel.setDisplay(x, SevenSegmentLed.TOP_RIGHT.getMask() | SevenSegmentLed.BOTTOM_RIGHT.getMask());
                    controller.rawWrite(panel.pack());
                    Delay.pause(200);
                    panel.clear();
                }
                for (int x = 3; x >= 0; x--) {
                    panel.setDisplay(x, SevenSegmentLed.TOP_RIGHT.getMask() | SevenSegmentLed.BOTTOM_RIGHT.getMask());
                    controller.rawWrite(panel.pack());
                    Delay.pause(200);
                    panel.clear();
                    panel.setDisplay(x, SevenSegmentLed.TOP_LEFT.getMask() | SevenSegmentLed.BOTTOM_LEFT.getMask());
                    controller.rawWrite(panel.pack());
                    Delay.pause(200);
                    panel.clear();
                }
            }
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 4; x++) {
                    panel.setDisplay(x, SevenSegmentLed.TOP_LEFT.getMask());
                    panel.setDisplay(3 - x, SevenSegmentLed.BOTTOM_RIGHT.getMask());
                    controller.rawWrite(panel.pack());
                    Delay.pause(200);
                    panel.clear();
                    panel.setDisplay(x, SevenSegmentLed.TOP_RIGHT.getMask());
                    panel.setDisplay(3 - x, SevenSegmentLed.BOTTOM_LEFT.getMask());
                    controller.rawWrite(panel.pack());
                    Delay.pause(200);
                    panel.clear();
                }
            }
            for (int x = 0; x < 4; x++) {
                panel.setDisplay(x, SevenSegmentLed.DECIMAL.getMask());
                controller.rawWrite(panel.pack());
                Delay.pause(200);
                panel.clear();
            }
            panel.clear();
            for (int x = 0; x < 4; x++) {
                panel.enableColon(true);
                controller.rawWrite(panel.pack());
                Delay.pause(200);
                panel.enableColon(false);
                controller.rawWrite(panel.pack());
                Delay.pause(200);
            }

        }
    }
}
