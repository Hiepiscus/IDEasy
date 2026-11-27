package com.devonfw.ide.gui.feature.commandlet;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.devonfw.ide.gui.core.service.NlsService;
import com.devonfw.ide.gui.core.tab.TabView;

/**
 * View of the commandlet tab. It binds the commandlet combo box, the parameters field and the execute button to the {@link CommandletViewModel}.
 */
public class CommandletView extends TabView {

  @FXML
  private TextField commandlet;

  @FXML
  private ListView<String> suggestions;

  @FXML
  private VBox parameters;

  @FXML
  private Button execute;

  private final CommandletViewModel viewModel;

  private final NlsService nlsService;

  /**
   * Creates the commandlet view and loads its FXML.
   *
   * @param viewModel the view model of the commandlet tab.
   * @param nlsService the localization service.
   */
  public CommandletView(CommandletViewModel viewModel, NlsService nlsService) {
    this.viewModel = viewModel;
    this.nlsService = nlsService;
    loadFxml("Commandlet.fxml", nlsService);
  }

  @FXML
  private void initialize() {
    this.commandlet.textProperty().bindBidirectional(this.viewModel.queryProperty());
    this.commandlet.disableProperty().bind(viewModel.isExecuteButtonDisabledProperty());

    this.commandlet.textProperty().addListener((_, _, text) -> {
      this.suggestions.getSelectionModel().clearSelection();
      this.viewModel.updateSuggestions(text);
    });

    this.suggestions.setItems(this.viewModel.getSuggestions());
    this.suggestions.disableProperty().bind(this.viewModel.isExecuteButtonDisabledProperty());

    this.suggestions.visibleProperty().bind(
        Bindings.or(this.commandlet.focusedProperty(), this.suggestions.focusedProperty())
            .and(Bindings.isNotEmpty(this.viewModel.getSuggestions())));
    this.suggestions.managedProperty().bind(this.suggestions.visibleProperty());
    this.suggestions.getSelectionModel().selectedItemProperty().addListener((_, _, selected) -> {
      if (selected != null) {
        javafx.application.Platform.runLater(() -> {
          this.viewModel.selectSuggestion(selected);
          this.commandlet.requestFocus();
        });
      }
    });

    this.commandlet.setOnKeyPressed(event -> {
      if (event == null) {
        return;
      }
      if (event.getCode() == KeyCode.ENTER && !this.suggestions.getItems().isEmpty()) {
        this.viewModel.selectSuggestion(this.viewModel.getSuggestions().getFirst());
        event.consume();
      }
    });

    this.parameters.disableProperty().bind(viewModel.isExecuteButtonDisabledProperty());
    this.viewModel.getParameters().addListener((ListChangeListener<ParameterFieldModel>) _ -> rebuildParameterForm());
    rebuildParameterForm();

    this.execute.disableProperty().bind(viewModel.isExecuteButtonDisabledProperty());
    this.execute.setOnAction(_ -> this.viewModel.execute());
  }

  private void rebuildParameterForm() {
    this.parameters.getChildren().clear();
    for (ParameterFieldModel field : this.viewModel.getParameters()) {
      this.parameters.getChildren().add(createParameterRow(field));
    }
  }

  private Region createParameterRow(ParameterFieldModel field) {
    Label label = new Label(field.getDisplayName());
    label.setMinWidth(200);
    label.setMaxWidth(200);
    label.setStyle("-fx-text-align: right; -fx-padding: 0 8 0 0;");
    if (field.getType() == ParameterFieldModel.FieldType.CHECKBOX) {
      CheckBox checkBox = new CheckBox();
      checkBox.setSelected(Boolean.parseBoolean(field.valueProperty().get()));
      checkBox.selectedProperty().addListener((_, _, selected) -> field.valueProperty().set(String.valueOf(selected)));
      return new HBox(5, label, checkBox);
    }
    TextField textField = new TextField();
    textField.setMaxWidth(Double.MAX_VALUE);
    textField.textProperty().bindBidirectional(field.valueProperty());
    if (field.getType() == ParameterFieldModel.FieldType.FILE || field.getType() == ParameterFieldModel.FieldType.FOLDER) {
      Button browseButton = new Button("...");
      browseButton.setMaxWidth(Region.USE_PREF_SIZE);
      browseButton.setOnAction(_ -> browse(field, textField));
      return new HBox(5, label, textField, browseButton);
    }
    return new HBox(5, label, textField);
  }

  /**
   * Opens a file or directory chooser (depending on the field type) and writes the picked path back to the field.
   */
  private void browse(ParameterFieldModel field, TextField textField) {
    Stage stage = (this.getScene() != null) ? (Stage) this.getScene().getWindow() : null;
    String current = textField.getText();
    boolean folder = field.getType() == ParameterFieldModel.FieldType.FOLDER;
    String selected = folder ? selectDirectory(stage, current) : selectFile(stage, current);
    if (selected != null) {
      field.valueProperty().set(selected);
    }
  }

  private String selectDirectory(Stage stage, String current) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle(this.nlsService.get("commandlet.choose_folder"));
    File initialDir = initialDirectoryOf(current, true);
    if (initialDir != null) {
      directoryChooser.setInitialDirectory(initialDir);
    }
    File result = directoryChooser.showDialog(stage);
    return (result != null) ? result.getAbsolutePath() : null;
  }

  private String selectFile(Stage stage, String current) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(this.nlsService.get("commandlet.choose_file"));
    File initialDir = initialDirectoryOf(current, false);

    if (initialDir != null) {
      fileChooser.setInitialDirectory(initialDir);
    }
    File result = fileChooser.showOpenDialog(stage);
    return (result != null) ? result.getAbsolutePath() : null;
  }

  private File initialDirectoryOf(String current, boolean folder) {
    if (current != null && !current.isBlank()) {
      File currentPath = new File(current);
      if (folder) {
        if (currentPath.isDirectory()) {
          return currentPath;
        }
      } else if (currentPath.isFile() && currentPath.getParentFile() != null) {
        return currentPath.getParentFile();
      }

      File cwdFile = this.viewModel.getWorkingDirectory().toFile();
      if (cwdFile.isDirectory()) {
        return cwdFile;
      }
    }
    return null;
  }
}

