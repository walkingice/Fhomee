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
public class Room extends GLObject {

    final String TAG = "Room";

    Wall mWall;
    Ground mGround;

    public static final float WIDTH  = Math.abs(ViewManager.PROJ_RIGHT - ViewManager.PROJ_LEFT);
    public static final float HEIGHT = Math.abs(ViewManager.PROJ_TOP - ViewManager.PROJ_BOTTOM);
    public static final float LEFT   = 0f;
    public static final float TOP    = 0f;
    public static final float WALL_HEIGHT   = 38f;
    public static final float GROUND_HEIGHT = HEIGHT - WALL_HEIGHT;

    private String mWallTexture   = "wall";
    private String mGroundTexture = "ground";

    public Room(int id, String wall, String ground) {
	super(id, 0, 0, WIDTH, HEIGHT);
	mWallTexture   = wall;
	mGroundTexture = ground;

	mWall   = new Wall(WIDTH, WALL_HEIGHT, mWallTexture);
	mGround = new Ground(WIDTH, GROUND_HEIGHT, mGroundTexture);

	mWall.setXY(0f, 0f);
	mGround.setXY(0f, WALL_HEIGHT);

	mGround.setTextureName(mGroundTexture);

	addChild(mWall);
	addChild(mGround);
    }

    public Room(int id, Wall wall, Ground ground) {
	super(id, 0, 0, WIDTH, HEIGHT);
	mWall   = wall;
	mGround = ground;

	addChild(mWall);
	addChild(mGround);
    }

    public void addElf(GLObject elf) {
	mGround.addElf(elf);
    }
}

