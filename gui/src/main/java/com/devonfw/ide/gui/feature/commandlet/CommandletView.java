package com.devonfw.ide.gui.feature.commandlet;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import com.devonfw.ide.gui.core.service.NlsService;
import com.devonfw.ide.gui.core.tab.TabView;

/**
 * View of the commandlet tab. It binds the commandlet combo box, the parameters field and the execute button to the {@link CommandletViewModel}.
 */
public class CommandletView extends TabView {

  @FXML
  private ComboBox<String> commandlet;

  @FXML
  private TextField parameters;

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
    this.commandlet.setItems(viewModel.getCommandletViewModel().getItems());
    this.commandlet.valueProperty().bindBidirectional(viewModel.getCommandletViewModel().selectedItemProperty());
    this.commandlet.disableProperty().bind(viewModel.isExecuteButtonDisabledProperty());

    this.parameters.textProperty().bindBidirectional(viewModel.parametersProperty());
    this.parameters.disableProperty().bind(viewModel.isExecuteButtonDisabledProperty());

    this.execute.disableProperty().bind(viewModel.isExecuteButtonDisabledProperty());
    this.execute.setOnAction(_ -> this.viewModel.execute());
  }

}
