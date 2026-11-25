package com.devonfw.ide.gui.feature.commandlet;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.devonfw.tools.ide.property.BooleanProperty;
import com.devonfw.tools.ide.property.Property;

public class ParameterFieldModel {

  private final Property<?> property;

  private final String displayName;

  private final boolean required;

  private final boolean isCheckbox;

  private final StringProperty value = new SimpleStringProperty();

  public ParameterFieldModel(Property<?> property) {
    this.property = property;
    this.displayName = buildDisplayName(property);
    this.required = property.isRequired();
    this.isCheckbox = property instanceof BooleanProperty;
  }

  public Property<?> getProperty() {
    return this.property;
  }

  public String getDisplayName() {
    return this.required ? this.displayName + " *" : this.displayName;
  }

  public boolean isCheckbox() {
    return this.isCheckbox;
  }

  public StringProperty valueProperty() {
    return this.value;
  }

  private String buildDisplayName(Property<?> property) {
    String name = property.getName();
    String alias = property.getAlias();
    if (property.isOption()) {
      if (alias != null && !alias.isEmpty()) {
        return name + " (" + alias + ")";
      }
      return name;
    }
    return property.getNameOrAlias();
  }
}
