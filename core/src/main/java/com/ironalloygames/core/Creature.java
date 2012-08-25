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
	}
	
	public void setMoveTarget(Vec2 moveTarget)
	{
		this.moveTarget = new Vec2(moveTarget);
	}
	
	public void render()
	{
		Camera cam = PetriDishEmpire.s.cam;
		
		if((body.getPosition().x < cam.upperLeftBound.x - radius &&
		   body.getPosition().y < cam.upperLeftBound.y - radius)
		   ||
		   (body.getPosition().x > cam.lowerRightBound.x - radius &&
		    body.getPosition().y > cam.lowerRightBound.y - radius))
		{
			return;
		}
		
		for(Piece p : pieces)
		{
			p.render(cam, this);
		}
		
		if(!playerOwned)
			cam.drawImage(body.getPosition(), "red-dot");
		
		if(mouseHover) renderBoundingBox(Color.argb(64, 255, 255, 255));
		
		if(selected)
		{
			Vec2 ul = body.getPosition().add(new Vec2(-radius*BOUNDING_BOX_MUL, radius*BOUNDING_BOX_MUL));
			Vec2 ur = body.getPosition().add(new Vec2(radius*BOUNDING_BOX_MUL, radius*BOUNDING_BOX_MUL));
			Vec2 ll = body.getPosition().add(new Vec2(-radius*BOUNDING_BOX_MUL, -radius*BOUNDING_BOX_MUL));
			Vec2 lr = body.getPosition().add(new Vec2(radius*BOUNDING_BOX_MUL, -radius*BOUNDING_BOX_MUL));
			
			cam.drawLine(ul, ul.add(new Vec2(SELECTION_SIZE, 0)), Color.rgb(255, 255, 255));
			cam.drawLine(ul, ul.add(new Vec2(0, -SELECTION_SIZE)), Color.rgb(255, 255, 255));
			
			cam.drawLine(ur, ur.add(new Vec2(-SELECTION_SIZE, 0)), Color.rgb(255, 255, 255));
			cam.drawLine(ur, ur.add(new Vec2(0, -SELECTION_SIZE)), Color.rgb(255, 255, 255));
			
			cam.drawLine(ll, ll.add(new Vec2(SELECTION_SIZE, 0)), Color.rgb(255, 255, 255));
			cam.drawLine(ll, ll.add(new Vec2(0, SELECTION_SIZE)), Color.rgb(255, 255, 255));
			
			cam.drawLine(lr, lr.add(new Vec2(-SELECTION_SIZE, 0)), Color.rgb(255, 255, 255));
			cam.drawLine(lr, lr.add(new Vec2(0, SELECTION_SIZE)), Color.rgb(255, 255, 255));
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
		Vec2 delta = moveTarget.sub(body.getPosition());
		
		if(delta.lengthSquared() > 1*1)
		{
			delta.normalize();
			delta.mulLocal(enginePower);
			
			body.applyForce(delta, body.getPosition());
		}
		
		body.applyTorque(enginePower);
		
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































