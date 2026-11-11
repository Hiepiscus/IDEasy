package com.devonfw.ide.gui.feature.commandlet;

import javafx.scene.control.Tab;

import com.devonfw.ide.gui.core.context.GuiStateManager;
import com.devonfw.ide.gui.core.event.TabChangeEvent;
import com.devonfw.ide.gui.core.tab.TabComponent;
import com.devonfw.ide.gui.core.tab.TabView;

/**
 * Tab that lets the user select a commandlet, enter parameters and execute it.
 */
public class CommandletTab extends TabComponent<CommandletViewModel> {

  private final String tabTitleKey = "tab.commandlet";

  /**
   * Creates the commandlet tab.
   *
   * @param guiStateManager the app-wide selection and context holder.
   */
  public CommandletTab(GuiStateManager guiStateManager) {
    super(guiStateManager);
  }

  @Override
  public TabView createTab() {
    this.viewModel = new CommandletViewModel(this.guiStateManager);
    this.view = new CommandletView(this.viewModel, this.nlsService);
    this.tab = new Tab(this.nlsService.get(tabTitleKey), this.view);
    this.tab.setUserData(tabTitleKey);
    this.tab.setClosable(false);

    return this.view;
  }

  @Override
  public TabChangeEvent createTabChangeEvent() {
    createTab();
    return new TabChangeEvent(this.tab);
  }

  @Override
  public String getTabTitleKey() {
    return this.tabTitleKey;
  }

  @Override
  public TabView getView() {
    return view;
  }

  @Override
  public CommandletViewModel getViewModel() {
    return viewModel;
  }

}
