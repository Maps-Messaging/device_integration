package io.mapsmessaging.devices.deviceinterfaces;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.mapsmessaging.devices.io.TypeNameResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "className", visible = true)
@JsonTypeIdResolver(TypeNameResolver.class)
public interface RegisterData {

}
