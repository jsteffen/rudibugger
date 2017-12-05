/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RPC;

import de.dfki.mlt.rudibugger.RPC.LogData.StringPart;
import java.util.HashMap;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import static de.dfki.mlt.rudibugger.RPC.LogData.*;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class LogDataCell extends ListCell<LogData> {

  public static HashMap<Integer, Color> colourMap
    = new HashMap<Integer, Color>() {{
      put(RED, Color.RED);
      put(GREEN, Color.GREEN);
      put(GRAY, Color.GRAY);
    }};

  @Override
  protected void updateItem(LogData item, boolean empty) {
    super.updateItem(item, empty);

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      Text text = new Text("Test");
      text.setFill(Color.RED);
      TextFlow textFlow = new TextFlow(text);
      for (StringPart x : item.text) {
        Text t = new Text(x.content);
        t.setFill(colourMap.get(x.colour));
        textFlow.getChildren().add(t);
      }
      setGraphic(textFlow);
      }
    }
  }
