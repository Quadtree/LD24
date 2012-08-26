package com.ironalloygames.core;

import static playn.core.PlayN.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
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
import playn.core.Sound;
import playn.core.Surface;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.util.Callback;

public class PetriDishEmpire implements Game, Listener, playn.core.Keyboard.Listener, ContactListener {
	
	private static final float DISH_HALFSIZE = 900;
	private static final float MINIMAP_SIZE = 320;
	private static final float KEY_CAMERA_MOVE_SPEED = 2;
	private static final float MOUSE_CAMERA_MOVE_SPEED = 1.f / 20.f;
	
	private static final int WALL_SECTIONS = 64;
	
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
	public float enemyMoney = 1800;
	
	public CanvasImage statsDisplay;
	
	Font drawFont;
	
	int lastFrameFPS = 0;
	
	boolean camMoveLeft = false;
	boolean camMoveRight = false;
	boolean camMoveUp = false;
	boolean camMoveDown = false;
	
	Vec2 camMoveRate = new Vec2();
	
	Vec2 mouseScrollStart = null;
	
	ArrayList<PolygonShape> polys = new ArrayList<PolygonShape>();
	
	Image minimapImage;
	
	Sound music = null;
	
	ImageLayer titleScreen = null;
	ImageLayer helpScreen = null;
	ImageLayer victoryScreen = null;
	
