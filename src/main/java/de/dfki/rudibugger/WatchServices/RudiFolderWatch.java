/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.rudibugger.WatchServices;
import static de.dfki.mlt.rudimant.common.Constants.*;

import static de.dfki.rudibugger.Constants.*;
import de.dfki.rudibugger.DataModel;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.stream.Stream;
import javafx.application.Platform;
import org.apache.log4j.Logger;

/**
 * This watch's one and only purpose is to check the folder containing .rudi
 * files for changes. If there are changes, a function to refresh the DataModel
 * is called.
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiFolderWatch {

  static Logger log = Logger.getLogger("rudiFolWatch");

  /**
   * the Thread in which the WatchService is run
   */
  private volatile Thread watchingTread;

  /**
   * the corresponding WatchSercie
   */
  private WatchService _watchService;

  /**
   * the DataModel
   */
  private DataModel _model;

  /**
   * start listening for folder changes
   */
  private void startListening() {
    watchingTread = new Thread() {
      @Override
      public void run() {
        try {
          eventLoop();
        } catch (IOException | InterruptedException ex) {
          watchingTread = null;
        }
      }
    };
    watchingTread.setDaemon(true);
    watchingTread.setName("rudiFolderWatchingTread");
    watchingTread.start();
    log.info("RudiFolderWatch has been started.");
  }

  /**
   * stop listening for folder changes
   */
  public void shutDownListener() {
    Thread thr = watchingTread;
    if (thr != null) {
      thr.interrupt();
    }
  }

  public void createRudiFolderWatch(DataModel model) {
    _model = model;

    try {
      _watchService = FileSystems.getDefault().newWatchService();
      model.getRudiFolder().register(_watchService, ENTRY_MODIFY, ENTRY_CREATE,
              ENTRY_DELETE);

      /* iterate over all subdirectories */
      Stream<Path> subpaths = Files.walk(model.getRudiFolder());
      subpaths.forEach(x -> {
        try {
          if (Files.isDirectory(x))
            x.register(_watchService, ENTRY_MODIFY, ENTRY_CREATE,
                    ENTRY_DELETE);
        } catch (IOException ex) {
          log.error("Could not set watch for subdirectories: " + ex);
        }
      });
    } catch (IOException e) {
      log.error("Could not register WatchService: " + e);
    }
    startListening();
  }

  public void eventLoop() throws IOException, InterruptedException {

    for (;;) {

      WatchKey rudiKey;

      /* watch for changes in the rudi folder */
      try {
        rudiKey = _watchService.take();
      } catch (InterruptedException x) {
        return;
      }

      for (WatchEvent<?> event : rudiKey.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
          log.error("An overflow while checking the rudi "
                  + "folder occured.");
          continue;
        }

        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path folder = (Path)rudiKey.watchable();
        Path filename = folder.resolve(ev.context());

        /* are folder's containing files changing? */
        if ((kind == ENTRY_CREATE || kind == ENTRY_DELETE
                || kind == ENTRY_MODIFY)
                && filename.getFileName().toString()
                        .endsWith(RULE_FILE_EXTENSION)) {

          /* rudi file added */
          if (kind == ENTRY_CREATE) {

            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                if (_model.addRudiPath(filename))
                  log.info("rudi file added: " + filename);
              }
            });

          }

          /* rudi file modified */
          if (kind == ENTRY_MODIFY) {
            // TODO: make better
            //log.info("rudi file has been modified : " + filename);
          }

          /* rudi file deleted */
          if (kind == ENTRY_DELETE) {

            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                _model.removeRudiPath(filename);
                log.info("rudi file deleted: " + filename);
              }
            });

          }
          continue;
        }

        /* new folder created? */
        if ((kind == ENTRY_CREATE || kind == ENTRY_DELETE
                || kind == ENTRY_MODIFY) && Files.isDirectory(filename)) {

          /* watch another folder */
          if (kind == ENTRY_CREATE) {
            filename.register(_watchService, ENTRY_MODIFY, ENTRY_CREATE,
                  ENTRY_DELETE);
            _model.addRudiPath(filename);
            log.debug("Started watching new folder: " + filename);
          }
        }

      }


      /* if the watchKey is no longer valid, leave the eventLoop */
      if (rudiKey != null) {
        boolean valid = rudiKey.reset();
        if (!valid) {
          log.debug("watchKey no longer valid. Probably because a watched folder has been deleted");
          log.debug("Restarting RudiFolderWatch...");
          createRudiFolderWatch(_model);
          _model.rudiHierarchy.removeObsoleteFolders();
          break;
        }
      }
    }
  }
}
