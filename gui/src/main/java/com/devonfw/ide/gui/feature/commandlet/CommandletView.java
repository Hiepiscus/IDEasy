package com.devonfw.ide.gui.feature.commandlet;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

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
  private ListView<ParameterFieldModel> parameters;

  @FXML
  private Button execute;

  private final CommandletViewModel viewModel;

  /**
   * Creates the commandlet view and loads its FXML.
   *
   * @param viewModel the view model of the commandlet tab.
   * @param nlsService the localization service.
   */
  public CommandletView(CommandletViewModel viewModel, NlsService nlsService) {
    this.viewModel = viewModel;
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

    this.parameters.setItems(this.viewModel.getParameters());
    this.parameters.disableProperty().bind(this.viewModel.isExecuteButtonDisabledProperty());
    this.parameters.setCellFactory(view -> new ParameterCell());

    this.execute.disableProperty().bind(viewModel.isExecuteButtonDisabledProperty());
    this.execute.setOnAction(_ -> this.viewModel.execute());
  }

  /**
   * Renders one parameter form field: a text field or checkbox bound to the value of the {@link ParameterFieldModel}.
   */
  private class ParameterCell extends ListCell<ParameterFieldModel> {

    private final Label label = new Label();

    private final TextField textField = new TextField();

    private final CheckBox checkBox = new CheckBox();

    /** The field currently rendered by this (recycled) cell; the checkbox listener writes to this. */
    private ParameterFieldModel field;

    ParameterCell() {
      this.label.setMinWidth(200);
      this.label.setMaxWidth(200);
      this.label.setStyle("-fx-text-align: right; -fx-padding: 0 8 0 0;");
      this.checkBox.selectedProperty().addListener((_, _, selected) -> {
        if (this.field != null) {
          this.field.valueProperty().set(String.valueOf(selected));
        }
      });
    }

    @Override
    protected void updateItem(ParameterFieldModel item, boolean empty) {
      super.updateItem(item, empty);
      this.field = empty ? null : item;
      if (empty || (item == null)) {
        setGraphic(null);
        return;
      }
      this.label.setText(item.getDisplayName());
      if (item.isCheckbox()) {
        this.checkBox.setSelected(Boolean.parseBoolean(item.valueProperty().get()));
        setGraphic(new HBox(5, this.label, this.checkBox));
      } else {
        this.textField.textProperty().bindBidirectional(item.valueProperty());
        this.textField.setMaxWidth(Double.MAX_VALUE);
        setGraphic(new HBox(5, this.label, this.textField));
      }
    }
  }
}

