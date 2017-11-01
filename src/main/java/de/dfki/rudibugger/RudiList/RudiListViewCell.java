/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.RudiList;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static de.dfki.rudibugger.Constants.*;

/**
 * This ListCell is used to visualize the different .rudi files according to 
 * their usage stage in the current project.
 * 
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiListViewCell extends ListCell<RudiPath> {

  static final String FILE_ICON_PATH
          = "file:src/main/resources/icons/FilesAndFolders/";
  
  static Image enabled = new Image(FILE_ICON_PATH + "enabled.png");
  static Image disabled = new Image(FILE_ICON_PATH + "disabled.png");
  static Image main = new Image(FILE_ICON_PATH + "main.png");

  @Override
  protected void updateItem(RudiPath rudiPath, boolean empty) {
    super.updateItem(rudiPath, empty);

    if (empty || rudiPath == null) {

      setText(null);
      setGraphic(null);

    } else {

      setText(rudiPath.toString());

      switch (rudiPath._usedProperty().getValue()) {
        case FILE_IS_MAIN:
          setGraphic(new ImageView(main));
          break;
        case FILE_USED:
          setGraphic(new ImageView(enabled));
          break;
        case FILE_NOT_USED:
          setGraphic(new ImageView(disabled));
          break;
        default:
          break;
        }

    }
  }
}
