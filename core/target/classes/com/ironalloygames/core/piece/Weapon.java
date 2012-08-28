package com.ironalloygames.core.piece;

import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

import com.ironalloygames.core.AudioSystem;
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
				float food = p.takeDamage(damage) / 3;
				
				if(owner.playerOwned)
					PetriDishEmpire.s.playerMoney += food;
				else
					PetriDishEmpire.s.enemyMoney += food;
				
				if(damage > 2)
				{
					if(p instanceof Armor)
						AudioSystem.play("hit_no_effect");
					else
						AudioSystem.play("hit" + PetriDishEmpire.s.rand.nextInt(2));
				}
				
				damage = 0;
				break;
			}
		}
	}
	
	public float getCostMod(){ return 3.6f; }
}
