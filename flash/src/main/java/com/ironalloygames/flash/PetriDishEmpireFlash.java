package com.ironalloygames.flash;

import playn.core.PlayN;
import playn.flash.FlashGame;
import playn.flash.FlashPlatform;
import com.ironalloygames.core.PetriDishEmpire;

public class PetriDishEmpireFlash extends FlashGame {

  @Override
  public void start() {
    FlashPlatform platform = FlashPlatform.register();
    platform.assets().setPathPrefix("pde/");
    
    int width = 800;
    int height = 600;
    
    /*Map<String, List<String>> paramMap = Window.Location.getParameterMap();
    
    if(paramMap.containsKey("w")) width = Integer.parseInt(paramMap.get("w").get(0));
    if(paramMap.containsKey("h")) height = Integer.parseInt(paramMap.get("h").get(0));*/
    
    PlayN.graphics().setSize(width, height);
    PlayN.run(new PetriDishEmpire());
  }
}