	@Override
	public void init() {
		s = this;
		
		cam = new Camera();
		
		statsDisplay = graphics().createImage(220, 160);
		
		minimapImage = assets().getImage("images/minimap.png");
		
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
				
				for(PolygonShape ps : polys)
				{
					for(int i=3;i<ps.getVertexCount();++i)
					{
						cam.drawLine(ps.getVertex(i-1), ps.getVertex(i), 0xFFFFFFFF);
					}
				}
			}
		});
		
		ImmediateLayer minimapIL = graphics().createImmediateLayer((int)MINIMAP_SIZE, (int)MINIMAP_SIZE, new Renderer(){

			@Override
			public void render(Surface surface) {
				surface.drawImage(minimapImage, 0, 0);
				//surface.setFillColor(Color.rgb(128, 128, 128));
				//surface.fillRect(0, 0, 1000, 1000);
				
				//surface.setFillColor(Color.rgb(0, 0, 0));
				//surface.fillRect(1, 1, MINIMAP_SIZE - 2, MINIMAP_SIZE - 2);
				
				for(Creature c : getCreatures())
				{
					int color;
					
					if(c.playerOwned)
						color = Color.rgb(128, 128, 255);
					else
						color = Color.rgb(255, 128, 0);
					
					Vec2 pos = new Vec2(c.body.getPosition());
					pos.y = -pos.y;
					pos.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE / 2);
					pos.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
					
					//System.out.println(pos);
					
					float RAD_MUL = 1.f / 7.f;
					
					surface.setFillColor(color);
					surface.fillRect(pos.x - c.radius * RAD_MUL, pos.y - c.radius * RAD_MUL, c.radius * RAD_MUL * 2, c.radius * RAD_MUL * 2);
					//surface.drawLine(pos.x, pos.y, pos.x+1, pos.y, 1);
				}
				
				Vec2 ulb = new Vec2(cam.upperLeftBound);
				ulb.y = -ulb.y;
				ulb.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE / 2);
				ulb.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
				
				Vec2 lrb = new Vec2(cam.lowerRightBound);
				lrb.y = -lrb.y;
				lrb.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE / 2);
				lrb.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
				
				surface.setFillColor(0xFFFFFFFF);
				surface.drawLine(ulb.x, ulb.y, lrb.x, ulb.y, 1);
				surface.drawLine(ulb.x, ulb.y, ulb.x, lrb.y, 1);
				surface.drawLine(lrb.x, lrb.y, lrb.x, ulb.y, 1);
				surface.drawLine(lrb.x, lrb.y, ulb.x, lrb.y, 1);
				
				/*for(PolygonShape ps : polys)
				{
					for(int i=3;i<ps.getVertexCount();++i)
					{
						Vec2 v1 = new Vec2(ps.getVertex(i-1));
						v1.y = -v1.y;
						v1.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE / 2);
						v1.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
						
						Vec2 v2 = new Vec2(ps.getVertex(i));
						v2.y = -v2.y;
						v2.mulLocal(MINIMAP_SIZE / DISH_HALFSIZE / 2);
						v2.addLocal(new Vec2(MINIMAP_SIZE / 2, MINIMAP_SIZE / 2));
						
						surface.setFillColor(0xFFFFFFFF);
						surface.drawLine(v1.x, v1.y, v2.x, v2.y, 1);
					}
				}*/
			}
		});
	  
		graphics().rootLayer().add(il);
		graphics().rootLayer().add(graphics().createImageLayer(statsDisplay));
		graphics().rootLayer().addAt(minimapIL, graphics().width() - 40 - MINIMAP_SIZE, 40);
		
		helpScreen = graphics().createImageLayer(assets().getImage("images/help.png"));
		graphics().rootLayer().addAt(helpScreen, graphics().width() / 2 - 320, graphics().height() / 2 - 180);
		
		titleScreen = graphics().createImageLayer(assets().getImage("images/title.png"));
		titleScreen.setScale(graphics().height() / 1080.f);
		graphics().rootLayer().add(titleScreen);
		
		
		world = new World(new Vec2(0,0), true);
		
		rand = new Random();
		
		while(enemyMoney > 0)
		{
			entities.add(new Creature(makeRandomEnemyPos(), new Genome(), false));
		}
		
		PlayN.mouse().setListener(this);
		PlayN.keyboard().setListener(this);
		
		world.setContactListener(this);
		
		BodyDef bd = new BodyDef();
		bd.type = BodyType.STATIC;
		
		Body dishWalls = world.createBody(bd);
		
		for(float angle=0;angle<MathUtils.TWOPI;angle += (MathUtils.TWOPI / WALL_SECTIONS))
		{
			Vec2 sectionStart = new Vec2(MathUtils.cos(angle) * DISH_HALFSIZE, MathUtils.sin(angle) * DISH_HALFSIZE);
			Vec2 sectionEnd = new Vec2(MathUtils.cos(angle - (MathUtils.TWOPI / WALL_SECTIONS)) * DISH_HALFSIZE, MathUtils.sin(angle - (MathUtils.TWOPI / WALL_SECTIONS)) * DISH_HALFSIZE);
			Vec2 center = sectionStart.add(sectionEnd).mul(0.5f);
			
			PolygonShape polygon = new PolygonShape();
			
			polygon.setAsBox(sectionStart.sub(sectionEnd).length() / 2, 10, center, angle + MathUtils.HALF_PI - (MathUtils.TWOPI / WALL_SECTIONS / 2));
			
			polys.add(polygon);
			
			//System.out.println(sectionStart + " " + sectionEnd + " " + center);
			
			dishWalls.createFixture(polygon, 0);
		}
		
		music = assets().getSound("sound/music0");
		music.setLooping(true);
		//music.setVolume(0.85f);
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
	
	public Vec2 makeRandomEnemyPos()
	{
		float bearing = rand.nextFloat() * MathUtils.TWOPI;
		float dist = rand.nextFloat() * DISH_HALFSIZE;
		
		return new Vec2(MathUtils.cos(bearing) * dist, MathUtils.sin(bearing) * dist);
	}

	@Override
	public void update(float delta) {
		
		if((titleScreen != null && titleScreen.visible()) || (helpScreen != null && helpScreen.visible()) || (victoryScreen != null && victoryScreen.visible())) return;
		
		if(!music.isPlaying())
			music.play();
		
		if(alliedBiomass > 3200 && enemyBiomass < 800 && victoryScreen == null)
		{
			victoryScreen = graphics().createImageLayer(assets().getImage("images/victory.png"));
			graphics().rootLayer().addAt(victoryScreen, graphics().width() / 2 - 320, graphics().height() / 2 - 180);
		}
		
		Vec2 mousePos = cam.screenToReal(mouseScreenPos);
		
		enemiesOnField = 0;
		alliesOnField = 0;
		
		enemyBiomass = 0;
		alliedBiomass = 0;
		
		float bestDist = Float.MAX_VALUE;
		Creature bestSelectTarget = null;
		
		for(Creature c : getCreatures())
		{
			if(c.isInBoundingBox(mousePos) && c.playerOwned)
			{
				float dist = c.body.getPosition().sub(mousePos).lengthSquared();
				if(dist < bestDist)
				{
					 bestSelectTarget = c;
					 bestDist = dist;
				}
			}
		}
		
		for(Entity e : entities)
		{
			e.update();
			
			if(e instanceof Creature)
			{
				Creature c = (Creature)e;
				
				if(c == bestSelectTarget) c.mouseHover = true;
				
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
					if(!c.playerOwned && rand.nextInt(++n) == 0)
						nextGenome = c.genome;
				}
				
				genomes.add(nextGenome);
			}
			
			if(genomes.size() == 0) genomes.add(new Genome());
			
			entities.add(new Creature(makeRandomEnemyPos(), new Genome(genomes), false));
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
			boolean any = false;
			
			for(Creature c : getCreatures())
			{
				if(c.selected)
				{
					c.setMoveTarget(mousePos);
					c.aggressiveMode = false;
					any = true;
				}
			}
			
			if(any) AudioSystem.play("move" + PetriDishEmpire.s.rand.nextInt(3));
		}
		if(event.button() == Mouse.BUTTON_MIDDLE)
		{
			mouseScrollStart = new Vec2(mouseScreenPos);
		}
		
		if(titleScreen != null && titleScreen.visible())
			titleScreen.setVisible(false);
		else if(helpScreen != null && helpScreen.visible())
			helpScreen.setVisible(false);
		else if(victoryScreen != null && victoryScreen.visible())
			victoryScreen.setVisible(false);
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
			
			boolean oneSelected = false;
			
			if(mousePos.sub(mouseDownRealPos).length() < BAND_SELECT_THRESH)
			{
				float bestDist = Float.MAX_VALUE;
				Creature bestSelectTarget = null;
				
				for(Creature c : getCreatures())
				{
					if(c.isInBoundingBox(mousePos) && c.playerOwned)
					{
						float dist = c.body.getPosition().sub(mousePos).lengthSquared();
						if(dist < bestDist)
						{
							 bestSelectTarget = c;
							 bestDist = dist;
						}
					}
				}
				
				if(bestSelectTarget != null)
				{
					bestSelectTarget.selected = true;
					oneSelected = true;
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
							oneSelected = true;
							c.selected = true;
						}
					}
				}
			}
			
			if(oneSelected) AudioSystem.play("select" + PetriDishEmpire.s.rand.nextInt(3));
			
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
			
			AudioSystem.play("split");
		}
		
		if(event.key() == Key.UP) camMoveUp = true;
		if(event.key() == Key.DOWN) camMoveDown = true;
		if(event.key() == Key.LEFT) camMoveLeft = true;
		if(event.key() == Key.RIGHT) camMoveRight = true;
		
		if(event.key() == Key.A)
		{
			boolean agg = false;
			
			for(Creature c : getCreatures())
			{
				if(c.selected)
				{
					c.aggressiveMode = true;
					agg = true;
				}
			}
			
			if(agg) AudioSystem.play("agg");
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
		
		if(event.key() == Key.UP) camMoveUp = false;
		if(event.key() == Key.DOWN) camMoveDown = false;
		if(event.key() == Key.LEFT) camMoveLeft = false;
		if(event.key() == Key.RIGHT) camMoveRight = false;
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
