package com.ironalloygames.core;

import java.util.HashMap;

import org.jbox2d.common.Vec2;

import playn.core.Image;
import playn.core.PlayN;
import playn.core.Surface;

public class Camera {
	public Surface surf;
	
	Vec2 position = new Vec2(0,0);
	float zoom = 5 / PlayN.graphics().height();
	
	HashMap<String, Image> images = new HashMap<String, Image>();
	
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
	
	public Vec2 screenToReal(Vec2 input)
	{
		Vec2 t = new Vec2(input);
		
		t.x /= PlayN.graphics().height();
		t.y /= PlayN.graphics().height();
		
		t.y = 1 - t.y;
		
		t.x -= 0.5f * PlayN.graphics().width() / PlayN.graphics().height();
		t.y -= 0.5f;
		
		t.mulLocal(1.f / zoom);
		
		t.addLocal(position);
		
		return t;
	}
	
	public void drawLine(Vec2 start, Vec2 end, int color)
	{
		drawLine(start,end,color,1 / zoom / PlayN.graphics().height());
	}
	
	public void drawLine(Vec2 start, Vec2 end, int color, float width)
	{
		Vec2 screenStart = realToScreen(start);
		Vec2 screenEnd = realToScreen(end);
		
		surf.setFillColor(color);
		surf.drawLine(screenStart.x, screenStart.y, screenEnd.x, screenEnd.y, width*zoom*PlayN.graphics().height());
	}
	
	public void drawImage(Vec2 pos, String imageName)
	{
		Vec2 screenPos = realToScreen(pos);
		
		if(!images.containsKey(imageName))
		{
			images.put(imageName, PlayN.assets().getImage("images/" + imageName + ".png"));
		}
		
		surf.drawImageCentered(images.get(imageName), screenPos.x, screenPos.y);
	}
	
	public void drawRectangle(Vec2 center, Vec2 size, int color)
	{
		Vec2 upperLeft = center.sub(size.mul(0.5f));
		Vec2 lowerRight = center.add(size.mul(0.5f));
		
		upperLeft = realToScreen(upperLeft);
		lowerRight = realToScreen(lowerRight);
		
		Vec2 screenSize = lowerRight.sub(upperLeft);
		
		surf.setFillColor(color);
		surf.fillRect(upperLeft.x, upperLeft.y, screenSize.x, screenSize.y);
	}
}
