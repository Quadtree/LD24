package com.ironalloygames.java;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import com.ironalloygames.core.DisplayCallback;
import com.ironalloygames.core.PetriDishEmpire;

public class PetriDishEmpireJava {

  public static void main(String[] args) {
    JavaPlatform platform = JavaPlatform.register();
    platform.assets().setPathPrefix("com/ironalloygames/resources");
    
    if(args.length >= 2)
    	PlayN.graphics().setSize(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    else
    	PlayN.graphics().setSize(1920, 1200);
    
    if(args.length == 3)
    	PetriDishEmpire.dishHalfsize = Integer.parseInt(args[2]);
    
    platform.setTitle("Empire of the Petri Dish");
    
    /*try {
		Display.setFullscreen(true);
	} catch (LWJGLException e) {
		e.printStackTrace();
	}*/
    
    PetriDishEmpire pde = new PetriDishEmpire();
    
    pde.callback = new DisplayCallback(){

		@Override
		public void call() {
			try {
				Display.setFullscreen(true);
				
				Display.setTitle("TESSSST");
			} catch (LWJGLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	platform.
    
    PlayN.run(pde);
  }
}
