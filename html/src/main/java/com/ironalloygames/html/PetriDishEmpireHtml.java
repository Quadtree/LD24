package com.ironalloygames.html;

import java.util.List;
import java.util.Map;

import playn.core.PlayN;
import playn.html.HtmlGame;
import playn.html.HtmlPlatform;

import com.google.gwt.user.client.Window;
import com.ironalloygames.core.PetriDishEmpire;

public class PetriDishEmpireHtml extends HtmlGame {

  @Override
  public void start() {
    HtmlPlatform platform = HtmlPlatform.register();
    platform.assets().setPathPrefix("pde/");
    
    int width = 800;
    int height = 600;
    
    Map<String, List<String>> paramMap = Window.Location.getParameterMap();
    
    if(paramMap.containsKey("w")) width = Integer.parseInt(paramMap.get("w").get(0));
    if(paramMap.containsKey("h")) height = Integer.parseInt(paramMap.get("h").get(0));
    
    PlayN.graphics().setSize(width, height);
    PlayN.run(new PetriDishEmpire());
  }
}
