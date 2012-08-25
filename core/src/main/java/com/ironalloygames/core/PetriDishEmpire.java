package com.ironalloygames.core;

import static playn.core.PlayN.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import com.ironalloygames.core.piece.Piece;

import playn.core.CanvasImage;
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
import playn.core.Font;
import playn.core.Surface;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.util.Callback;

public class PetriDishEmpire implements Game, Listener, playn.core.Keyboard.Listener, ContactListener {
	
	public Camera cam;
	public World world;
	public Random rand;
	
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public static PetriDishEmpire s;
	
	int fps = 0;
	long lastSecond = 0;
	
	Vec2 mouseScreenPos = new Vec2();
	
	Vec2 mouseDownRealPos = null;
	
	boolean shiftKeyDown = false;
	
	final static float BAND_SELECT_THRESH = 2;
	
	public ArrayList<Entity> entityAddQueue = new ArrayList<Entity>();
	
	public float playerMoney = 50;
	
	public CanvasImage statsDisplay;
	
	Font drawFont;
	
	@Override
	public void init() {
		s = this;
		
		cam = new Camera();
		
		statsDisplay = graphics().createImage(256, 256);
		
		ImmediateLayer il = graphics().createImmediateLayer(new Renderer(){

			@Override
			public void render(Surface surface) {
				cam.setCamera(new Vec2(0,0), 5);
				
				surface.clear();
				cam.surf = surface;
				
				for(Entity c : entities)
				{
					c.render();
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
				
				statsDisplay.canvas().clear();
				statsDisplay.canvas().setStrokeColor(Color.rgb(255, 255, 255));
				
				TextFormat tf = new TextFormat();
				tf.font = new Font();
				
				TextLayout tl = new TextLayout();
				
				statsDisplay.canvas().strokeText(tl, 30.f, 30.f);
				
				surface.drawImage(statsDisplay, 0, 0);
			}
		});
	  
		graphics().rootLayer().add(il);
		
		world = new World(new Vec2(0,0), true);
		
		rand = new Random();
		
		entities.add(new Creature(new Vec2(10,10), new Genome(), false));
		
		PlayN.mouse().setListener(this);
		PlayN.keyboard().setListener(this);
		
		world.setContactListener(this);
	}
	
	public List<Creature> getCreatures()
	{
		ArrayList<Creature> ret = new ArrayList<Creature>();
		
		for(Entity e : entities)
			if(e instanceof Creature) ret.add((Creature)e);
		
		return ret;
	}

	@Override
	public void paint(float alpha) {
	}

	@Override
	public void update(float delta) {
		
		Vec2 mousePos = cam.screenToReal(mouseScreenPos);
		
		for(Entity e : entities)
		{
			e.update();
			if(e instanceof Creature)
			{
				Creature c = (Creature)e;
				if(c.isInBoundingBox(mousePos) && c.playerOwned) c.mouseHover = true;
			}
		}
		
		for(int i=0;i<entities.size();++i)
		{
			if(!entities.get(i).keep()) entities.remove(i--);
		}
		
		entities.addAll(entityAddQueue);
		entityAddQueue.clear();
		
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
			for(Creature c : getCreatures())
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
				for(Creature c : getCreatures())
				{
					c.selected = false;
				}
			}
			
			if(mousePos.sub(mouseDownRealPos).length() < BAND_SELECT_THRESH)
			{
				for(Creature c : getCreatures())
				{
					if(c.isInBoundingBox(mousePos) && c.playerOwned) c.selected = true;
				}
			} else {
				Vec2 ul = new Vec2(Math.min(mousePos.x, mouseDownRealPos.x), Math.min(mousePos.y, mouseDownRealPos.y));
				Vec2 lr = new Vec2(Math.max(mousePos.x, mouseDownRealPos.x), Math.max(mousePos.y, mouseDownRealPos.y));
				
				for(Creature c : getCreatures())
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
			
			for(Creature c : getCreatures())
			{
				if(c.selected) genomes.add(c.genome);
			}
			
			if(genomes.size() == 0) genomes.add(new Genome());
			
			Genome genome = new Genome(genomes);
			
			Vec2 mousePos = cam.screenToReal(mouseScreenPos);
			
			Creature crt = new Creature(mousePos, genome, true);
			
			entities.add(crt);
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
			
			//System.out.println("CONTACT!");
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
