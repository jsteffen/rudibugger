package de.dfki.rudibugger.project;

import javafx.scene.control.CheckBoxTreeItem;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Christophe Biwer (yoshegg) christophe.biwer@dfki.de
 */
public class RudiCheckBoxTreeItem extends CheckBoxTreeItem {

  public RudiCheckBoxTreeItem(String label) {
    super(label);
    this.indeterminateProperty().set(true);
  }



}
