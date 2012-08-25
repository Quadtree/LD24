package com.ironalloygames.html;

import playn.core.PlayN;
import playn.html.HtmlGame;
import playn.html.HtmlPlatform;

import com.ironalloygames.core.PetriDishEmpire;

public class PetriDishEmpireHtml extends HtmlGame {

  @Override
  public void start() {
    HtmlPlatform platform = HtmlPlatform.register();
    platform.assets().setPathPrefix("pde/");
    PlayN.run(new PetriDishEmpire());
  }
}
