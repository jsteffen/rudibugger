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

package de.dfki.mlt.rudibugger.Controller.MenuBar;

import static de.dfki.mlt.rudibugger.Constants.*;
import de.dfki.mlt.rudibugger.HelperWindows;
import de.dfki.mlt.rudibugger.MainApp;
import de.dfki.mlt.rudibugger.DataModel;
import de.dfki.mlt.rudibugger.Project.Project;
import de.dfki.mlt.rudibugger.Project.RuleModel.State.RuleModelState;
import de.dfki.mlt.rudibugger.Project.VondaRuntimeConnection;
import de.dfki.mlt.rudibugger.TabManagement.RudiTab;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller's purpose is to manage the MenuBar and the ToolBar of
 * rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class MenuController {

  static Logger log = LoggerFactory.getLogger("MenuController");

  /** TODO */
  private DataModel _model;

  /** Represents a potentially loaded project. */
  private Project _project;


  /*****************************************************************************
   * EXTENSIONS OF THE MENU CONTROLLER
   ****************************************************************************/

  /**
   * Contains functionality to manage the look and file of the compile button.
   */
  private CompileButtonManager compileButtonManager;

  /**
   * Contains functionality to manage the look and file of the connection
   * button.
   */
  private ConnectionButtonManager connectionButtonManager;


  /*****************************************************************************
   * INITIALIZERS / CONSTRUCTORS
   ****************************************************************************/

  /** Initializes this controller. */
  public void init(DataModel model) {
    _model = model;

    compileButtonManager = CompileButtonManager.init(compileButton,
            toolBar);
    connectionButtonManager = ConnectionButtonManager.init(_model,
            vondaConnectionButton, toolBar);
    listenForProject();
  }

  private void listenForProject() {
    Project.projectLoadedProperty().addListener((o, ov, nv) -> {
      _project = Project.getCurrentProject();

      if (nv) {
        log.debug("Project open: enable GUI-elements.");
        closeProjectItem.setDisable(false);
        newRudiFileItem.setDisable(false);
        loadLoggingStateMenu.setDisable(false);
        saveLoggingStateItem.setDisable(false);
        findInProjectItem.setDisable(false);
        connectionButtonManager.manageLookOfVondaConnectionButton();
        compileButtonManager.defineCompileButton(_project, _project.compiler);
      } else {
        log.debug("Project closed: disable GUI-elements.");
        closeProjectItem.setDisable(true);
        newRudiFileItem.setDisable(true);
        loadLoggingStateMenu.setDisable(true);
        saveLoggingStateItem.setDisable(true);
        findInProjectItem.setDisable(true);
        connectionButtonManager.manageLookOfVondaConnectionButton();
        compileButtonManager.defineCompileButton(_project, _project.compiler);
      }
    });
  }

  /**
   * Initializes the controller.
   */
  public void initController() {


    listenForProject();

    _model.getCurrentProject().vonda.connectedProperty().addListener(l ->
      connectionButtonManager.manageLookOfVondaConnectionButton()
    );

    /* this listener enables saving depending on the selected tab */
    _model.getCurrentProject().getTabStore().currentlySelectedTabProperty().addListener((o, oldVal, newVal) -> {

      /* no tab is opened */
      if (newVal == null) {
        saveItem.setDisable(true);
        saveAsItem.setDisable(true);
        saveAllItem.setDisable(true);

      /* one known tab is selected and can be saved */
      } else if (((RudiTab) newVal).isKnown()) {

        if (newVal.hasBeenModifiedProperty().getValue()) {
          saveItem.setDisable(false);
        } else {
          /* wait until the tab content has been modified */
          newVal.hasBeenModifiedProperty().addListener((o2, oldVal2, newVal2) -> {
            if (newVal2) {
              saveItem.setDisable(false);
            } else {
              saveItem.setDisable(true);
            }
          });
        }

        saveAsItem.setDisable(false);
        saveAllItem.setDisable(false);


      /* a newly created file can only be saved as */
      } else if (! ((RudiTab) newVal).isKnown()) {
        saveItem.setDisable(true);
        saveAsItem.setDisable(false);
        saveAllItem.setDisable(false);
      }
    });

    /* initalize the recent projets submenu... */
    if (! _model.globalConf.recentProjects.isEmpty()) {
      buildRecentProjectsMenu();
    }

    /* ... then keep track of changes */
    _model.globalConf.recentProjects.addListener(
            (ListChangeListener.Change<? extends String> c) -> {
      buildRecentProjectsMenu();
    });
  }

  private void buildRecentProjectsMenu() {
    openRecentProjectMenu.getItems().clear();
    _model.globalConf.recentProjects.forEach((x) -> {
      MenuItem mi = new MenuItem(x);
      mi.setOnAction((event) -> {
        checkForOpenProject();
      });
      openRecentProjectMenu.getItems().add(mi);
    });
  }

  /**
   * Builds the menu offering to load the 10 most recent RuleModelState
   * configurations.
   */
  @FXML
  private void buildLoadRuleSelectionStateMenu() {
    if (_project == null) return;
    RuleModelState rms = _model.getCurrentProject().getRuleModel()
            .getRuleModelState();
    if (!rms.getRecentStates().isEmpty()) {
      loadLoggingStateMenu.getItems().clear();
      rms.getRecentStates().forEach((x) -> {
        String filenameWithFolder = _model.getCurrentProject().getRuleModelStatesFolder()
                .relativize(x).toString();
        MenuItem mi = new MenuItem(filenameWithFolder);
        mi.setOnAction((event) -> {
          rms.loadState(x);
        });
        loadLoggingStateMenu.getItems().add(mi);
      });
    } else {
      loadLoggingStateMenu.getItems().clear();
      loadLoggingStateMenu.getItems().add(noRecentConfigurationFound);
    }
    loadLoggingStateMenu.getItems().add(new SeparatorMenuItem());
    loadLoggingStateMenu.getItems().add(openRuleLoggingStateItem);
  }

  /**
   * This function is used to check for open projects
   * TODO: Should probably be somewhere else
   *
   * @param ymlFile null, if the project has not been defined yet, else the Path
   * to the project's .yml file
   */
  private boolean checkForOpenProject() {

    /* a project is already open */
    if (_model.isProjectLoadedProperty().getValue() == PROJECT_OPEN) {
      if (OVERWRITE_PROJECT == HelperWindows.openOverwriteProjectCheckDialog(
        _model.getCurrentProject().getProjectName())) {
        return true;
      } else return false;
    }
    return true;
  }


  /*****************************************************************************
   * TOOLBAR BUTTONS
   ****************************************************************************/

  /* Represents the compile button. */
  @FXML
  private Button compileButton;

  /**
   * Represents the con-/disconnect button also monitoring the connection state.
   */
  @FXML
  private Button vondaConnectionButton;


  /*****************************************************************************
   * Menu items actions (from menu bar)
   ****************************************************************************/

  @FXML
  private void findInProject(ActionEvent event) {
    HelperWindows.openSearchWindow(_model.mainStage, _model.getCurrentProject());
  }

  @FXML
  private MenuItem findInProjectItem;

  /********* File *********/


  /** MenuItem "New Project..." */
  @FXML
  private MenuItem newProjectItem;

  /** Action "New Project..." */
  @FXML
  private void newProjectAction(ActionEvent event) {
    _model.createNewProject();
  }


  /** MenuItem "New rudi File..." */
  @FXML
  private MenuItem newRudiFileItem;

  /** Action "New rudi File..." */
  @FXML
  private void newRudiFileAction(ActionEvent event)
          throws FileNotFoundException {
    _model.getCurrentProject().openFile(null);
  }


  /** MenuItem "Open Project..." */
  @FXML
  private MenuItem openProjectItem;

  /** Action "Open Project..." */
  @FXML
  private void openProjectAction(ActionEvent event)
          throws FileNotFoundException, IOException, IllegalAccessException {
    if (checkForOpenProject()) {
      Path projectYml = HelperWindows.openYmlProjectFileDialog(_model.mainStage);
      _model.openProject(projectYml);
    }
  }


  /** Menu "Open Recent Project" */
  @FXML
  private Menu openRecentProjectMenu;


  /** MenuItem "Close Project" */
  @FXML
  private MenuItem closeProjectItem;

  /** Action "Close Project" */
  @FXML
  private void closeProjectAction(ActionEvent event)
          throws FileNotFoundException {
    _model.closeProject();
  }


  /** MenuItem "No recent configuration found. */
  @FXML
  private MenuItem noRecentConfigurationFound;

  /** MenuItem "Open configuration file... */
  @FXML
  private MenuItem openRuleLoggingStateItem;

  /** Menu "Load logging state" */
  @FXML
  private Menu loadLoggingStateMenu;

  /** Action "Open configuration file..." */
  @FXML
  private void openRuleLoggingStateConfigurationFile(ActionEvent event) {
    Path saveFolder = _model.getCurrentProject().getRuleModelStatesFolder();
    Path chosenFile = HelperWindows.openRuleLoggingStateFileDialog(
            _model.mainStage, saveFolder);
    if (chosenFile == null) return;
    _model.getCurrentProject().getRuleModel().getRuleModelState()
            .loadState(chosenFile);
  }

  /** MenuItem "Save logging state..." */
  @FXML
  private MenuItem saveLoggingStateItem;

  /** Action "Save logging state" */
  @FXML
  private void saveLoggingStateAction(ActionEvent event) {
    Path saveFolder = _model.getCurrentProject().getRuleModelStatesFolder();
    Path newStateFile = HelperWindows.openSaveRuleModelStateDialog(
            _model.mainStage, saveFolder);
    _model.getCurrentProject().getRuleModel().getRuleModelState()
            .saveRequestProperty().set(newStateFile);
  }


  /** MenuItem "Save" */
  @FXML
  private MenuItem saveItem;

  /** Action "Save" */
  @FXML
  private void saveAction(ActionEvent event) {
    _model.getCurrentProject().quickSaveFile(
            _model.getCurrentProject().getTabStore().currentlySelectedTabProperty().get());
  }


  /** MenuItem "Save as..." */
  @FXML
  private MenuItem saveAsItem;

  /** Action "Save as..." */
  @FXML
  private void saveAsAction(ActionEvent event) {
    RudiTab currentTab = _model.getCurrentProject().getTabStore().currentlySelectedTabProperty().get();


//    _model.getCurrentProject().saveFileAs( // TODO
//            _model.getCurrentProject().getTabStore().currentlySelectedTabProperty().get());
  }

  /**
   * Save tab's content into a new file.
   * TODO: Should be somewhere else
   * @return True, if the file has been successfully saved, else false
   */
  public boolean saveFileAs(RudiTab tab) {
    Project project = _model.getCurrentProject();
    String content = tab.getRudiCode();

    Path newRudiFile = HelperWindows.openSaveNewFileAsDialog(
            _model.mainStage, project.getRudiFolder());

    if (project.saveFile(newRudiFile, content)) {
      tab.setText(newRudiFile.getFileName().toString());
      project.getTabStore().openTabsProperty().get().remove(tab.getFile());
      tab.setFile(newRudiFile);
      project.getTabStore().openTabsProperty().get().put(newRudiFile, tab);
      tab.waitForModifications();

      log.debug("File " + newRudiFile.getFileName() + " has been saved.");
      return true;
    }
    return false;
  }


  /** MenuItem "Save all" */
  @FXML
  private MenuItem saveAllItem;

  /** Action "Save all" */
  @FXML
  private void saveAllAction(ActionEvent event) {
    _model.getCurrentProject().quickSaveAllFiles();
  }


  /** MenuItem "Exit" */
  @FXML
  private MenuItem exitItem;

  /** Action "Exit" */
  @FXML
  private void exitAction(ActionEvent event) {
    _model.layout.saveLayoutToFile();
    MainApp.exitRudibugger();
  }


  /********* Tools *********/
  @FXML
  private void openSettingsDialog(ActionEvent event) {
    HelperWindows.showSettingsWindow(_model.mainStage, _model.globalConf, _model.emacs);
  }

  /********* Help *********/
  @FXML
  private void openAboutWindow(ActionEvent event) {
    HelperWindows.showAboutWindow(_model.mainStage);
  }


  /*****************************************************************************
   * Button actions & toolBar
  *****************************************************************************/

  /** Contains buttons */
  @FXML
  private ToolBar toolBar;

  /** Establishes a connection to the VOnDA server or disconnects from it. */
  @FXML
  private void changeVondaConnectionState(ActionEvent event) {
    VondaRuntimeConnection vonda = _model.getCurrentProject().vonda;
    int conStatus = vonda.connectedProperty().get();
    if (conStatus == DISCONNECTED_FROM_VONDA)
      vonda.connect(_model.getCurrentProject().getVondaPort());
    else
      vonda.closeConnection();
  }

}
