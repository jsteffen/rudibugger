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

package de.dfki.mlt.rudibugger;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * For convenience this class contains constants that can be used anywhere in
 * rudibugger.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class Constants {

  /* Default values of some essential files and folders. */
  public static Path PATH_TO_RUDI_FOLDER = Paths.get("src/main/rudi/");
  public static Path PATH_TO_GENERATED_FOLDER
    = Paths.get("src/main/resources/generated");
  public static String COMPILE_FILE = "compile";
  public static String RUN_FILE = "run.sh";
  public static Path GLOBAL_CONFIG_FILE
    = Paths.get(System.getProperty("user.home"), ".config", "rudibugger",
      "rudibuggerConfiguration.yml");
  public static Path RECENT_PROJECTS_FILE
    = Paths.get(System.getProperty("user.home"), ".config", "rudibugger",
      "recentProjects.yml");
  public static Path GLOBAL_CONFIG_PATH
    = Paths.get(System.getProperty("user.home"), ".config", "rudibugger");

  /* Mark and signalize the user's request of a new project. */
  public static final int OVERWRITE_CHECK_CANCEL = 0;
  public static final int OVERWRITE_CHECK_CURRENT_WINDOW = 1;
  public static final int OVERWRITE_CHECK_NEW_WINDOW = 2;

  /* Mark and signalize the state of the RuleModel. */
  public static final int RULE_MODEL_UNCHANGED = 0;
  public static final int RULE_MODEL_NEWLY_CREATED = 1;
  public static final int RULE_MODEL_CHANGED = 2;
  public static final int RULE_MODEL_REMOVED = 9;

  /* Signalize the opening or closing of a project. */
  public static final int PROJECT_OPEN = 1;
  public static final int PROJECT_CLOSED = 0;

  /* Mark the usage state of a file in a project. */
  public static final int FILE_USED = 1;
  public static final int FILE_NOT_USED = 0;
  public static final int FILE_IS_MAIN = 2;
  public static final int FILE_IS_WRAPPER = 3;
  public static final int IS_FOLDER = 9;

  /* Compilation state of .rudi files. */
  public static final int COMPILATION_PERFECT = 1;
  public static final int COMPILATION_WITH_ERRORS = 2;
  public static final int COMPILATION_WITH_WARNINGS = 3;
  public static final int COMPILATION_FAILED = 4;
  public static final int COMPILATION_UNDEFINED = 5;

  /* Modification state of .rudi files. */
  public static final int FILES_SYNCED = 10;
  public static final int FILES_OUT_OF_SYNC = 20;
  public static final int FILES_SYNC_UNDEFINED = 30;

}
