package com.devonfw.ide.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.devonfw.tools.ide.context.IdeContext;
import com.devonfw.tools.ide.property.BooleanProperty;
import com.devonfw.tools.ide.property.FileProperty;
import com.devonfw.tools.ide.property.FolderProperty;
import com.devonfw.tools.ide.property.KeywordProperty;
import com.devonfw.tools.ide.property.PathProperty;
import com.devonfw.tools.ide.property.Property;

public class PropertyFormFieldFactory {

  private PropertyFormFieldFactory() {

  }

  public static javafx.scene.Node createFormField(Property<?> property, IdeContext context, Stage stage) {
    if (property instanceof KeywordProperty) {
      return createKeywordField((KeywordProperty) property);
    }

    if (property instanceof BooleanProperty booleanProperty) {
      return createBooleanField(booleanProperty);
    }

    if (property instanceof PathProperty pathProperty) {
      return createPathField(pathProperty, context, stage);
    }

    return createTextField(property, context);
  }

  private static javafx.scene.Node createTextField(Property<?> property, IdeContext context) {
    String labelName = buildLabelName(property);
    Label label = new Label(labelName);
    label.setMinWidth(140);
    label.setPadding(new Insets(2, 0, 2, 0));

    TextField textField = new TextField();
    textField.setText(property.getValueAsString() != null ? property.getValueAsString() : "");

    if (property.isRequired()) {
      label.setText(labelName + " *");
    }

    HBox hbox = new HBox(5, label, textField);
    hbox.setPadding(new Insets(2, 0, 2, 0));
    hbox.setUserData(property);

    return hbox;
  }

  private static javafx.scene.Node createBooleanField(BooleanProperty booleanProperty) {

    String displayName = booleanProperty.getName();
    if (displayName.isEmpty()) {
      displayName = booleanProperty.getAlias() != null ? booleanProperty.getAlias() : "flag";
    }

    CheckBox checkbox = new CheckBox(displayName);
    checkbox.setSelected(booleanProperty.isTrue());
    checkbox.setPadding(new Insets(2, 0, 2, 0));

    checkbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
      booleanProperty.setValue(newVal);
    });

    return checkbox;
  }

  private static javafx.scene.Node createKeywordField(KeywordProperty keywordProperty) {
    Label label = new Label(keywordProperty.getOptionName());
    label.setStyle("-fx-font-weight: bold;");
    label.setPadding(new Insets(2, 0, 2, 0));
    return label;
  }

  private static javafx.scene.Node createPathField(PathProperty pathProperty, IdeContext context, Stage stage) {
    String labelName = buildLabelName(pathProperty);
    Label label = new Label(labelName);
    label.setMinWidth(140);
    label.setPadding(new Insets(2, 0, 2, 0));

    TextField textField = new TextField();
    textField.setText(pathProperty.getValueAsString() != null ? pathProperty.getValueAsString() : "");
    HBox.setHgrow(textField, Priority.ALWAYS);

    if (pathProperty.isRequired()) {
      label.setText(labelName + " *");
    }

    Button browseButton = new Button("...");
    browseButton.setOnAction(actionEvent -> {
      Stage currentStage = stage;
      if (currentStage == null && textField.getScene() != null) {
        currentStage = (Stage) textField.getScene().getWindow();
      }

      String selected;
      if (pathProperty instanceof FolderProperty) {
        selected = selectDirectory(currentStage);
      } else if (pathProperty instanceof FileProperty) {
        selected = selectFile(currentStage);
      } else {
        selected = selectDirectory(currentStage);
      }

      if (selected != null) {
        textField.setText(selected);
      }
    });

    HBox hbox = new HBox(5, label, textField, browseButton);
    hbox.setPadding(new Insets(2, 0, 2, 0));
    hbox.setUserData(pathProperty);

    return hbox;
  }

  private static String selectFile(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select File");
    java.io.File result = fileChooser.showOpenDialog(stage);
    return result != null ? result.getAbsolutePath() : null;
  }

  private static String selectDirectory(Stage stage) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Select Folder");
    java.io.File result = directoryChooser.showDialog(stage);
    return result != null ? result.getAbsolutePath() : null;
  }

  private static String buildLabelName(Property<?> property) {
    String name = property.getName();
    String alias = property.getAlias();

    if (property.isOption()) {
      if (alias != null && !alias.isEmpty()) {
        return name + " (" + alias + ")";
      }
      return name;
    }

    if (name.isEmpty() && alias != null) {
      return alias;
    }
    return name;
  }
}
