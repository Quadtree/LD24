package com.ironalloygames.core;

import java.util.List;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import com.ironalloygames.core.piece.*;

public class Gene {
	public static final int GT_STRUCTURE = 0;
	public static final int GT_ENGINE = 1;
	public static final int GT_SOLARCELL = 2;
	public static final int GT_ARMOR = 3;
	public static final int GT_WEAKPOINT = 4;
	public static final int GT_WEAPON = 5;
	
	float geneType;
	float length;
	float angle;
	float backtrack;
	
	public Gene(float geneType, float length, float angle, float backtrack) {
		super();
		this.geneType = geneType;
		this.length = length;
		this.angle = angle;
		this.backtrack = backtrack;
	}
	
	protected Gene(Gene g)
	{
		super();
		this.geneType = g.geneType;
		this.length = g.length;
		this.angle = g.angle;
		this.backtrack = g.backtrack;
	}

	public Gene mutate()
	{
		Gene ng = new Gene(this);
		
		ng.geneType += PetriDishEmpire.s.rand.nextGaussian() * 0.35f;
		ng.length += PetriDishEmpire.s.rand.nextGaussian() * 0.3f;
		ng.angle += PetriDishEmpire.s.rand.nextGaussian();
		ng.backtrack += PetriDishEmpire.s.rand.nextGaussian() * 0.5f;
		
		return ng;
	}
	
	public void build(List<Piece> previous, float startAngle)
	{
		Piece p = null;
		
		switch(Math.round(geneType))
		{
			case GT_STRUCTURE: p = new Structure(); break;
			case GT_ENGINE: p = new Engine(); break;
			case GT_SOLARCELL: p = new SolarCell(); break;
			case GT_ARMOR: p = new Armor(); break;
			case GT_WEAKPOINT: p = new WeakPoint(); break;
			case GT_WEAPON: p = new Weapon(); break;
		}
		
		
		int bti = Math.round(backtrack);
		
		Vec2 parentEnd = new Vec2(0,0);
		float parentAngle = startAngle;
		
		if(previous.size() > 0)
		{
			Piece parent = previous.get(Math.max(previous.size() - bti - 1, 0));
			
			parentEnd = parent.getEnd();
			parentAngle = parent.getAngle();
		}
		
		p.setStart(parentEnd);
		
		Vec2 end = p.getStart();
		
		float absAngle = parentAngle + angle;
		
		end.addLocal(MathUtils.cos(absAngle) * length, MathUtils.sin(absAngle) * length);
		
		p.setEnd(end);
		
		previous.add(p);
	}
}
