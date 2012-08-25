package com.ironalloygames.core.piece;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import com.ironalloygames.core.Camera;
import com.ironalloygames.core.Creature;

import playn.core.Color;

public class Piece {
	Vec2 start;
	Vec2 end;
	Fixture fixture;
	public Creature owner;
	
	protected int getColor(){ return Color.rgb(255, 255, 255); }
	
	public Vec2 getStart()
	{
		return new Vec2(start);
	}
	
	public void setStart(Vec2 start)
	{
		this.start = new Vec2(start);
	}
	
	public float getAngle()
	{
		return MathUtils.fastAtan2(end.y - start.y, end.x - start.x);
	}
	
	public float getLength()
	{
		return end.sub(start).length();
	}
	
	public Vec2 getEnd()
	{
		return new Vec2(end);
	}
	
	public void setEnd(Vec2 end)
	{
		this.end = new Vec2(end);
	}
	
	public void render(Camera cam, Creature crt)
	{
		Transform t = new Transform();
		t.set(crt.body.getPosition(), crt.body.getAngle());
		
		Vec2 startAbs = Transform.mul(t, start);
		Vec2 endAbs = Transform.mul(t, end);
		
		cam.drawLine(startAbs, endAbs, getColor());
	}
	
	public FixtureDef getFixtureDef()
	{
		FixtureDef fd = new FixtureDef();
		fd.density = 1;
		fd.userData = this;
		
		PolygonShape ps = new PolygonShape();
		ps.setAsBox((getLength() / 2 + 0.1f), 0.1f, (start.add(end)).mul(0.5f), getAngle());
		
		fd.shape = ps;
		
		return fd;
	}
	
	public float getSolarPower(){ return 0; }
	public float getEnginePower(){ return 0; }
}
