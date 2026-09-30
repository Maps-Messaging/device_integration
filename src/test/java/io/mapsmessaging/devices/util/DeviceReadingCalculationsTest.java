package io.mapsmessaging.devices.util;

import static org.junit.jupiter.api.Assertions.*;

import io.mapsmessaging.devices.sensorreadings.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;

class DeviceReadingCalculationsTest {
  @Test
  void saturatedAirHasDewPointAtTemperatureAndCondensationRisk() {
    var readings = augment(Map.of("temperature",20f,"humidity",100f));
    assertEquals(20f,number(readings,"dewPoint"),0.1f);
    assertEquals(0f,number(readings,"dewPointSpread"),0.1f);
    assertEquals(Boolean.TRUE,readings.get("condensationRisk").getValue().getResult());
    assertEquals(2.338f,number(readings,"saturationVapourPressure"),0.003f);
    assertEquals(2.338f,number(readings,"vapourPressure"),0.003f);
    assertEquals(17.28f,number(readings,"absoluteHumidity"),0.05f);
  }

  @Test
  void comfortableAirProducesPhysicalMoistureAndThermalReadings() {
    var readings=augment(Map.of("temperature",20f,"humidity",50f));
    assertEquals(9.3f,number(readings,"dewPoint"),0.1f);
    assertEquals(10.7f,number(readings,"dewPointSpread"),0.1f);
    assertEquals(1.169f,number(readings,"vapourPressure"),0.003f);
    assertEquals(8.64f,number(readings,"absoluteHumidity"),0.05f);
    assertEquals(20f,number(readings,"heatIndex"),0.1f);
    assertEquals("Comfortable",readings.get("humidityCategory").getValue().getResult());
    assertEquals(Boolean.FALSE,readings.get("condensationRisk").getValue().getResult());
  }

  @TestFactory
  Stream<DynamicTest> windSpeedUsesBeaufortBands() {
    float[] speeds={0f,1f,2f,4f,6f,9f,12f,15f,18f,22f,26f,30f,35f};
    String[] descriptions={"Calm","Light Air","Light Breeze","Gentle Breeze","Moderate Breeze","Fresh Breeze","Strong Breeze","Near Gale","Gale","Severe Gale","Storm","Violent Storm","Hurricane"};
    List<DynamicTest> tests=new ArrayList<>();
    for(int i=0;i<speeds.length;i++) { int scale=i; tests.add(DynamicTest.dynamicTest(descriptions[i],()->{
      var readings=augment(Map.of("windspeed",speeds[scale]));
      assertEquals(scale,number(readings,"beaufortScale"));
      assertEquals(descriptions[scale],readings.get("beaufortDescription").getValue().getResult());
    })); }
    return tests.stream();
  }

  @Test
  void windChillAppliesOnlyInColdWindyConditions() {
    assertEquals(-4.9f,number(augment(Map.of("temperature",0f,"windspeed",5f)),"windChill"),0.1f);
    assertEquals(20f,number(augment(Map.of("temperature",20f,"windspeed",5f)),"windChill"),0.1f);
    assertEquals(0f,number(augment(Map.of("temperature",0f,"windspeed",0f)),"windChill"),0.1f);
  }

  @Test
  void sourceErrorsRemainErrorsInDerivedReadings() {
    var temperature = new FloatSensorReading("temperature","C","Temperature",20f,true,-50f,100f,1,()->{throw new IOException("sensor unavailable");});
    var humidity = new FloatSensorReading("humidity","%","Humidity",50f,true,0f,100f,1,()->50f);
    var derived=SensorReadingAugmentor.addComputedReadings(List.of(temperature,humidity));
    var result=derived.stream().filter(r->r.getName().equals("dewPoint")).findFirst().orElseThrow().getValue();
    assertTrue(result.hasError());
    assertInstanceOf(IOException.class,result.getError());
  }

  @Test
  void pressureTrendRatesUseElapsedTimeAndKeepFallingSign() {
    var samples=List.of(new TimedFloatSample(0,1000f),new TimedFloatSample(1800000,999f),new TimedFloatSample(3600000,998f));
    assertEquals(-2f,RollingComputations.deltaFirstToLast().compute(samples));
    assertEquals(-2f,RollingComputations.ratePerHourFirstToLast().compute(samples));
    assertEquals(-2f,RollingComputations.slopeLeastSquaresPerHour().compute(samples));
    assertEquals(999f,RollingComputations.average().compute(samples));
    assertEquals(998f,RollingComputations.min().compute(samples));
    assertEquals(1000f,RollingComputations.max().compute(samples));
  }

