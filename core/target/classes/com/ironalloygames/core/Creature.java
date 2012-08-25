package com.ironalloygames.core;

import java.util.ArrayList;
import java.util.Collections;

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
	}
	
	public void render(Camera cam)
	{
		for(Piece p : pieces)
		{
			p.render(cam, this);
		}
	}
}
