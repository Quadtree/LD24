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
	
	@Override
	public void init() {
		s = this;
		
		cam = new Camera();
		
		ImmediateLayer il = graphics().createImmediateLayer(new Renderer(){

			@Override
			public void render(Surface surface) {
				surface.clear();
				cam.surf = surface;
				
				for(Creature c : creatures)
				{
					c.render(cam);
				}
			}
		});
	  
		graphics().rootLayer().add(il);
		
		world = new World(new Vec2(0,0), false);
		
		rand = new Random();
		
		ArrayList<Gene> genes = new ArrayList<Gene>();
		genes.add(new Gene(Gene.GT_STRUCTURE, 2, 0, 0));
		genes.add(new Gene(Gene.GT_STRUCTURE, 2, 1, 0));
		genes.add(new Gene(Gene.GT_STRUCTURE, 2, 1, 0));
		genes.add(new Gene(Gene.GT_STRUCTURE, 2, -1, 1));
		
		Genome genome = new Genome(genes, 4);
		
		Creature crt = new Creature(new Vec2(100,100), genome);
		
		creatures.add(crt);
	}

	@Override
	public void paint(float alpha) {
	}

	@Override
	public void update(float delta) {
		world.step(delta, 4, 4);
	}

	@Override
	public int updateRate() {
		return 60;
	}	
}
