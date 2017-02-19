package com.ironalloygames.core.piece;

import com.ironalloygames.core.PetriDishEmpire;

import playn.core.Color;

public class SolarCell extends Piece {
	protected int getColor(){ return Color.rgb(0, 255, 0); }

	@Override
	public float getSolarPower() {
		return getMass() / 1400;
	}
	
	public float getCostMod(){ return 1.6f; }
	
	public static float getSolarCellEffectiveness()
	{
		return 1 - PetriDishEmpire.s.lastFrameTotalBiomass / (2200 * PetriDishEmpire.getDishAreaModifier());
	}
}
