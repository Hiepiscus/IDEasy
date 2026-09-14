package com.devonfw.tools.ide.service;

import java.util.Map;

public record ServiceRequest(ServiceOperation operation, Map<String, String> params) {

  public String getParam(String name){

    return this.params == null ? null : this.params.get(name);
  }

}
