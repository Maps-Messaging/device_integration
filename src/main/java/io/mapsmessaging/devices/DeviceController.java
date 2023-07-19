/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices;

import io.mapsmessaging.devices.util.ProxyDeviceConfiguration;
import io.mapsmessaging.devices.util.ProxyDeviceRequest;
import io.mapsmessaging.devices.util.ProxyDeviceValue;
import io.mapsmessaging.schemas.config.SchemaConfig;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.StringWriter;

public interface DeviceController {

  String getName();

  String getDescription();

  SchemaConfig getSchema();

  default DeviceValue getValue() throws IOException{
    return new ProxyDeviceValue(getDeviceState());
  }

  default DeviceConfiguration getConfiguration() throws IOException{
    return new ProxyDeviceConfiguration(getDeviceConfiguration());
  }


  default DeviceRequest updateDeviceConfiguration(DeviceRequest request) throws IOException{
    if(request instanceof ProxyDeviceRequest){
      return new ProxyDeviceRequest(updateDeviceConfiguration( ((ProxyDeviceRequest)request).getBuf()));
    }
    return new ProxyDeviceRequest(new byte[0]);
  }


  byte[] getDeviceConfiguration() throws IOException;


  byte[] getDeviceState() throws IOException;

  default byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    return new byte[0];
  }

  default void close() {
  }

  default String schemaToString(ObjectSchema schema) {
    StringWriter stringWriter = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(stringWriter);
    JSONPrinter printer = new JSONPrinter(jsonWriter);
    schema.describeTo(printer);
    return stringWriter.getBuffer().toString();
  }
}
