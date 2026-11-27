package com.devonfw.ide.gui.feature.commandlet;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.devonfw.tools.ide.property.BooleanProperty;
import com.devonfw.tools.ide.property.FileProperty;
import com.devonfw.tools.ide.property.FolderProperty;
import com.devonfw.tools.ide.property.PathProperty;
import com.devonfw.tools.ide.property.Property;

public class ParameterFieldModel {

  public enum FieldType {
    TEXT,
    FILE,
    FOLDER,
    CHECKBOX
  }

  private final Property<?> property;

  private final String displayName;

  private final boolean required;

  private final FieldType fieldType;

  private final StringProperty value = new SimpleStringProperty();

  public ParameterFieldModel(Property<?> property) {
    this.property = property;
    this.displayName = buildDisplayName(property);
    this.required = property.isRequired();
    this.fieldType = determineType(property);
  }

  public Property<?> getProperty() {
    return this.property;
  }

  public String getDisplayName() {
    return this.required ? this.displayName + " *" : this.displayName;
  }

  public StringProperty valueProperty() {
    return this.value;
  }

  public FieldType getType() {
    return this.fieldType;
  }

  private static FieldType determineType(Property<?> property) {

    if (property instanceof BooleanProperty) {
      return FieldType.CHECKBOX;
    }
    if (property instanceof FolderProperty) {
      return FieldType.FOLDER;
    }
    if (property instanceof FileProperty) {
      return FieldType.FILE;
    }

    if (property instanceof PathProperty) {
      return FieldType.FOLDER;
    }
    return FieldType.TEXT;
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

  public List<String> toCliArguments() {
    List<String> result = new ArrayList<>();
    String value = this.value.get();
    if (value == null || value.isBlank()) {
      return result;
    }
    if (this.property.isOption()) {
      if (this.fieldType == FieldType.CHECKBOX) {
        if (Boolean.parseBoolean(value)) {
          result.add(this.property.getName());
        }
      } else {
        result.add(this.property.getName());
        result.add(value);
      }
    } else {
      result.add(value);
    }
    return result;
  }
}
