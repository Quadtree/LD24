package com.ironalloygames.android;

import playn.android.GameActivity;
import playn.core.PlayN;

import com.ironalloygames.core.PetriDishEmpire;

public class PetriDishEmpireActivity extends GameActivity {

  @Override
  public void main(){
    platform().assets().setPathPrefix("com/ironalloygames/resources");
    PlayN.run(new PetriDishEmpire());
  }
}
