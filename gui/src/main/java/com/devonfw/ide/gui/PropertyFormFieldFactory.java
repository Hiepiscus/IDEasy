package com.devonfw.ide.gui;

import java.io.File;
import java.nio.file.Path;

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

    checkbox.setUserData(booleanProperty);

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
        selected = selectDirectory(currentStage, textField.getText(), context.getCwd());
      } else if (pathProperty instanceof FileProperty) {
        selected = selectFile(currentStage, textField.getText(), context.getCwd());
      } else {
        selected = selectDirectory(currentStage, textField.getText(), context.getCwd());
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

  private static String selectFile(Stage stage, String currentPath, Path projectPath) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select File");

    File initialDir = null;

    if (currentPath != null && !currentPath.isBlank()) {
      File currentFile = new File(currentPath);
      File parent = currentFile.getParentFile();
      if (currentFile.isDirectory()) {
        initialDir = currentFile;
      } else if (currentFile.isFile() && currentFile.getParentFile() != null) {
        initialDir = currentFile.getParentFile();
      }
    }

    if (initialDir == null && projectPath != null) {
      File projectDir = projectPath.toFile();
      if (projectDir.isDirectory()) {
        initialDir = projectDir;
      }
    }

    if (initialDir != null) {
      fileChooser.setInitialDirectory(initialDir);
    }

    File result = fileChooser.showOpenDialog(stage);
    return result != null ? result.getAbsolutePath() : null;
  }

  private static String selectDirectory(Stage stage, String currentPath, Path projectPath) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Select Folder");

    File initialDir = null;

    if (currentPath != null && !currentPath.isBlank()) {
      File currentDir = new File(currentPath);
      if (currentDir.isDirectory()) {
        initialDir = currentDir;
      }
    }

    if (initialDir == null && projectPath != null) {
      File projectDir = projectPath.toFile();
      if (projectDir.isDirectory()) {
        initialDir = projectDir;
      }
    }

    if (initialDir != null) {
      directoryChooser.setInitialDirectory(initialDir);
    }

    File result = directoryChooser.showDialog(stage);
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

    return property.getNameOrAlias();
  }
}
