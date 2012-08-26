package com.ironalloygames.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import com.ironalloygames.core.PetriDishEmpire;

public class PetriDishEmpireJava {

  public static void main(String[] args) {
    JavaPlatform platform = JavaPlatform.register();
    platform.assets().setPathPrefix("com/ironalloygames/resources");
    
    if(args.length == 2)
    	PlayN.graphics().setSize(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    else
    	PlayN.graphics().setSize(1920, 1200);
    
    platform.setTitle("Empire of the Petri Dish");
    
    PlayN.run(new PetriDishEmpire());
  }
}
