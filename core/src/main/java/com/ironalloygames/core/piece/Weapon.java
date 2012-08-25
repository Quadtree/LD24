package com.ironalloygames.core.piece;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

import com.ironalloygames.core.PetriDishEmpire;
import com.ironalloygames.core.Spark;

import playn.core.Color;

public class Weapon extends Piece {
	private static final int CHARGE_TIME = 60;
	private static final float DAMAGE_MOD = 1.f;

	protected int getColor(){ return Color.rgb(255, 0, 0); }
	
	float damage = 0;
	
	public void update()
	{
		damage = Math.min((damage + getMass() / CHARGE_TIME) * DAMAGE_MOD, getMass() * DAMAGE_MOD);
		
		for(Piece p : contactList)
		{
			if(owner.playerOwned == false || p.owner.playerOwned == false)
			{
				p.takeDamage(damage);
				
				p.emitSparks((int)(damage*0.5f), Color.rgb(255, 0, 0));
				
				damage = 0;
				break;
			}
		}
	}
}
