package com.devonfw.ide.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devonfw.ide.gui.modal.IdeDialog;
import com.devonfw.tools.ide.commandlet.Commandlet;
import com.devonfw.tools.ide.commandlet.EnvironmentCommandlet;
import com.devonfw.tools.ide.context.IdeContext;
import com.devonfw.tools.ide.property.BooleanProperty;
import com.devonfw.tools.ide.property.KeywordProperty;
import com.devonfw.tools.ide.property.Property;
import com.devonfw.tools.ide.tool.ToolCommandlet;
import com.devonfw.tools.ide.validation.ValidationResult;

public class CommandletController {

  private static final Logger LOG = LoggerFactory.getLogger(CommandletController.class);

  private Commandlet selectedCommandlet;
  private final IdeContext context;
  private final Runnable goBackCallback;

  @FXML
  private ComboBox<String> commandletSelector;

  @FXML
  private VBox formContainer;

  @FXML
  private Button runButton;

  @FXML
  private TextField executionDirectoryField;

  @FXML
  private ResourceBundle resources;

  /// @param context
  public CommandletController(IdeContext context, Runnable goBackCallback) {
    this.context = context;
    this.goBackCallback = goBackCallback;
  }

  @FXML
  private void initialize() {
    commandletSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> onCommandletSelected(newVal));
    Platform.runLater(this::populateCommandletList);
  }

  @FXML
  private void goBack() {
    this.goBackCallback.run();
  }


  private void onCommandletSelected(String name) {
    if (name == null) {
      return;
    }

    Commandlet commandlet = context.getCommandletManager().getCommandlet(name);
    this.selectedCommandlet = commandlet;

    generateFormFields(commandlet.getProperties());
    populateExecutionDirectories(commandlet);
  }

  private void generateFormFields(List<Property<?>> properties) {
    ObservableList<javafx.scene.Node> children = formContainer.getChildren();
    children.clear();

    for (Property<?> property : properties) {
      children.add(PropertyFormFieldFactory.createFormField(property, context));
    }
  }

  private void populateExecutionDirectories(Commandlet commandlet) {
    Path workspacePath = this.context.getWorkspacePath();
    Path candidate = findValidExecutionDirectories(workspacePath, commandlet);

    boolean relevant = !commandlet.isValidExecutionDirectory(workspacePath);

    executionDirectoryField.setVisible(relevant);
    executionDirectoryField.setManaged(relevant);

    if (candidate != null) {
      executionDirectoryField.setText(candidate.toString());
    } else {
      executionDirectoryField.setText(workspacePath.toString());
    }
  }

  private Path findValidExecutionDirectories(Path workspacePath, Commandlet commandlet) {
    try (Stream<Path> stream = Files.walk(workspacePath, 3)) {
      return stream
          .filter(Files::isDirectory)
          .filter(commandlet::isValidExecutionDirectory)
          .findFirst()
          .orElse(null);
    } catch (IOException e) {
      LOG.warn("Failed to scan workspace {} for valid execution directories", workspacePath, e);
      return null;
    }
  }

  private void populateCommandletList() {
    commandletSelector.getItems().clear();
    commandletSelector.getItems().addAll(context.getCommandletManager().getCommandlets().stream()
        .map(Commandlet::getName)
        .sorted()
        .toList());
  }

  @FXML
  private void runCommandlet() {
    if (this.selectedCommandlet == null) {
      return;
    }

    this.selectedCommandlet.reset();

    for (javafx.scene.Node node : formContainer.getChildren()) {
      if (node instanceof javafx.scene.layout.HBox hbox && hbox.getUserData() instanceof Property<?>
          property) {
        for (javafx.scene.Node child : hbox.getChildren()) {
          if (child instanceof javafx.scene.control.TextField textField) {
            String value = textField.getText();
            if (!value.isBlank()) {
              for (String arg : value.split("\\s+")) {
                property.assignValueAsString(arg, this.context, this.selectedCommandlet);
              }
            }
            break;
          }
        }
      }

      if (node instanceof CheckBox checkbox && checkbox.getUserData() instanceof BooleanProperty
          boolProp) {
        boolProp.setValue(checkbox.isSelected());
      }

    }

    if (!validate(this.selectedCommandlet)) {
      return;
    }

    Commandlet commandlet = this.selectedCommandlet;

    if (commandlet instanceof ToolCommandlet toolCommandlet) {
      Path executionDirectory;

      if (executionDirectoryField.isVisible()
          && !executionDirectoryField.getText().isBlank()) {
        executionDirectory = Path.of(executionDirectoryField.getText());
      } else {
        executionDirectory = this.context.getWorkspacePath();
      }

      toolCommandlet.setExecutionDirectory(executionDirectory);
    }

    Task<Void> execution = new Task<>() {
      @Override
      protected Void call() {
        commandlet.run();
        return null;
      }
    };

    execution.setOnSucceeded(event -> {
      this.runButton.setDisable(false);
      new IdeDialog(AlertType.INFORMATION, this.resources.getString("executionSucceeded")).showAndWait();
    });

    execution.setOnFailed(event -> {
      this.runButton.setDisable(false);
      Throwable error = execution.getException();
      LOG.error("Commandlet execution failed", error);
      new IdeDialog(IdeDialog.AlertType.ERROR, error.getMessage()).showAndWait();
    });

    this.runButton.setDisable(true);
    Thread thread = new Thread(execution, "commandlet-" + commandlet.getName());
    thread.setDaemon(true);
    thread.start();
  }

  private boolean validate(Commandlet cmd) {

    for (Property<?> property : cmd.getProperties()) {
      if (property instanceof KeywordProperty keyword) {
        keyword.setValue(true);
      }
    }

    ValidationResult result = cmd.validate();
    if (!result.isValid()) {
      new IdeDialog(IdeDialog.AlertType.ERROR, result.getErrorMessage()).showAndWait();
      return false;
    }

    if (cmd.isIdeHomeRequired() && this.context.getIdeHome() == null) {
      new IdeDialog(IdeDialog.AlertType.ERROR, this.resources.getString("noIdeHome")).showAndWait();
      return false;
    }

    if (cmd.isIdeRootRequired() && this.context.getIdeRoot() == null) {
      new IdeDialog(IdeDialog.AlertType.ERROR, this.resources.getString("noIdeRoot")).showAndWait();
      return false;
    }

    Path licenseAgreement = this.context.getUserHomeIde().resolve(IdeContext.FILE_LICENSE_AGREEMENT);
    if (!Files.isRegularFile(licenseAgreement)) {
      if (cmd instanceof EnvironmentCommandlet) {
        return false;
      }
      new IdeDialog(IdeDialog.AlertType.ERROR, this.resources.getString("licenseNotAccepted")).showAndWait();
      return false;
    }

    return true;
  }

}
