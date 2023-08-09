package io.mapsmessaging.devices.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;

import java.util.Map;

public class SerialisationHelper {

  private final ObjectMapper mapper;

  public SerialisationHelper() {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    SimpleModule module = new SimpleModule();
    module.addDeserializer(RegisterData.class, new RegisterDataDeserializer(mapper));
    mapper.registerModule(module);
  }

  public Map<Integer, RegisterData> deserialise(byte[] val) throws JsonProcessingException {
    RegisterDataWrapper wrapper2 = mapper.readValue(new String(val), RegisterDataWrapper.class);
    return wrapper2.getMap();
  }

  public byte[] serialise(Map<Integer, RegisterData> map) throws JsonProcessingException {
    RegisterDataWrapper wrapper = new RegisterDataWrapper(map);
    String json = mapper.writeValueAsString(wrapper);
    return json.getBytes();
  }

}
