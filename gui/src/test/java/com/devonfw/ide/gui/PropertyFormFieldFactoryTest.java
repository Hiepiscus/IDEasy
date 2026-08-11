package com.devonfw.ide.gui;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import org.junit.jupiter.api.Test;

import com.devonfw.tools.ide.property.BooleanProperty;
import com.devonfw.tools.ide.property.FolderProperty;
import com.devonfw.tools.ide.property.KeywordProperty;
import com.devonfw.tools.ide.property.StringProperty;

///
public class PropertyFormFieldFactoryTest extends HeadlessApplicationTest {

  @Test
  void keywordPropertyShouldProduceLabel() {
    KeywordProperty prop = new KeywordProperty("install", false, null);
    javafx.scene.Node node = PropertyFormFieldFactory.createFormField(prop, null, null);
    assertThat(node).isInstanceOf(Label.class);
  }

  @Test
  void booleanPropertyShouldProduceCheckBox() {
    BooleanProperty prop = new BooleanProperty("--force", false, "-f");
    javafx.scene.Node node = PropertyFormFieldFactory.createFormField(prop, null, null);
    assertThat(node).isInstanceOf(CheckBox.class);
  }

  @Test
  void stringPropertyShouldProduceHBoxWithLabelAndTextField() {
    StringProperty prop = new StringProperty("--name", true, null);
    javafx.scene.Node node = PropertyFormFieldFactory.createFormField(prop, null, null);

    assertThat(node).isInstanceOf(HBox.class);
    HBox hbox = (HBox) node;

    assertThat(hbox.getChildrenUnmodifiable()).hasSize(2);
    assertThat(hbox.getChildrenUnmodifiable().get(0)).isInstanceOf(Label.class);
    assertThat(hbox.getChildrenUnmodifiable().get(1)).isInstanceOf(TextField.class);
  }

  @Test
  void folderPropertyShouldProduceHBoxWithLabelTextFieldAndBrowseButton() {
    FolderProperty prop = new FolderProperty("--target", true, null, false);
    Node node = PropertyFormFieldFactory.createFormField(prop, null, null);

    assertThat(node).isInstanceOf(HBox.class);
    HBox hbox = (HBox) node;

    assertThat(hbox.getChildren()).hasSize(3);
    assertThat(hbox.getChildren().get(0)).isInstanceOf(Label.class);
    assertThat(hbox.getChildren().get(1)).isInstanceOf(TextField.class);
    assertThat(hbox.getChildren().get(2)).isInstanceOf(Button.class);
  }

}
