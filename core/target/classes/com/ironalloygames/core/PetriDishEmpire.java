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
	
	private static final float DISH_HALFSIZE = 900;
	private static final float MINIMAP_SIZE = 160;
	private static final float KEY_CAMERA_MOVE_SPEED = 2;
	private static final float MOUSE_CAMERA_MOVE_SPEED = 1.f / 20.f;
	
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
	
	public float playerMoney = 700;
	public float enemyMoney = 2500;
	
	public CanvasImage statsDisplay;
	
	Font drawFont;
	
	int lastFrameFPS = 0;
	
	boolean camMoveLeft = false;
	boolean camMoveRight = false;
	boolean camMoveUp = false;
	boolean camMoveDown = false;
	
	Vec2 camMoveRate = new Vec2();
	
	Vec2 mouseScrollStart = null;
	
	@Override
	public void init() {
		s = this;
		
		cam = new Camera();
		
		statsDisplay = graphics().createImage(220, 160);
		
		ImmediateLayer il = graphics().createImmediateLayer(new Renderer(){

			@Override
			public void render(Surface surface) {
				
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
				
				if(mouseScrollStart != null)
				{
					surface.setFillColor(0xFFFFFFFF);
					surface.drawLine(mouseScrollStart.x, mouseScrollStart.y, mouseScreenPos.x, mouseScreenPos.y, 1);
				}
				
				fps++;
				
				if(System.currentTimeMillis() / 1000 != lastSecond)
				{
					lastSecond = System.currentTimeMillis() / 1000;
					lastFrameFPS = fps;
					fps = 0;
				}
			}
		});
		
		ImmediateLayer minimapIL = graphics().createImmediateLayer((int)MINIMAP_SIZE, (int)MINIMAP_SIZE, new Renderer(){

			@Override
			public void render(Surface surface) {
				surface.setFillColor(Color.rgb(128, 128, 128));
				surface.fillRect(0, 0, 1000, 1000);
				
				surface.setFillColor(Color.rgb(0, 0, 0));
				surface.fillRect(1, 1, MINIMAP_SIZE - 2, MINIMAP_SIZE - 2);
				
				for(Creature c : getCreatures())
				{
					int color;
					
					if(c.playerOwned)
						color = Color.rgb(128, 128, 255);
					else
						color = Color.rgb(255, 128, 0);
					
					Vec2 pos = new Vec2(c.body.getPosition());
					pos.y = -pos.y;
					pos.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE);
					pos.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
					
					//System.out.println(pos);
					
					surface.setFillColor(color);
					surface.drawLine(pos.x, pos.y, pos.x+1, pos.y, 1);
				}
				
				Vec2 ulb = new Vec2(cam.upperLeftBound);
				ulb.y = -ulb.y;
				ulb.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE);
				ulb.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
				
				Vec2 lrb = new Vec2(cam.lowerRightBound);
				lrb.y = -lrb.y;
				lrb.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE);
				lrb.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
				
				surface.setFillColor(0xFFFFFFFF);
				surface.drawLine(ulb.x, ulb.y, lrb.x, ulb.y, 1);
				surface.drawLine(ulb.x, ulb.y, ulb.x, lrb.y, 1);
				surface.drawLine(lrb.x, lrb.y, lrb.x, ulb.y, 1);
				surface.drawLine(lrb.x, lrb.y, ulb.x, lrb.y, 1);
			}
		});
	  
		graphics().rootLayer().add(il);
		graphics().rootLayer().add(graphics().createImageLayer(statsDisplay));
		graphics().rootLayer().addAt(minimapIL, graphics().width() - 40 - MINIMAP_SIZE, 40);
		
		world = new World(new Vec2(0,0), true);
		
		rand = new Random();
		
		while(enemyMoney > 0)
		{
			entities.add(new Creature(new Vec2((rand.nextFloat() - 0.5f) * DISH_HALFSIZE,(rand.nextFloat() - 0.5f) * DISH_HALFSIZE), new Genome(), false));
		}
		
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
	
	int enemiesOnField = 0;
	int alliesOnField = 0;
	
	int infoUpdateCountdown = 0;
	
	float enemyBiomass = 0;
	float alliedBiomass = 0;

	@Override
	public void update(float delta) {
		
		Vec2 mousePos = cam.screenToReal(mouseScreenPos);
		
		enemiesOnField = 0;
		alliesOnField = 0;
		
		enemyBiomass = 0;
		alliedBiomass = 0;
		
		for(Entity e : entities)
		{
			e.update();
			if(e instanceof Creature)
			{
				Creature c = (Creature)e;
				if(c.isInBoundingBox(mousePos) && c.playerOwned) c.mouseHover = true;
				
				if(!c.playerOwned)
					enemiesOnField++;
				else
					alliesOnField++;
				
				if(!c.playerOwned)
					enemyBiomass += c.body.getMass();
				else
					alliedBiomass += c.body.getMass();
			}
		}
		
		for(int i=0;i<entities.size();++i)
		{
			if(!entities.get(i).keep())
			{
				entities.get(i).destroyed();
				entities.remove(i--);
			}
		}
		
		entities.addAll(entityAddQueue);
		entityAddQueue.clear();
		
		world.step(delta, 3, 3);
		
		if(enemyMoney > 0)
		{
			ArrayList<Genome> genomes = new ArrayList<Genome>();
			
			int sourceGenomes = (int)(rand.nextGaussian() * 1.5);
			
			List<Creature> creatures = getCreatures();
			
			for(int i=0;i<sourceGenomes;++i)
			{
				int n = 0;
				Genome nextGenome = null;
				
				for(Creature c : creatures)
				{
					if(rand.nextInt(++n) == 0)
						nextGenome = c.genome;
				}
				
				genomes.add(nextGenome);
			}
			
			if(genomes.size() == 0) genomes.add(new Genome());
			
			entities.add(new Creature(new Vec2((rand.nextFloat() - 0.5f) * DISH_HALFSIZE,(rand.nextFloat() - 0.5f) * DISH_HALFSIZE), new Genome(genomes), false));
		}
		
		
		if(infoUpdateCountdown <= 0)
		{
			statsDisplay.canvas().clear();
			statsDisplay.canvas().setFillColor(playerMoney > 0 ? Color.rgb(0, 255, 0) : Color.rgb(255, 255, 0));
			statsDisplay.canvas().drawText("Food: " + (int)playerMoney, 20, 20);
			
			statsDisplay.canvas().setFillColor(Color.rgb(200, 200, 200));
			statsDisplay.canvas().drawText("Population:", 20, 40);
			
			statsDisplay.canvas().setFillColor(Color.rgb(100, 100, 255));
			statsDisplay.canvas().drawText("" + alliesOnField, 100, 40);
			
			statsDisplay.canvas().setFillColor(Color.rgb(255, 128, 0));
			statsDisplay.canvas().drawText("" + enemiesOnField, 150, 40);
			
			statsDisplay.canvas().setFillColor(Color.rgb(200, 200, 200));
			statsDisplay.canvas().drawText("Biomass:", 20, 60);
			
			statsDisplay.canvas().setFillColor(Color.rgb(100, 100, 255));
			statsDisplay.canvas().drawText("" + (int)alliedBiomass, 100, 60);
			
			statsDisplay.canvas().setFillColor(Color.rgb(255, 128, 0));
			statsDisplay.canvas().drawText("" + (int)enemyBiomass, 150, 60);
			
			statsDisplay.canvas().setFillColor(Color.rgb(255, 128, 0));
			statsDisplay.canvas().drawText("Enemy Food: " + (int)enemyMoney, 20, 120);
			
			statsDisplay.canvas().setFillColor(Color.rgb(255, 255, 255));
			statsDisplay.canvas().drawText("FPS: " + lastFrameFPS, 20, 140);
			
			infoUpdateCountdown = 10;
		} else {
			infoUpdateCountdown--;
		}
		
		Vec2 desiredCamMove = new Vec2();
		
		if(camMoveLeft && !camMoveRight) desiredCamMove.x = -KEY_CAMERA_MOVE_SPEED;
		if(!camMoveLeft && camMoveRight) desiredCamMove.x = KEY_CAMERA_MOVE_SPEED;
		if(camMoveUp && !camMoveDown) desiredCamMove.y = KEY_CAMERA_MOVE_SPEED;
		if(!camMoveUp && camMoveDown) desiredCamMove.y = -KEY_CAMERA_MOVE_SPEED;
		
		float INTERPOLATION = 0.9f;
		
		// if the center mouse button is down
		if(mouseScrollStart != null)
		{
			Vec2 mouseScrollDelta = mouseScrollStart.sub(mouseScreenPos);
			mouseScrollDelta.x = -mouseScrollDelta.x;
			mouseScrollDelta.mulLocal(MOUSE_CAMERA_MOVE_SPEED);
			desiredCamMove.addLocal(mouseScrollDelta);
		}
		
		camMoveRate = (camMoveRate.mul(INTERPOLATION)).add(desiredCamMove.mul(1 - INTERPOLATION));
		
		cam.translateCamera(camMoveRate, 5);
		
		
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
		if(event.button() == Mouse.BUTTON_MIDDLE)
		{
			mouseScrollStart = new Vec2(mouseScreenPos);
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
		if(event.button() == Mouse.BUTTON_MIDDLE)
		{
			mouseScrollStart = null;
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
		if(event.key() == Key.B && playerMoney > 0)
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
		
		if(event.key() == Key.W) camMoveUp = true;
		if(event.key() == Key.S) camMoveDown = true;
		if(event.key() == Key.A) camMoveLeft = true;
		if(event.key() == Key.D) camMoveRight = true;
		
		if(event.key() == Key.SHIFT) shiftKeyDown = true;
	}

	@Override
	public void onKeyTyped(TypedEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyUp(Event event) {
		if(event.key() == Key.SHIFT) shiftKeyDown = false;
		
		if(event.key() == Key.W) camMoveUp = false;
		if(event.key() == Key.S) camMoveDown = false;
		if(event.key() == Key.A) camMoveLeft = false;
		if(event.key() == Key.D) camMoveRight = false;
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
