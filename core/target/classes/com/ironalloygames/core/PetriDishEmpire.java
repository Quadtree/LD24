package com.ironalloygames.core;

import static playn.core.PlayN.*;

import playn.core.Color;
import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.ImmediateLayer.Renderer;
import playn.core.Surface;

public class PetriDishEmpire implements Game {
  @Override
  public void init() {
    // create and add background image layer
    //Image bgImage = assets().getImage("images/bg.png");
    //ImageLayer bgLayer = graphics().createImageLayer(bgImage);
    //graphics().rootLayer().add(bgLayer);
	  
	  ImmediateLayer il = graphics().createImmediateLayer(new Renderer(){

		@Override
		public void render(Surface surface) {
			surface.clear();
			//surface.setFillColor(Color.rgb(255, 0, 0));
			surface.setFillColor(0xffffffff);
			surface.drawLine(5, 5, 20, 20, 1);
		}
	  });
	  
	  graphics().rootLayer().add(il);
  }

  @Override
  public void paint(float alpha) {
    // the background automatically paints itself, so no need to do anything here!
  }

  @Override
  public void update(float delta) {
  }

  @Override
  public int updateRate() {
    return 25;
  }
}
