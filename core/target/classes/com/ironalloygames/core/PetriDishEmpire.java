package com.ironalloygames.core;

import static playn.core.PlayN.*;

import java.util.ArrayList;
import java.util.Random;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import com.ironalloygames.core.piece.Piece;

import playn.core.Color;
import playn.core.Game;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Key;
import playn.core.ImmediateLayer.Renderer;
import playn.core.Keyboard.Event;
import playn.core.Keyboard.TextType;
import playn.core.Keyboard.TypedEvent;
import playn.core.Mouse;
import playn.core.Mouse.ButtonEvent;
import playn.core.Mouse.Listener;
import playn.core.Mouse.MotionEvent;
import playn.core.Mouse.WheelEvent;
import playn.core.PlayN;
import playn.core.Surface;
import playn.core.util.Callback;

public class PetriDishEmpire implements Game, Listener, playn.core.Keyboard.Listener, ContactListener {
	
	public Camera cam;
	public World world;
	public Random rand;
	
	public ArrayList<Creature> creatures = new ArrayList<Creature>();
	
	public static PetriDishEmpire s;
	
	int fps = 0;
	long lastSecond = 0;
	
	Vec2 mouseScreenPos = new Vec2();
	
	Vec2 mouseDownRealPos = null;
	
	boolean shiftKeyDown = false;
	
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
				
				
			}
		});
	  
		graphics().rootLayer().add(il);
		
		world = new World(new Vec2(0,0), true);
		
		rand = new Random();
		
		for(int i=0;i<6;++i)
		{
			creatures.add(new Creature(new Vec2((i % 2) * 40, (i / 2) * 40 - 50), new Genome(), true));
		}
		
		creatures.add(new Creature(new Vec2(100,100), new Genome(), false));
		
		PlayN.mouse().setListener(this);
		PlayN.keyboard().setListener(this);
		
		world.setContactListener(this);
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
		
		fps++;
		
		if(System.currentTimeMillis() / 1000 != lastSecond)
		{
			lastSecond = System.currentTimeMillis() / 1000;
			System.out.println("FPS: " + fps);
			fps = 0;
		}
	}

	@Override
	public int updateRate() {
		return 16;
	}

	@Override
	public void onMouseDown(ButtonEvent event) {
		mouseScreenPos = new Vec2(event.x(), event.y());
		
		Vec2 mousePos = cam.screenToReal(mouseScreenPos);
		
		if(event.button() == Mouse.BUTTON_LEFT)
		{
			mouseScreenPos = new Vec2(event.x(), event.y());
			
			mouseDownRealPos = new Vec2(mousePos);
		}
		if(event.button() == Mouse.BUTTON_RIGHT)
		{
			for(Creature c : creatures)
			{
				if(c.selected) c.setMoveTarget(mousePos);
			}
		}
	}

	@Override
	public void onMouseUp(ButtonEvent event) {
		mouseScreenPos = new Vec2(event.x(), event.y());
		
		if(event.button() == Mouse.BUTTON_LEFT)
		{
			Vec2 mousePos = cam.screenToReal(mouseScreenPos);
			
			if(!shiftKeyDown)
			{
				for(Creature c : creatures)
				{
					c.selected = false;
				}
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

	@Override
	public void onKeyDown(Event event) {
		if(event.key() == Key.B)
		{
			ArrayList<Genome> genomes = new ArrayList<Genome>();
			
			for(Creature c : creatures)
			{
				if(c.selected) genomes.add(c.genome);
			}
			
			Genome genome = new Genome(genomes);
			
			Vec2 mousePos = cam.screenToReal(mouseScreenPos);
			
			Creature crt = new Creature(mousePos, genome, true);
			
			creatures.add(crt);
		}
		
		if(event.key() == Key.SHIFT) shiftKeyDown = true;
	}

	@Override
	public void onKeyTyped(TypedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyUp(Event event) {
		if(event.key() == Key.SHIFT) shiftKeyDown = false;
	}

	@Override
	public void beginContact(Contact contact) {
		if(contact.m_fixtureA.m_userData != null &&
		   contact.m_fixtureB.m_userData != null &&
		   contact.m_fixtureA.m_userData instanceof Piece &&
		   contact.m_fixtureB.m_userData instanceof Piece)
		{
			((Piece)contact.m_fixtureA.m_userData).contactList.add(((Piece)contact.m_fixtureB.m_userData));
			((Piece)contact.m_fixtureB.m_userData).contactList.add(((Piece)contact.m_fixtureA.m_userData));
		}
	}

	@Override
	public void endContact(Contact contact) {
		if(contact.m_fixtureA.m_userData != null &&
		   contact.m_fixtureB.m_userData != null &&
		   contact.m_fixtureA.m_userData instanceof Piece &&
		   contact.m_fixtureB.m_userData instanceof Piece)
		{
			((Piece)contact.m_fixtureA.m_userData).contactList.remove(((Piece)contact.m_fixtureB.m_userData));
			((Piece)contact.m_fixtureB.m_userData).contactList.remove(((Piece)contact.m_fixtureA.m_userData));
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}	
}
