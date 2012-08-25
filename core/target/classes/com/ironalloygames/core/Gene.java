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
	
	public static final float MUTATION_MUL = 0.25f;
	
	float geneType;
	float length;
	float width;
	float angle;
	float backtrack;
	
	public Gene()
	{
		geneType = PetriDishEmpire.s.rand.nextInt(6);
		length = (float)PetriDishEmpire.s.rand.nextGaussian() * 2.f + 4.f;
		width = (float)PetriDishEmpire.s.rand.nextGaussian() * 0.2f + 0.5f;
		angle = (float)PetriDishEmpire.s.rand.nextGaussian() * 0.5f;
		backtrack = (float)PetriDishEmpire.s.rand.nextGaussian() * 2;
		
		clamp();
	}
	
	public Gene(float geneType, float length, float angle, float backtrack) {
		super();
		this.geneType = geneType;
		this.length = length;
		this.angle = angle;
		this.backtrack = backtrack;
		
		clamp();
	}
	
	protected void clamp()
	{
		length = Math.max(length, 0.2f);
		backtrack = Math.max(backtrack, 0);
		width = Math.max(width, 0.2f);
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
		
		ng.geneType += PetriDishEmpire.s.rand.nextGaussian() * 0.35f * MUTATION_MUL;
		ng.length += PetriDishEmpire.s.rand.nextGaussian() * 0.3f * MUTATION_MUL;
		ng.angle += PetriDishEmpire.s.rand.nextGaussian() * MUTATION_MUL;
		ng.backtrack += PetriDishEmpire.s.rand.nextGaussian() * 0.5f * MUTATION_MUL;
		
		ng.clamp();
		
		return ng;
	}
	
	public void build(List<Piece> previous, float startAngle, Creature parentCreature)
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
			default: p = new Structure(); break;
		}
		
		
		int bti = Math.round(backtrack);
		
		Vec2 parentEnd = new Vec2(0,0);
		float parentAngle = startAngle;
		Piece parent = null;
		
		if(previous.size() > 0)
		{
			parent = previous.get(Math.max(previous.size() - bti - 1, 0));
			
			parentEnd = parent.getEnd();
			parentAngle = parent.getAngle();
		}
		
		p.setStart(parentEnd);
		
		Vec2 end = p.getStart();
		
		float absAngle = parentAngle + angle;
		
		end.addLocal(MathUtils.cos(absAngle) * length, MathUtils.sin(absAngle) * length);
		
		p.setEnd(end);
		
		p.owner = parentCreature;
		
		parentCreature.radius = Math.max(parentCreature.radius, end.length());
		
		previous.add(p);
		
		if(parent != null)
		{
			parent.childPieces.add(p);
		}
		
		p.width = width;
		
		p.hp = p.getLength() * p.getHPMod();
	}
}
