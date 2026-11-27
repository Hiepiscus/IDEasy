package com.devonfw.ide.gui.feature.commandlet;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.devonfw.ide.gui.core.context.GuiContextChangeListener;
import com.devonfw.ide.gui.core.context.GuiStateManager;
import com.devonfw.ide.gui.core.context.IdeGuiContext;
import com.devonfw.ide.gui.core.service.CommandletService;
import com.devonfw.ide.gui.core.tab.TabViewModel;
import com.devonfw.tools.ide.commandlet.AbstractCommandlet;
import com.devonfw.tools.ide.property.KeywordProperty;
import com.devonfw.tools.ide.property.Property;

/**
 * View model of the commandlet tab. It exposes the available commandlets, the selected commandlet, the entered parameters and the execute button state.
 */
public class CommandletViewModel extends TabViewModel {

  private static final String TAB_TITLE_KEY = "tab.commandlet";

  private final CommandletService commandletService;

  private final List<String> allCommandlets = new ArrayList<>();

  private final StringProperty query = new SimpleStringProperty();

  private final ObservableList<ParameterFieldModel> parameters = FXCollections.observableArrayList();

  private String loadedCommandlet;

  private final BooleanProperty isExecuteButtonDisabled = new SimpleBooleanProperty();

  private final GuiContextChangeListener contextChangeListener = _ -> load();

  private final ObservableList<String> suggestions = FXCollections.observableArrayList();

  /**
   * Creates the view model and loads the commandlets of the currently selected project.
   *
   * @param guiStateManager the app-wide selection and context holder.
   */
  public CommandletViewModel(GuiStateManager guiStateManager) {
    super(guiStateManager);
    this.commandletService = guiStateManager.getCommandletService();

    // The execute button stays disabled until a project/workspace is selected, mirroring the IDE launcher.
    this.isExecuteButtonDisabled.bind(this.guiStateManager.workspaceSelectedProperty().not());
    this.guiStateManager.addGuiContextChangeListener(this.contextChangeListener);
    load();
  }

  /**
   * Loads the commandlets of the currently selected project. If no workspace is selected yet, the list is cleared and filled again once a workspace becomes
   * available (via the context-change listener).
   */
  public void load() {
    IdeGuiContext context = this.guiStateManager.getCurrentContext();
    this.allCommandlets.clear();
    if (context == null) {
      this.parameters.clear();
      return;
    }
    for (AbstractCommandlet commandlet : context.getCommandletManager().getCommandlets()) {
      this.allCommandlets.add(commandlet.getName());
    }
    updateSuggestions(this.query.get());
    loadPropertiesForm(this.query.get());
  }

  private void loadPropertiesForm(String commandlet) {
    commandlet = (commandlet == null) ? "" : commandlet.trim();
    if (commandlet == null || commandlet.equals(this.loadedCommandlet)) {
      return;
    }
    this.loadedCommandlet = commandlet;
    this.parameters.clear();
    IdeGuiContext context = this.guiStateManager.getCurrentContext();
    AbstractCommandlet commandletObj = (context != null) ? context.getCommandletManager().getCommandlet(commandlet) : null;
    if (commandletObj == null) {
      return;
    }
    for (Property<?> property : commandletObj.getProperties()) {
      if (property instanceof KeywordProperty) {
        continue;
      }
      ParameterFieldModel field = new ParameterFieldModel(property);
      if (field.getType() == ParameterFieldModel.FieldType.CHECKBOX) {
        field.valueProperty().set(String.valueOf(((com.devonfw.tools.ide.property.BooleanProperty) property).isTrue()));
      } else if (property.getValueAsString() != null) {
        field.valueProperty().set(property.getValueAsString());
      }
      this.parameters.add(field);
    }
  }

  void updateSuggestions(String text) {
    this.suggestions.setAll(filter(text));
  }

  private List<String> filter(String text) {
    if (text == null || text.isBlank()) {
      return this.allCommandlets;
    }

    String needle = text.trim().toLowerCase();
    List<String> matches = new ArrayList<>();
    for (String commandlet : this.allCommandlets) {
      if (commandlet.toLowerCase().contains(needle)) {
        matches.add(commandlet);
      }
    }
    return matches;
  }

  /**
   * Releases the resources of this view model, i.e. removes the context-change listener that reloads the commandlet list. Called when the tab is closed.
   */
  public void dispose() {
    this.guiStateManager.removeGuiContextChangeListener(this.contextChangeListener);
  }

  @Override
  public String getTabTitleKey() {
    return TAB_TITLE_KEY;
  }

  public BooleanProperty isExecuteButtonDisabledProperty() {
    return this.isExecuteButtonDisabled;
  }

  /**
   * Builds the argument array for the CLI from the selected commandlet and the user's space-separated parameters.
   *
   * @param commandlet the selected commandlet name.
   * @return the command-line arguments (commandlet name followed by the individual parameters).
   */
  public String[] buildArguments(String commandlet) {
    List<String> arguments = new ArrayList<>();
    arguments.add(commandlet);
    for (ParameterFieldModel field : this.parameters) {
      arguments.addAll(field.toCliArguments());
    }
    return arguments.toArray(new String[0]);
  }

  /**
   * Executes the selected commandlet with the entered parameters in the currently selected project/workspace context.
   */
  public void execute() {
    String commandlet = this.query.get().trim();
    if (commandlet.isEmpty()) {
      return;
    }
    this.commandletService.runCommandlet(buildArguments(commandlet));
  }

  private static boolean isBlank(String text) {
    return (text == null) || text.isBlank();
  }

  public ObservableList<String> getSuggestions() {
    return this.suggestions;
  }

  public void selectSuggestion(String commandlet) {
    this.query.set(commandlet);
    loadPropertiesForm(commandlet);
  }

  public ObservableList<ParameterFieldModel> getParameters() {
    return this.parameters;
  }

  public StringProperty queryProperty() {
    return this.query;
  }

  public Path getWorkingDirectory() {
    IdeGuiContext context = this.guiStateManager.getCurrentContext();
    return (context != null) ? context.getCwd() : null;
  }
}