  @Test
  void rainCounterRatesRejectResetsAndMissingElapsedTime() {
    var rising=List.of(new TimedFloatSample(0,10f),new TimedFloatSample(1800000,13f));
    assertEquals(3f,RollingComputations.deltaFirstToLastNonNegative().compute(rising));
    assertEquals(6f,RollingComputations.ratePerHourFirstToLastNonNegative().compute(rising));
    var reset=List.of(new TimedFloatSample(0,10f),new TimedFloatSample(1800000,1f));
    assertTrue(Float.isNaN(RollingComputations.deltaFirstToLastNonNegative().compute(reset)));
    assertTrue(Float.isNaN(RollingComputations.ratePerHourFirstToLastNonNegative().compute(reset)));
    var sameTime=List.of(new TimedFloatSample(0,1f),new TimedFloatSample(0,2f));
    assertTrue(Float.isNaN(RollingComputations.ratePerHourFirstToLast().compute(sameTime)));
    assertTrue(Float.isNaN(RollingComputations.slopeLeastSquaresPerHour().compute(sameTime)));
  }

  @Test
  void emptyWindowsAndSingleSampleSlopeRemainUnavailable() {
    for(var computation:List.of(RollingComputations.average(),RollingComputations.min(),RollingComputations.max(),RollingComputations.deltaFirstToLast(),RollingComputations.ratePerHourFirstToLast(),RollingComputations.deltaFirstToLastNonNegative(),RollingComputations.ratePerHourFirstToLastNonNegative(),RollingComputations.slopeLeastSquaresPerHour()))
      assertTrue(Float.isNaN(computation.compute(List.of())));
    assertTrue(Float.isNaN(RollingComputations.slopeLeastSquaresPerHour().compute(List.of(new TimedFloatSample(1,3f)))));
  }

  @Test
  void accumulatingRainCounterIgnoresInvalidSamplesAndResets() {
    var counter=new AccumulatingCounterDelta();
    assertEquals(0f,counter.computeDelta(10));
    assertEquals(3f,counter.computeDelta(13));
    assertTrue(Float.isNaN(counter.computeDelta(Float.NaN)));
    assertTrue(Float.isNaN(counter.computeDelta(Float.POSITIVE_INFINITY)));
    assertEquals(2f,counter.computeDelta(15));
    assertEquals(0f,counter.computeDelta(1));
    assertEquals(1f,counter.computeDelta(2));
    counter.reset(); assertEquals(0f,counter.computeDelta(8));
  }

  @Test
  void boundedStatefulWindowRetainsOnlyLatestSamples() {
    Queue<Float> source=new ArrayDeque<>(List.of(1f,3f,5f,Float.NaN));
    var reading=new StatefulFloatSensorReading("average","","Average",0f,true,-100f,100f,1,source::remove,2,60000,RollingComputations.average());
    assertEquals(1f,reading.getValue().getResult());
    assertEquals(2f,reading.getValue().getResult());
    assertEquals(4f,reading.getValue().getResult());
    assertEquals(4f,reading.getValue().getResult());
  }

  @Test
  void hotHumidAirRaisesHeatIndexAndHumidex() {
    var readings=augment(Map.of("temperature",32.2222f,"humidity",70f));
    // NWS heat-index table: 90 F at 70% RH is about 106 F (41.1 C).
    assertEquals(41.1f,number(readings,"heatIndex"),0.6f);
    assertTrue(number(readings,"humidex")>40f);
  }

  @TestFactory
  Stream<DynamicTest> lightClassificationUsesLuxBoundaries() {
    float[] lux={0f,10f,100f,10000f}; String[] labels={"Night","Twilight","Day","Bright"};
    List<DynamicTest> tests=new ArrayList<>();
    for(int i=0;i<lux.length;i++){int index=i;tests.add(DynamicTest.dynamicTest(labels[i],()->
        assertEquals(labels[index],augment(Map.of("lux",lux[index])).get("daylightState").getValue().getResult())));}
    return tests.stream();
  }

  @TestFactory
  Stream<DynamicTest> humidityClassificationChangesAtComfortBoundaries() {
    float[] humidity={20f,30f,60f,75f}; String[] labels={"Dry","Comfortable","Humid","Very Humid"};
    List<DynamicTest> tests=new ArrayList<>();
    for(int i=0;i<humidity.length;i++){int index=i;tests.add(DynamicTest.dynamicTest(labels[i],()->
        assertEquals(labels[index],augment(Map.of("temperature",20f,"humidity",humidity[index])).get("humidityCategory").getValue().getResult())));}
    return tests.stream();
  }

  private static Map<String,SensorReading<?>> augment(Map<String,Float> values) {
    List<SensorReading<?>> base=new ArrayList<>();
    values.forEach((name,value)->base.add(new FloatSensorReading(name,"","Fixture",value,true,-100f,100000f,2,()->value)));
    Map<String,SensorReading<?>> result=new HashMap<>();
    SensorReadingAugmentor.addComputedReadings(base).forEach(r->result.put(r.getName(),r));
    return result;
  }
  private static float number(Map<String,SensorReading<?>> readings,String name) {
    var result=readings.get(name).getValue(); assertFalse(result.hasError(),name);
    return ((Number)result.getResult()).floatValue();
  }
}
