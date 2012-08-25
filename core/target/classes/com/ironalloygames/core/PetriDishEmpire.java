package com.ironalloygames.core;

import static playn.core.PlayN.*;

import java.util.ArrayList;
import java.util.Random;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import playn.core.Color;
import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.ImmediateLayer.Renderer;
import playn.core.Surface;

public class PetriDishEmpire implements Game {
	
	public Camera cam;
	public World world;
	public Random rand;
	
	public ArrayList<Creature> creatures = new ArrayList<Creature>();
	
	public static PetriDishEmpire s;
	
	int fps = 0;
	long lastSecond = 0;
	
	@Override
	public void init() {
		s = this;
		
		cam = new Camera();
		
		ImmediateLayer il = graphics().createImmediateLayer(new Renderer(){

			@Override
			public void render(Surface surface) {
				cam.setCamera(new Vec2(0,0), 5);
				
				surface.clear();
				cam.surf = surface;
				
				for(Creature c : creatures)
				{
					c.render(cam);
				}
				
				fps++;
				
				if(System.currentTimeMillis() / 1000 != lastSecond)
				{
					lastSecond = System.currentTimeMillis() / 1000;
					System.out.println("FPS: " + fps);
					fps = 0;
				}
			}
		});
	  
		graphics().rootLayer().add(il);
		
		world = new World(new Vec2(0,0), true);
		
		rand = new Random();
		
		ArrayList<Gene> genes = new ArrayList<Gene>();
		genes.add(new Gene(Gene.GT_ENGINE, 2, 0, 0));
		genes.add(new Gene(Gene.GT_STRUCTURE, 2, 1, 0));
		genes.add(new Gene(Gene.GT_WEAPON, 2, 1, 0));
		genes.add(new Gene(Gene.GT_ARMOR, 2.3f, -1, 1));
		
		Genome genome = new Genome(genes, 3);
		
		Creature crt = new Creature(new Vec2(0,0), genome, true);
		Creature crt2 = new Creature(new Vec2(20,0), genome, false);
		
		creatures.add(crt);
		creatures.add(crt2);
		
		crt.setMoveTarget(new Vec2(20,0));
	}

	@Override
	public void paint(float alpha) {
	}

	@Override
	public void update(float delta) {
		
		for(Creature c : creatures)
			c.update();
		
		world.step(delta, 8, 8);
	}

	@Override
	public int updateRate() {
		return 60;
	}	
}
