package com.ironalloygames.core;

import java.util.ArrayList;
import java.util.Collections;

import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

import com.ironalloygames.core.piece.*;

public class Creature {
	public Body body;
	Genome genome;
	boolean playerOwned;
	
	float solarRate;
	float enginePower;
	
	Vec2 moveTarget;
	Creature attackTarget = null;
	
	ArrayList<Piece> pieces = new ArrayList<Piece>();
	
	public Creature(Vec2 pos, Genome genome, boolean playerOwned)
	{
		this.playerOwned = playerOwned;
		
		BodyDef bd = new BodyDef();
		bd.position = pos;
		bd.type = BodyType.DYNAMIC;
		
		body = PetriDishEmpire.s.world.createBody(bd);
		
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
			body.createFixture(p.getFixtureDef());
		
		moveTarget = new Vec2(pos);
		
		recalculateStats();
	}
	
	public void setMoveTarget(Vec2 moveTarget)
	{
		this.moveTarget = new Vec2(moveTarget);
	}
	
	public void render(Camera cam)
	{
		for(Piece p : pieces)
		{
			p.render(cam, this);
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
		
		if(delta.lengthSquared() > 5*5)
		{
			delta.normalize();
			delta.mulLocal(enginePower);
			
			MassData md = new MassData();
			
			body.getMassData(md);
			
			body.applyForce(delta, md.center);
			
			System.out.println("Moving to " + delta);
		}
	}
}































