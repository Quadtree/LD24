package com.ironalloygames.core;

import org.jbox2d.common.Vec2;

import playn.core.PlayN;
import playn.core.Surface;

public class Camera {
	public Surface surf;
	
	Vec2 position = new Vec2(0,0);
	float zoom = 5 / PlayN.graphics().height();
	
	public void setCamera(Vec2 position, float zoom)
	{
		this.position = new Vec2(position);
		this.zoom = zoom / PlayN.graphics().height();
	}
	
	public Vec2 realToScreen(Vec2 input)
	{
		Vec2 t = new Vec2(input);
		t.subLocal(position);
		
		t.mulLocal(zoom);
		
		t.x += 0.5f * PlayN.graphics().width() / PlayN.graphics().height();
		t.y += 0.5f;
		
		// we are now in normalized device coordinates
		
		t.y = 1 - t.y;
		
		t.x *= PlayN.graphics().height();
		t.y *= PlayN.graphics().height();
		
		// we are now in pixel coordinates
		
		return t;
	}
	
	public void drawLine(Vec2 start, Vec2 end, int color)
	{
		Vec2 screenStart = realToScreen(start);
		Vec2 screenEnd = realToScreen(end);
		
		surf.setFillColor(color);
		surf.drawLine(screenStart.x, screenStart.y, screenEnd.x, screenEnd.y, 1);
	}
}
