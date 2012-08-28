package com.ironalloygames.core;

import java.util.ArrayList;
import java.util.Collections;

import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

import playn.core.Color;

import com.ironalloygames.core.piece.*;

public class Creature extends Entity{
	
	private static final float DAMPENING = 0.9f;
	Genome genome;
	public boolean playerOwned;
	
	float solarRate;
	float enginePower;
	
	Vec2 moveTarget;
	Creature attackTarget = null;
	
	ArrayList<Piece> pieces = new ArrayList<Piece>();
	
	public boolean mouseHover;
	public boolean selected;
	
	public float radius;
	
	static final float BOUNDING_BOX_MUL =  1.15f;
	static final float SELECTION_SIZE =  1.6f;
	
	public boolean aggressiveMode = false;
	
	boolean woken = false;
	
	int spinDir = 1;
	
	public Creature(Vec2 pos, Genome genome, boolean playerOwned)
	{
		super(pos);
		
		this.playerOwned = playerOwned;
		//selected = playerOwned;
		
		this.genome = genome;
		
		for(float angle = 0;angle < MathUtils.TWOPI;angle += MathUtils.TWOPI / this.genome.arms)
		{
			ArrayList<Piece> arm = new ArrayList<Piece>();
			
			for(Gene g : genome.genes)
			{
				g.build(arm, angle, this);
			}
			
			pieces.addAll(arm);
		}
		
		for(Piece p : pieces)
		{
			Fixture f = body.createFixture(p.getFixtureDef());
			p.fixture = f;
			p.hp = p.getMass();
			
			if(playerOwned)
				PetriDishEmpire.s.playerMoney -= p.getCostMod() * p.getMass();
			else
				PetriDishEmpire.s.enemyMoney -= p.getCostMod() * p.getMass();
		}
		
		moveTarget = new Vec2(pos);
		
		recalculateStats();
		
		body.setActive(false);
		
		if(!playerOwned && Math.round(genome.genes.get(genome.genes.size() - 1).geneType) == Gene.GT_WEAPON) aggressiveMode = true;
		
		boolean moved = true;
		
		int moves = 0;
		
		while(moved)
		{
			moved = false;
			moves++;
			
			for(Creature c : PetriDishEmpire.s.getCreatures())
			{
				if(c == this) continue;
				
				float dist = c.body.getPosition().sub(body.getPosition()).length();
				
				if(dist < c.radius + radius + 2.f)
				{
					Vec2 delta = body.getPosition().sub(c.body.getPosition());
					delta.normalize();
					if(delta.length() < 0.5f)
						delta.x = 1;
					delta.mulLocal(moves);
					body.setTransform(body.getPosition().add(delta), body.getAngle());
					moved = true;
				}
			}
		}
	}
	
	public void setMoveTarget(Vec2 moveTarget)
	{
		this.moveTarget = new Vec2(moveTarget);
	}
	
	public void render()
	{
		Camera cam = PetriDishEmpire.s.cam;
		
		if(!playerOwned)
			cam.drawImage(body.getPosition(), "red-dot", new Vec2(radius*2,radius*2));
		else
			cam.drawImage(body.getPosition(), "blue-dot", new Vec2(radius*2,radius*2));
		
		if(body.getPosition().x < cam.upperLeftBound.x - radius ||
		   body.getPosition().y < cam.upperLeftBound.y - radius ||
		   body.getPosition().x > cam.lowerRightBound.x + radius ||
		   body.getPosition().y > cam.lowerRightBound.y + radius)
		{
			return;
		}
		
		for(Piece p : pieces)
		{
			p.render(cam, this);
		}
		
		if(mouseHover) renderBoundingBox(Color.argb(64, 255, 255, 255));
		
		if(selected)
		{
			int color;
			
			if(!aggressiveMode)
				color = Color.rgb(255, 255, 255);
			else
				color = Color.rgb(255, 0, 0);
			
			Vec2 ul = body.getPosition().add(new Vec2(-radius*BOUNDING_BOX_MUL, radius*BOUNDING_BOX_MUL));
			Vec2 ur = body.getPosition().add(new Vec2(radius*BOUNDING_BOX_MUL, radius*BOUNDING_BOX_MUL));
			Vec2 ll = body.getPosition().add(new Vec2(-radius*BOUNDING_BOX_MUL, -radius*BOUNDING_BOX_MUL));
			Vec2 lr = body.getPosition().add(new Vec2(radius*BOUNDING_BOX_MUL, -radius*BOUNDING_BOX_MUL));
			
			cam.drawLine(ul, ul.add(new Vec2(SELECTION_SIZE, 0)), color);
			cam.drawLine(ul, ul.add(new Vec2(0, -SELECTION_SIZE)), color);
			
			cam.drawLine(ur, ur.add(new Vec2(-SELECTION_SIZE, 0)), color);
			cam.drawLine(ur, ur.add(new Vec2(0, -SELECTION_SIZE)), color);
			
			cam.drawLine(ll, ll.add(new Vec2(SELECTION_SIZE, 0)), color);
			cam.drawLine(ll, ll.add(new Vec2(0, SELECTION_SIZE)), color);
			
			cam.drawLine(lr, lr.add(new Vec2(-SELECTION_SIZE, 0)), color);
			cam.drawLine(lr, lr.add(new Vec2(0, SELECTION_SIZE)), color);
		}
	}
	
