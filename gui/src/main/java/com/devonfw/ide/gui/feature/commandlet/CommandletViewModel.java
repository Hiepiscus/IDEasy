package com.devonfw.ide.gui.feature.commandlet;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.devonfw.ide.gui.core.context.GuiContextChangeListener;
import com.devonfw.ide.gui.core.context.GuiStateManager;
import com.devonfw.ide.gui.core.context.IdeGuiContext;
import com.devonfw.ide.gui.core.helper.combobox.ComboBoxViewModel;
import com.devonfw.ide.gui.core.service.CommandletService;
import com.devonfw.ide.gui.core.tab.TabViewModel;
import com.devonfw.tools.ide.commandlet.AbstractCommandlet;

/**
 * View model of the commandlet tab. It exposes the state that drives the tab: the list of available commandlets, the selected commandlet, the raw parameters
 * entered by the user, and the enabled state of the execute button (which stays disabled until a project/workspace is selected).
 */
public class CommandletViewModel extends TabViewModel {

  private static final String TAB_TITLE_KEY = "tab.commandlet";

  private final CommandletService commandletService;

  private final ComboBoxViewModel<String> commandletViewModel = new ComboBoxViewModel<>();

  private final StringProperty parameters = new SimpleStringProperty();

  private final BooleanProperty isExecuteButtonDisabled = new SimpleBooleanProperty();

  /**
   * Creates the view model.
   *
   * @param guiStateManager the app-wide selection and context holder.
   */
  public CommandletViewModel(GuiStateManager guiStateManager) {
    super(guiStateManager);
    this.commandletService = guiStateManager.getCommandletService();

    // The execute button stays disabled until a project/workspace is selected, mirroring the IDE launcher.
    this.isExecuteButtonDisabled.bind(this.guiStateManager.workspaceSelectedProperty().not());

    initializeCommandletList();
  }

  @Override
  public String getTabTitleKey() {
    return TAB_TITLE_KEY;
  }

  /**
   * Populates the commandlet combo box. If no context is available yet (no workspace selected), it registers a self-removing listener so the list is filled the
   * first time a context becomes available.
   */
  private void initializeCommandletList() {
    IdeGuiContext context = this.guiStateManager.getCurrentContext();
    if (context != null) {
      populateCommandlets(context);
      return;
    }
    final GuiContextChangeListener[] listenerHolder = new GuiContextChangeListener[1];
    listenerHolder[0] = newContext -> {
      populateCommandlets(newContext);
      this.guiStateManager.removeGuiContextChangeListener(listenerHolder[0]);
    };
    this.guiStateManager.addGuiContextChangeListener(listenerHolder[0]);
  }

  private void populateCommandlets(IdeGuiContext context) {
    this.commandletViewModel.getItems().clear();
    for (AbstractCommandlet commandlet : context.getCommandletManager().getCommandlets()) {
      this.commandletViewModel.getItems().add(commandlet.getName());
    }
  }

  public ComboBoxViewModel<String> getCommandletViewModel() {
    return this.commandletViewModel;
  }

  public StringProperty parametersProperty() {
    return this.parameters;
  }

  public BooleanProperty isExecuteButtonDisabledProperty() {
    return this.isExecuteButtonDisabled;
  }

  /**
   * Builds the argument array for the CLI from the selected commandlet and the user's space-separated parameters.
   *
   * @param commandlet the selected commandlet name.
   * @param parameters the raw, space-separated parameters entered by the user (may be blank).
   * @return the command-line arguments (commandlet name followed by the individual parameters).
   */
  public String[] buildArguments(String commandlet, String parameters) {
    if (parameters == null || parameters.isBlank()) {
      return new String[] { commandlet };
    }
    String[] parts = parameters.trim().split("\\s+");
    String[] arguments = new String[parts.length + 1];
    arguments[0] = commandlet;
    System.arraycopy(parts, 0, arguments, 1, parts.length);
    return arguments;
  }

  /**
   * Executes the selected commandlet with the entered parameters in the currently selected project/workspace context.
   */
  public void execute() {
    String commandlet = this.commandletViewModel.getSelectedItem().orElse(null);
    if (commandlet == null) {
      return;
    }
    this.commandletService.runCommandlet(buildArguments(commandlet, this.parameters.get()));
  }

}
