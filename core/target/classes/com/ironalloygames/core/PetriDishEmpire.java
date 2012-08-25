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
import playn.core.Mouse;
import playn.core.Mouse.ButtonEvent;
import playn.core.Mouse.Listener;
import playn.core.Mouse.MotionEvent;
import playn.core.Mouse.WheelEvent;
import playn.core.PlayN;
import playn.core.Surface;

public class PetriDishEmpire implements Game, Listener {
	
	public Camera cam;
	public World world;
	public Random rand;
	
	public ArrayList<Creature> creatures = new ArrayList<Creature>();
	
	public static PetriDishEmpire s;
	
	int fps = 0;
	long lastSecond = 0;
	
	Vec2 mouseScreenPos = new Vec2();
	
	Vec2 mouseDownRealPos = null;
	
	final static float BAND_SELECT_THRESH = 2;
	
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
				
				if(mouseDownRealPos != null)
				{
					Vec2 mousePos = cam.screenToReal(mouseScreenPos);
					
					if(mousePos.sub(mouseDownRealPos).length() >= BAND_SELECT_THRESH)
					{
						cam.drawLine(mousePos, new Vec2(mousePos.x, mouseDownRealPos.y), 0xFFFFFFFF);
						cam.drawLine(mousePos, new Vec2(mouseDownRealPos.x, mousePos.y), 0xFFFFFFFF);
						
						cam.drawLine(mouseDownRealPos, new Vec2(mousePos.x, mouseDownRealPos.y), 0xFFFFFFFF);
						cam.drawLine(mouseDownRealPos, new Vec2(mouseDownRealPos.x, mousePos.y), 0xFFFFFFFF);
					}
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
		
		PlayN.mouse().setListener(this);
	}

	@Override
	public void paint(float alpha) {
	}

	@Override
	public void update(float delta) {
		
		Vec2 mousePos = cam.screenToReal(mouseScreenPos);
		
		for(Creature c : creatures)
		{
			c.update();
			
			if(c.isInBoundingBox(mousePos) && c.playerOwned) c.mouseHover = true;
		}
		
		world.step(delta, 12, 12);
	}

	@Override
	public int updateRate() {
		return 60;
	}

	@Override
	public void onMouseDown(ButtonEvent event) {
		if(event.button() == Mouse.BUTTON_LEFT)
		{
			mouseScreenPos = new Vec2(event.x(), event.y());
			
			Vec2 mousePos = cam.screenToReal(mouseScreenPos);
			
			mouseDownRealPos = new Vec2(mousePos);
		}
	}

	@Override
	public void onMouseUp(ButtonEvent event) {
		if(event.button() == Mouse.BUTTON_LEFT)
		{
			Vec2 mousePos = cam.screenToReal(mouseScreenPos);
			
			for(Creature c : creatures)
			{
				c.selected = false;
			}
			
			if(mousePos.sub(mouseDownRealPos).length() < BAND_SELECT_THRESH)
			{
				for(Creature c : creatures)
				{
					if(c.isInBoundingBox(mousePos) && c.playerOwned) c.selected = true;
				}
			} else {
				Vec2 ul = new Vec2(Math.min(mousePos.x, mouseDownRealPos.x), Math.min(mousePos.y, mouseDownRealPos.y));
				Vec2 lr = new Vec2(Math.max(mousePos.x, mouseDownRealPos.x), Math.max(mousePos.y, mouseDownRealPos.y));
				
				for(Creature c : creatures)
				{
					if(c.playerOwned)
					{
						if(c.body.getPosition().x >= ul.x &&
						   c.body.getPosition().y >= ul.y &&
						   c.body.getPosition().x <= lr.x &&
						   c.body.getPosition().y <= lr.y)
						{
							c.selected = true;
						}
					}
				}
				
				
			}
			
			mouseDownRealPos = null;
		}
	}

	@Override
	public void onMouseMove(MotionEvent event) {
		mouseScreenPos = new Vec2(event.x(), event.y());
	}

	@Override
	public void onMouseWheelScroll(WheelEvent event) {
		// TODO Auto-generated method stub
		
	}	
}