	public void recalculateStats()
	{
		solarRate = 0;
		enginePower = 0;
		
		for(Piece p : pieces)
		{
			solarRate += p.getSolarPower();
			enginePower += p.getEnginePower();
		}
	}
	
	public void update()
	{
		woken = false;
		
		if(!woken)
		{
			for(Entity e : PetriDishEmpire.s.entities)
			{
				if(e instanceof Creature && ((Creature) e).playerOwned)
				{
					if(Math.abs(e.body.getPosition().x - body.getPosition().x) < 50 || Math.abs(e.body.getPosition().y - body.getPosition().y) < 50)
					{
						woken = true;
						break;
					}
				}
			}
		}
		
		body.setActive(woken);
		
		if(woken)
		{
			if(aggressiveMode)
			{
				float bestDist = 200*200;
				Creature target = null;
				
				for(Creature c : PetriDishEmpire.s.getCreatures())
				{
					if(c.playerOwned != playerOwned)
					{
						float dist = body.getPosition().sub(c.body.getPosition()).lengthSquared();
						
						if(dist < bestDist)
						{
							bestDist = dist;
							target = c;
						}
					}
				}
				
				if(target != null)
					moveTarget = target.body.getPosition();
			}
			
			Vec2 delta = moveTarget.sub(body.getPosition());
			
			if(delta.lengthSquared() > 1*1)
			{
				delta.normalize();
				delta.mulLocal(enginePower);
				
				body.applyForce(delta, body.getPosition());
			}
			
			body.applyTorque(enginePower*2.5f*spinDir);
			
			if(PetriDishEmpire.s.rand.nextInt(400) == 0) spinDir = -spinDir;
			
			mouseHover = false;
			
			for(int i=0;i<pieces.size();++i)
			{
				if(pieces.get(i).hp > 0)
				{
					pieces.get(i).update();
				} else {
					body.destroyFixture(pieces.get(i).fixture);
					pieces.remove(i);
					i--;
					this.recalculateStats();
				}
			}
			
			body.setLinearVelocity(body.getLinearVelocity().mul(DAMPENING));
			body.setAngularVelocity(body.getAngularVelocity() * DAMPENING);
			
			//System.out.println(body.getPosition());
		}
		
		if(PetriDishEmpire.s.lastFrameTotalBiomass < 1700)
		{
			if(playerOwned)
				PetriDishEmpire.s.playerMoney += this.solarRate;
			else
				PetriDishEmpire.s.enemyMoney += this.solarRate;
		}
	}
	
	void renderBoundingBox(int color)
	{
		PetriDishEmpire.s.cam.drawRectangle(body.getPosition(), new Vec2(radius*BOUNDING_BOX_MUL*2, radius*BOUNDING_BOX_MUL*2), color);
	}
	
	boolean isInBoundingBox(Vec2 pos)
	{
		return Math.abs(pos.x - body.getPosition().x) < radius*BOUNDING_BOX_MUL &&
			   Math.abs(pos.y - body.getPosition().y) < radius*BOUNDING_BOX_MUL;
	}

	@Override
	public boolean keep() {
		return pieces.size() > 0;
	}
}































