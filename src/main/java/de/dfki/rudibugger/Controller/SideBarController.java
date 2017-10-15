/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.Controller;

import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.RuleStore.Import;
import de.dfki.rudibugger.DataModel;
import de.dfki.rudibugger.RuleStore.Rule;
import de.dfki.rudibugger.project.RudiFileTreeItem;
import de.dfki.rudibugger.RuleTreeView.BasicTreeItem;
import de.dfki.rudibugger.RuleTreeView.ImportTreeItem;
import de.dfki.rudibugger.RuleTreeView.RuleTreeItem;
import de.dfki.rudibugger.project.RudiFolderTreeItem;
import de.dfki.rudibugger.tabs.RudiTab;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;

/**
 * This controller manages the left part of rudibugger window:
 * the TreeView of files, the TreeView of rules and some buttons.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SideBarController {

  /** the logger of the SideBarController */
  static Logger log = Logger.getLogger("rudiLog");

  /** the DataModel */
  private DataModel model;

  /**
   * This function connects this controller to the DataModel
   *
   * @param model
   */
  public void initModel(DataModel model) {
    if (this.model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this.model = model;


    /* this Listener builds or modifies the TreeView, if the RuleModel
    was changed.*/
    model.ruleModelChangeProperty().addListener(new ChangeListener() {
      @Override
      public void changed(ObservableValue o, Object oldVal,
              Object newVal) {
        switch ((int) newVal) {
          case RULE_MODEL_NEWLY_CREATED:
            log.debug("RuleModel has been found, building TreeView...");
            ruleTreeView.setRoot(buildTreeView(model));
            log.debug("TreeView based on RuleModel has been built.");
            model.setRuleModelChangeStatus(RULE_MODEL_UNCHANGED);
            break;
          case RULE_MODEL_CHANGED:
            log.debug("RuleModel has been modified, adapting TreeView...");
            log.debug("FUNCTION TO BE IMPLEMENTED YET");
            ruleTreeView.setRoot(buildTreeView(model));
            log.debug("TreeView has been adapted.");
            model.setRuleModelChangeStatus(RULE_MODEL_UNCHANGED);
            break;
          case RULE_MODEL_UNCHANGED:
            break;
          default:
            break;
        }
      }
    });



    /* TODO: what should happen when a .rudi file is double clicked */
    fileTreeView.setOnMouseClicked((MouseEvent mouseEvent) -> {
      if (mouseEvent.getClickCount() == 2) {
        Object test = model.fileTreeView .getSelectionModel().getSelectedItem();
        if (test instanceof RudiFileTreeItem) {
          RudiFileTreeItem item = (RudiFileTreeItem) test;
          RudiTab tab = model.tabPaneBack.getTab(item.getFile());
        }
      }
    });
  }

  public static ImportTreeItem buildTreeView(DataModel model) {

    /* retrieve rootImport from given DataModel */
    Import rootImport = model.ruleModel.rootImport;

    /* build rootItem */
    ImportTreeItem rootItem = new ImportTreeItem(model.ruleModel.rootImport, model);

    /* bind rootItem's properties to the Import */
    rootImport.importNameProperty()
            .bindBidirectional(rootItem.getLabel().textProperty());

    /* iterate over rootImport's children and add them to the rootItem */
    for (Object obj : rootImport.childrenProperty()) {
      rootItem.getChildren().add(buildTreeViewHelper(obj, model));
    }

    /* return the rootItem */
    return rootItem;
  }

  private static BasicTreeItem buildTreeViewHelper(Object unknownObj,
          DataModel model) {

    /* the next object is an Import */
    if (unknownObj instanceof Import) {
      Import newImport = (Import) unknownObj;

      /* build newImportItem */
      ImportTreeItem newImportItem = new ImportTreeItem(newImport, model);

      /* bind newImportItem's properties to the Import */
      newImport.importNameProperty()
              .bindBidirectional(newImportItem.getLabel().textProperty());

      /* iterate over newImport's children and add them to the rootItem */
      for (Object obj : newImport.childrenProperty()) {
        newImportItem.getChildren().add(buildTreeViewHelper(obj, model));
      }
      return newImportItem;
    }

    /* the next object is a Rule */
    if (unknownObj instanceof Rule) {
      Rule newRule = (Rule) unknownObj;

      /* build newRuleItem */
      RuleTreeItem newRuleItem = new RuleTreeItem(newRule, model);

      /* bind newRuleItem's properties to the Rule */
      newRule.ruleNameProperty()
              .bindBidirectional(newRuleItem.getLabel().textProperty());
      newRule.ruleStateProperty()
              .bindBidirectional(newRuleItem.stateProperty());
      newRule.lineProperty()
              .bindBidirectional(newRuleItem.lineProperty());

      /* iterate over newRule's children and add them to the rootItem */
      for (Object obj : newRule.subRuleProperty()) {
        newRuleItem.getChildren().add(buildTreeViewHelper(obj, model));
      }
      return newRuleItem;
    }

    /* our new object is neither Rule nor Import */
    else {
      log.error("tried to read in an object that is not an Import or Rule.");
      return null;
    }
  }

  public static RudiFolderTreeItem buildFileView(DataModel model) {
    return new RudiFolderTreeItem("root");
  }

  /**
   * ****************************
   * The different GUI elements *
   *****************************
   */

  /* The TreeView showing the different rudi rules and imports */
  @FXML
  private TreeView ruleTreeView;

  /* The TreeView showing the content of the rudi folder */
  @FXML
  private TreeView fileTreeView;

}
