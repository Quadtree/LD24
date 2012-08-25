package com.ironalloygames.core;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;

import playn.core.Color;

public class Spark extends Entity {

	int color;
	
	float lifespan;
	
	public Spark(Vec2 pos, int color) {
		super(pos);
		this.color = color;
		
		body.setLinearVelocity(new Vec2((float)PetriDishEmpire.s.rand.nextGaussian()*3, (float)PetriDishEmpire.s.rand.nextGaussian()*3));
		
		System.out.println("SPARK POS " + body.getLinearVelocity());
		
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.2f;
		
		body.createFixture(cs, 0.01f);
		
		lifespan = 1;
	}

	@Override
	public void update() {
		lifespan -= 1.f / 50.f;
		
		super.update();
	}

	@Override
	public void render() {
		super.render();
		
		int x = Color.argb(0, 255, 255, 255);
		
		PetriDishEmpire.s.cam.drawLine(body.getPosition(), body.getPosition().sub(body.getLinearVelocity().mul(16)), (color & 0x00FFFFFF) + ((int)(lifespan * 255) << 24));
	}

	@Override
	public boolean keep() {
		return lifespan > 0.05f;
	}
}
