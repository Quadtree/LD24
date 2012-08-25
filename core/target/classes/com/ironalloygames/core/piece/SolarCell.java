package com.ironalloygames.core.piece;

import playn.core.Color;

public class SolarCell extends Piece {
	protected int getColor(){ return Color.rgb(0, 255, 0); }

	@Override
	public float getSolarPower() {
		return 1 * getLength();
	}
}
