package com.ironalloygames.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import com.ironalloygames.core.PetriDishEmpire;

public class PetriDishEmpireJava {

  public static void main(String[] args) {
    JavaPlatform platform = JavaPlatform.register();
    platform.assets().setPathPrefix("com/ironalloygames/resources");
    PlayN.graphics().setSize(1920, 1200);
    PlayN.run(new PetriDishEmpire());
  }
}
