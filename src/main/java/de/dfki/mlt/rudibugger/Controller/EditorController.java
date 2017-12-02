/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.TabManagement.TabStore;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class EditorController {

  /* the logger */
  static Logger log = LoggerFactory.getLogger("rudiLog");

  /* the model */
  private DataModel _model;

  public void initModel(DataModel model) {
    if (_model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    _model = model;

    /* intialise the TabStore */
    tabStore = new TabStore(tabBox);

    /* this listener waits for tab requests: open or switch to */
    _model.requestedFileProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal != null) {
        tabStore.openTab(newVal);
      }
    _model.requestedFileProperty().setValue(null);
    });

    /* this listener waits for tab requests: close */
    _model.requestedCloseTabProperty().addListener((arg, oldVal, newVal) -> {
      if (newVal != null) {
        tabStore.closeTab(newVal);
      }
    });

    /* this listener represents the current active tab */
    _model.selectedTabProperty().bindBidirectional(
            tabStore.currentTabProperty());
  }

  /*****************************************************************************
   * The Tab Management
   ****************************************************************************/

  private TabStore tabStore;


  /*****************************************************************************
   * The different GUI elements
   ****************************************************************************/

  /* The HBox containing the tabPane(s) */
  @FXML
  private HBox tabBox;

}