package com.devonfw.tools.ide.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class IdeServiceProtocol {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private IdeServiceProtocol() {
  }

  public static void writeRequest(Writer writer, ServiceRequest request) throws IOException {

    MAPPER.writeValue(writer, request);
    writer.write('\n');
    writer.flush();
  }

  public static ServiceRequest readRequest(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    return line == null ? null : MAPPER.readValue(line, ServiceRequest.class);
  }

  public static void writeResponse(Writer writer, ServiceResponse response) throws IOException {
    MAPPER.writeValue(writer, response);
    writer.write('\n');
    writer.flush();
  }

  public static ServiceResponse readResponse(BufferedReader reader) throws IOException {

    String line = reader.readLine();
    return line == null ? null : MAPPER.readValue(line, ServiceResponse.class);
  }
}
