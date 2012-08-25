package com.ironalloygames.core.piece;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

import com.ironalloygames.core.Camera;
import com.ironalloygames.core.Creature;

import playn.core.Color;

public class Piece {
	Vec2 start;
	Vec2 end;
	
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
}
