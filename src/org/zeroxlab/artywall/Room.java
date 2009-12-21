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


package org.zeroxlab.artywall;

import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import android.content.res.Resources;

import javax.microedition.khronos.opengles.GL10;

/** 
 * Room is a basic data structure which contain a Wall, Ground and other GLObjects.
 */
public class Room {

    final String TAG = "Room";

    public int mID = 0;

    GLObject mWall;
    GLObject mGround;

    public static final float WIDTH  = ViewManager.PROJ_RIGHT - ViewManager.PROJ_LEFT;
    public static final float HEIGHT = ViewManager.PROJ_BOTTOM - ViewManager.PROJ_TOP;
    public static final float LEFT   = ViewManager.PROJ_LEFT;
    public static final float TOP    = ViewManager.PROJ_TOP;

    private String mWallTexture   = "wall";
    private String mGroundTexture = "ground";

    public Room(int id, String wall, String ground) {
	mID = id;
	mWallTexture   = wall;
	mGroundTexture = ground;

	mWall   = new GLObject(0, 0, WIDTH, 38f);
	mGround = new GLObject(0, 38f, WIDTH, 8f);

	mWall.setDepth(0);
	mGround.setDepth(0);

	mWall.setTextureName(mWallTexture);
	mGround.setTextureName(mGroundTexture);
    }

    public Room(int id, GLObject wall, GLObject ground) {
	mID     = id;
	mWall   = wall;
	mGround = ground;
    }

    public void generateTextures(GL10 gl, ResourcesManager resM, TextureManager texM) {
	mWall.generateTextures(gl, resM, texM);
	mGround.generateTextures(gl, resM, texM);
    }

    public void draw(GL10 gl) {
	/* Every time you start drawing this room, the position of ModelView
	   should be placed at TOP-LEFT corner of this room */
	gl.glPushMatrix();
	mWall.draw(gl);
	mGround.draw(gl);
	gl.glPopMatrix();
    }
}

