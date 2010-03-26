/*
 * Authored By Julian Chu <walkingice@0xlab.org>
 *
 * Copyright (c) 2009 0xlab.org - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.zeroxlab.Fhome;

import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import android.content.res.Resources;

import javax.microedition.khronos.opengles.GL10;

/** 
 * Room is a basic data structure which contain a Wall, Ground and other GLObjects.
 */
public class Room extends GLObject {

    final String TAG = "Room";

    Wall mWall;
    Ground mGround;

    public static final float WIDTH  = World.ROOM_WIDTH;
    public static final float HEIGHT = World.ROOM_HEIGHT;
    public static final float LEFT   = 0f;
    public static final float TOP    = 0f;
    public static final float WALL_HEIGHT   = HEIGHT * 0.9f;
    public static final float GROUND_HEIGHT =
	(ViewManager.LEVEL_3 * (HEIGHT-WALL_HEIGHT)) / (HEIGHT / 2);

    private String mWallTexture   = "wall";
    private String mGroundTexture = "ground";

    public Room(String wall, String ground) {
	super(0, 0, WIDTH, HEIGHT);
	mWallTexture   = wall;
	mGroundTexture = ground;

	mWall   = new Wall(WIDTH, WALL_HEIGHT, mWallTexture);
	mGround = new Ground(WIDTH, GROUND_HEIGHT, mGroundTexture);

	mWall.setXY(0f, 0f);
	mGround.setXY(0f, 0f);

	mGround.setDefaultTextureName(mGroundTexture);

	addChild(mWall);
	addChild(mGround);
    }

    public Room(Wall wall, Ground ground) {
	super(0, 0, WIDTH, HEIGHT);
	mWall   = wall;
	mGround = ground;

	addChild(mWall);
	addChild(mGround);
    }

    public void draw(GL10 gl) {
	gl.glTranslatef(mPosition.x, mPosition.y, mDepth);
	gl.glRotatef(mAngle, 0, 0, 1f);
	gl.glPushMatrix();
	mWall.draw(gl);
	gl.glPopMatrix();

	gl.glPushMatrix();
	gl.glTranslatef(0f, WALL_HEIGHT, 0f);
	gl.glRotatef(-90f, 1f, 0f, 0f);
	mGround.draw(gl);
	gl.glPopMatrix();
    }

    public void addItem(GLObject obj, float x, float y, float angle) {
	mWall.addItem(obj, x, y , angle);
    }

    public GLObject createThumbnail() {
	GLObject obj = new GLObject(1f, 1f);
	obj.setDefaultTextureName(mWallTexture);
	return obj;
    }

    class Wall extends GLObject {

	public Wall(float width, float height, String background) {
	    super(0, 0, width, height);
	    setDefaultTextureName(background);
	}

	public void addItem(GLObject obj, float x, float y, float angle) {
	    obj.setAngle(angle);
	    obj.setXY(x, y);
	    addChild(obj);
	}
    }

    class Ground extends GLObject {

	public Ground(float width, float height, String background) {
	    super(0, 0, width, height);
	    setDefaultTextureName(background);
	}

	protected void drawChildren(GL10 gl) {
	    GLObject obj;
	    for (int i = 0; i < mChildren.size(); i++) {
		obj = mChildren.get(i);

		gl.glPushMatrix();
		gl.glRotatef(90f, 1f, 0f, 0f);
		obj.draw(gl);
		gl.glPopMatrix();
	    }
	    return;
	}
    }
}

