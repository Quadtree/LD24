package com.ironalloygames.core.piece;

import java.util.ArrayList;

import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

import com.ironalloygames.core.Camera;
import com.ironalloygames.core.Creature;
import com.ironalloygames.core.PetriDishEmpire;
import com.ironalloygames.core.Spark;

import playn.core.Color;

public class Piece {
	Vec2 start;
	Vec2 end;
	public Fixture fixture;
	public Creature owner;
	
	public float width;
	
	public float hp;
	
	public ArrayList<Piece> contactList = new ArrayList<Piece>();
	
	public ArrayList<Piece> childPieces = new ArrayList<Piece>();
	
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
	
	public float getMass()
	{
		if(fixture == null) return 0;
		
		MassData md = new MassData();
		fixture.getMassData(md);
		return md.mass;
	}
	
	public void emitSparks(int count, int color)
	{
		if(count < 1)
		{
			if(PetriDishEmpire.s.rand.nextFloat() < 0.2)
			{
				count = 1;
			}
		}
		
		Transform t = new Transform();
		t.set(owner.body.getPosition(), owner.body.getAngle());
		
		Vec2 startAbs = Transform.mul(t, start);
		Vec2 endAbs = Transform.mul(t, end);
		
		for(int i=0;i<count;++i)
		{
			float pt = PetriDishEmpire.s.rand.nextFloat();
			
			Vec2 centerPoint = startAbs.mul(pt).add(endAbs.mul(1 - pt));
			
			PetriDishEmpire.s.entityAddQueue.add(new Spark(centerPoint, color));
		}
	}
	
	public void render(Camera cam, Creature crt)
	{
		Transform t = new Transform();
		t.set(crt.body.getPosition(), crt.body.getAngle());
		
		Vec2 startAbs = Transform.mul(t, start);
		Vec2 endAbs = Transform.mul(t, end);
		
		cam.drawLine(startAbs, endAbs, getColor(), width);
	}
	
	public FixtureDef getFixtureDef()
	{
		FixtureDef fd = new FixtureDef();
		fd.density = 1;
		fd.userData = this;
		
		PolygonShape ps = new PolygonShape();
		ps.setAsBox((getLength() / 2 + 0.2f), 0.1f+(width/2), (start.add(end)).mul(0.5f), getAngle());
		
		fd.shape = ps;
		
		return fd;
	}
	
	public float takeDamage(float amount)
	{
		amount /= getHPMod();
		
		if(hp <= 0) return 0;
		
		float food = 0;
		
		hp -= amount;
		
		if(hp <= 0)
		{
			emitSparks((int)(getMass()*1f), getColor());
			
			food += getMass();
			
			for(Piece p : childPieces)
			{
				food += p.takeDamage(100000);
			}
			
			childPieces.clear();
		} else {
			emitSparks((int)(amount*3f), getHitSparkColor());
		}
		
		return food;
	}
	
	public int getHitSparkColor(){ return Color.rgb(255, 0, 0); }
	
	public void update(){}
	
	public float getSolarPower(){ return 0; }
	public float getEnginePower(){ return 0; }
	
	public float getHPMod(){ return 1; }
	
	public float getCostMod(){ return 1; }
}
