package com.ironalloygames.core;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;

public class Spark extends Entity {

	int color;
	
	public Spark(Vec2 pos, int color) {
		super(pos);
		this.color = color;
		
		body.setLinearVelocity(new Vec2((float)PetriDishEmpire.s.rand.nextGaussian()*3, (float)PetriDishEmpire.s.rand.nextGaussian()*3));
		
		System.out.println("SPARK POS " + pos);
		
		CircleShape cs = new CircleShape();
		cs.m_radius = 0.2f;
		
		body.createFixture(cs, 0.1f);
	}

	@Override
	public void update() {
		super.update();
	}

	@Override
	public void render() {
		super.render();
		
		PetriDishEmpire.s.cam.drawLine(body.getPosition(), body.getPosition().sub(body.getLinearVelocity().mul(16)), color);
	}

}
