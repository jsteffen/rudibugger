/*
 * Rudibugger is a debugger for .rudi code
 * written in the context of a bachelor's thesis
 * by Christophe Biwer (cbiwer@coli.uni-saarland.de)
 */
package de.dfki.mlt.rudibugger.RPC;

import java.util.ArrayList;
import java.util.Date;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class LogData {

  protected static final int RED = 1;
  protected static final int GREEN = 2;
  protected static final int GRAY = 3;
  protected static final int BLACK = 0;

  private static Date currentDate = new Date();
  private static int timeCounter = 1;

  public class StringPart {
    public String content;
    public int colour;

    private StringPart(String content, int colour)  {
      this.content = content;
      this.colour = colour;
    }
  }

  public class DatePart {
    public Date date;
    public int counter;

    private DatePart(Date date, int counter) {
      this.date = date;
      this.counter = counter;
    }
  }

  public LogData() {
    Date date = new Date();
    if (currentDate.equals(date)) {
      timeCounter++;
    } else {
      currentDate = date;
      timeCounter = 1;
    }
      timestamp = new SimpleObjectProperty<>(new DatePart(date, timeCounter));
  }

  /** the label of this LogData */
  public SimpleObjectProperty<StringPart> label
          = new SimpleObjectProperty<>();

  /** the evaluated data of this LogData */
  public SimpleObjectProperty<ArrayList<StringPart>> evaluated
          = new SimpleObjectProperty<>(new ArrayList<>());

  /** the timestamp of this LogData */
  public SimpleObjectProperty<DatePart> timestamp;

  /** the ruleId of this LogData */
  private int _ruleId;

  public void addStringPart(String content, int colour) {
    if (label.getValue() == null) {
      label.setValue(new StringPart(content, colour));
    } else {
      evaluated.getValue().add(new StringPart(content, colour));
    }
  }

  public void addRuleId(int ruleId) {
    _ruleId = ruleId;
  }

  public int getRuleId() {
    return _ruleId;
  }

}
