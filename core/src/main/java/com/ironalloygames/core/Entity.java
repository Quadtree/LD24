package com.ironalloygames.core;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;

public class Entity {
	public Body body;
	
	public Entity(Vec2 pos)
	{
		BodyDef bd = new BodyDef();
		bd.position = pos;
		bd.type = BodyType.DYNAMIC;
		bd.bullet = false;
		
		body = PetriDishEmpire.s.world.createBody(bd);
	}
	
	public void update(){}
	public void render(){}
	public boolean keep(){ return true; }
	
	public void destroyed()
	{
		PetriDishEmpire.s.world.destroyBody(body);
	}
}
