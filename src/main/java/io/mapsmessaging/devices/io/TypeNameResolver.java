package io.mapsmessaging.devices.io;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class TypeNameResolver extends TypeIdResolverBase {

  @Override
  public String idFromValue(Object value) {
    return PackageNameProcessor.getInstance().getPrefix(value.getClass().getName());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return idFromValue(value);
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    try {
      id = PackageNameProcessor.getInstance().getPackage(id);
      Class<?> clazz = Class.forName(id);
      return context.constructType(clazz);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Cannot find class " + id, e);
    }
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.CUSTOM;
  }
}
