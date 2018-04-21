/*
 * The Creative Commons CC-BY-NC 4.0 License
 *
 * http://creativecommons.org/licenses/by-nc/4.0/legalcode
 *
 * Creative Commons (CC) by DFKI GmbH
 *  - Bernd Kiefer <kiefer@dfki.de>
 *  - Anna Welker <anna.welker@dfki.de>
 *  - Christophe Biwer <christophe.biwer@dfki.de>
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package de.dfki.mlt.rudibugger.Controller;

import de.dfki.mlt.rudimant.common.BasicInfo;
import de.dfki.mlt.rudimant.common.ImportInfo;
import de.dfki.mlt.rudimant.common.RuleInfo;
import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.FileTreeView.RudiTreeCell;
import de.dfki.mlt.rudibugger.FileTreeView.RudiPath;
import de.dfki.mlt.rudibugger.RuleTreeView.BasicInfoTreeCell;
import de.dfki.mlt.rudibugger.RuleModel.ImportInfoExtended;
import de.dfki.mlt.rudibugger.RuleModel.RuleInfoExtended;
import de.dfki.mlt.rudibugger.RuleTreeView.RuleModelComplete;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * This controller manages the left part of rudibugger window:
 * the TreeView of files, the TreeView of rules and some buttons.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class SideBarController {

  /** the logger of the SideBarController */
  static Logger log = LoggerFactory.getLogger("rudiLog");

  /** the DataModel */
  private DataModel _model;

  /**
   * the RuleModelComplete, needed to save and load the expansion and rule
 logging state of the RuleTreeView
   */
  private RuleModelComplete ruleTreeViewState;

  /**
   * This function connects this controller to the DataModel
   *
   * @param model
   */
  public void initModel(DataModel model) {
    if (this._model != null) {
      throw new IllegalStateException("Model can only be initialized once");
    }
    this._model = model;

    /* this Listener keeps the rudiTreeView containing the .rudi files up to date */
    model.projectStatusProperty().addListener((o, oldVal, newVal) -> {
      switch ((int) newVal) {
        case PROJECT_OPEN:
          rudiTreeView.setRoot(model.rudiHierarchy._root);
          rudiTreeView.setShowRoot(false);
          break;
        case PROJECT_CLOSED:
          rudiTreeView.setRoot(null);
          break;
      }
    });

    /* define how a cell in this rudiTreeView looks like */
    rudiTreeView.setCellFactory(value -> new RudiTreeCell());

    /* open a new tab or select the already opened tab from the selected file */
    rudiTreeView.setOnMouseClicked((MouseEvent mouseEvent) -> {
      if (mouseEvent.getClickCount() == 2) {
        TreeItem ti = (TreeItem) rudiTreeView.getSelectionModel().getSelectedItem();
        RudiPath rp = (RudiPath) ti.getValue();
        if (! Files.isDirectory(rp.getPath())) {
          model.rudiLoad.openFile(rp.getPath());
        }
      }
    });

    /* define how a cell in the ruleTreeView looks like */
    ruleTreeView.setCellFactory(value -> new BasicInfoTreeCell());


    /* this Listener builds or modifies the RuleTreeView, if the RuleModel
    was changed.*/
    model.ruleModel.changedStateProperty().addListener((o, oldVal, newVal) -> {
      switch ((int) newVal) {
        case RULE_MODEL_NEWLY_CREATED:
          log.debug("RuleModel has been found.");
          log.debug("Building TreeView...");
          ruleTreeView.setRoot(buildTreeView(model));
          ruleTreeView.getRoot().setExpanded(true);
          ruleTreeViewState = new RuleModelComplete();
          log.debug("TreeView based on RuleModel has been built.");
          log.debug("Marking used .rudi files...");
          markFilesInRudiList();
          log.debug("Marked used .rudi files.");
          model.ruleModel.setChangedStateProperty(RULE_MODEL_UNCHANGED);
          break;
        case RULE_MODEL_CHANGED:
          log.debug("RuleModel has been modified.");
          log.debug("Adapting ruleTreeView");
          ruleTreeViewState.retrieveTreeState(ruleTreeView);
          ruleTreeView.setRoot(buildTreeView(model));
          ruleTreeViewState.setTreeState(ruleTreeView);
          log.debug("ruleTreeView has been adapted.");
          log.debug("Remarking used .rudi files...");
          markFilesInRudiList();
          log.debug("Remarked used .rudi files.");
          model.ruleModel.setChangedStateProperty(RULE_MODEL_UNCHANGED);
          break;
        case RULE_MODEL_REMOVED:
          log.debug("RuleModel has been resetted / removed");
          ruleTreeView.setRoot(null);
          // TODO: reset file view
          log.debug("GUI has been resetted.");
          model.ruleModel.setChangedStateProperty(RULE_MODEL_UNCHANGED);
          break;
        case RULE_MODEL_UNCHANGED:
          break;
        default:
          break;
      }
    });

    /* Listen to request for saving ruleLoggingState */
    model.ruleLoggingStateSaveRequestProperty().addListener((o, ov, nv) -> {
      if (nv) {
        log.debug("Requested to save ruleLoggingState.");
        ruleTreeViewState.retrieveTreeState(ruleTreeView);
        model.saveRuleLoggingState(ruleTreeViewState);
        model.resetRuleLoggingStateSaveRequestProperty();
      }
    });

    /* Listen to request for loading ruleLoggingState */
    _model.ruleLoggingStateLoadRequestProperty().addListener((o, ov, nv) -> {
      if (nv == null) return;
      RuleModelComplete rtvs;
      try {
        Yaml yaml = new Yaml();
        rtvs = (RuleModelComplete) yaml.load(new FileReader(nv.toFile()));
      } catch (FileNotFoundException e) {
        log.error("Could not read in configuration file");
        return;
      }
      ruleTreeViewState = rtvs;
      ruleTreeViewState.setTreeState(ruleTreeView);

      /* reset this listener */
      _model.ruleLoggingStateLoadRequestProperty().set(null);
    });

    /* Listen to out of sync changes */
    // TODO: Should not be needed, but seems to be a bug in JavaFX
//    model._modifiedFilesProperty().addListener((o, ov, nv) -> {
//      rudiTreeView.refresh();
//    });
  }

  /**
   * This function is used to mark the files in the <b>rudiList</b> according to
   * their state.
   */
  private void markFilesInRudiList() {
    for (RudiPath x : _model.rudiHierarchy.rudiPathSet) {

      /* mark the main .rudi file, must be in root folder */
      if (_model.ruleModel.getRootImport().getAbsolutePath().getFileName().equals(
              x.getPath().getFileName())) {
        x._usedProperty().setValue(FILE_IS_MAIN);
        continue;
      }

      /* mark the wrapper file,  must be in root folder */
      if (_model.project.getWrapperClass().getFileName()
              .equals(x.getPath().getFileName())) {
        x._usedProperty().setValue(FILE_IS_WRAPPER);
        continue;
      }

      /* mark the other files */
      if (_model.ruleModel.getImportSet().contains(x.getPath())) {
        x._usedProperty().setValue(FILE_USED);
      } else {
        x._usedProperty().setValue(FILE_NOT_USED);
      }
    }

    /* let the cells reload according to their usage state */
    // TODO: https://stackoverflow.com/questions/14682881/binding-image-in-javafx
    // Should not be needed anymore
    //    rudiTreeView.refresh();
  }

  public static TreeItem buildTreeView(DataModel model) {

    ImportInfoExtended root = model.ruleModel.getRootImport();

    /* build rootItem */
    TreeItem<ImportInfoExtended> rootItem = new TreeItem(root);

    /* iterate over rootImport's children and add them to the rootItem */
    for (BasicInfo obj : root.getChildren()) {
      rootItem.getChildren().add(buildTreeViewHelper(obj, model, root));
    }

    /* return the rootItem */
    return rootItem;
  }

  private static TreeItem buildTreeViewHelper(BasicInfo unknownObj,
          DataModel model, BasicInfo parent) {

    /* the next object is an Import */
    if (unknownObj instanceof ImportInfo) {
      ImportInfoExtended newImport = (ImportInfoExtended) unknownObj;

      /* build newImportItem */
      TreeItem<ImportInfoExtended> newImportItem = new TreeItem(newImport);

      /* iterate over newImport's children and add them to the rootItem */
      for (BasicInfo obj : newImport.getChildren()) {
        newImportItem.getChildren().add(buildTreeViewHelper(obj, model, newImport));
      }
      return newImportItem;
    }

    /* the next object is a Rule */
    if (unknownObj instanceof RuleInfo) {
      RuleInfoExtended newRule = (RuleInfoExtended) unknownObj;

      /* build newRuleItem */
      TreeItem<RuleInfoExtended> newRuleItem = new TreeItem(newRule);

//      /* bind newRuleItem's properties to the Rule */
//      newRuleItem.setState(newRule.getState());

      /* iterate over newRule's children and add them to the rootItem */
      for (BasicInfo obj : newRule.getChildren()) {
        newRuleItem.getChildren().add(buildTreeViewHelper(obj, model, newRule));
      }
      return newRuleItem;
    }

    /* our new object is neither Rule nor Import, this should never happen */
    else {
      log.error("tried to read in an object that is not an Import or a Rule.");
      return null;
    }
  }

  private static void expandTreeItem(TreeItem item) {
    item.setExpanded(true);
    item.getChildren().forEach((child) -> {
      expandTreeItem((TreeItem) child);
    });
  }

  private static void collapseTreeItem(TreeItem item) {
    item.setExpanded(false);
    item.getChildren().forEach((child) -> {
      collapseTreeItem((TreeItem) child);
    });
  }


  /*****************************************************************************
   * The different GUI elements *
   ****************************************************************************/

  /* The TreeView showing the different rudi rules and imports */
  @FXML
  private TreeView ruleTreeView;

  /* The ListView showing the content of the rudi folder */
  @FXML
  private TreeView rudiTreeView;


  /*****************************************************************************
   * GUI ACTIONS
   ****************************************************************************/

  /** Expand all TreeItems */
  @FXML
  private void expandAll(ActionEvent event) {
    if (ruleTreeView.getRoot() != null) {
      expandTreeItem((TreeItem) ruleTreeView.getRoot());
    }
  }

  /** Collapse all TreeItems (Except for the root) */
  @FXML
  private void collapseAll(ActionEvent event) {
    if (ruleTreeView.getRoot() != null) {
      collapseTreeItem((TreeItem) ruleTreeView.getRoot());
      ruleTreeView.getRoot().setExpanded(true);
    }
  }

}
