package com.devonfw.tools.ide.service;

public record ServiceResponse(boolean success, String payload, String errorMessage) {

  public static ServiceResponse ok(String payload) {

    return new ServiceResponse(true, payload, null);
  }

  public static ServiceResponse error(String error) {

    return new ServiceResponse(false, null, error);
  }
}
