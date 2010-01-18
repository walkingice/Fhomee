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

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.LinkedList;
import javax.microedition.khronos.opengles.GL10;

public class World extends GLObject {

    final String TAG = "World";
    private LinkedList<Room> mRooms;
    private int mCurrentRoom = 0;

    public final static float ROOM_WIDTH  = ViewManager.convertToLevel(3, ViewManager.PROJ_WIDTH);
    public final static float ROOM_HEIGHT = ViewManager.convertToLevel(3, ViewManager.PROJ_HEIGHT);

    public World() {
	this(-1);
    }

    public World(int id) {
	super(id, 0, 0, 0, 0);
	mRooms = new LinkedList<Room>();
    }

    public void addRoom(Room room) {
	this.addRoom(-1, room);
    }

    public void addRoom(int position, Room room) {
	int pos = position;
	if (pos < 0 || pos > mRooms.size()) {
	    pos = mRooms.size(); // add to tail
	}

	mRooms.add(pos, room);
	resetRoomPosition();
    }

    private void resetRoomPosition() {
	for (int i = 0; i < mRooms.size(); i++) {
	    Room room = mRooms.get(i);
	    room.setXY(i * ROOM_WIDTH, 0f);
	}
    }

    @Override
    public int pointerAt(float nearX, float nearY) {
	int current = getCurrentRoom();
	Room room = mRooms.get(current);
	// Rooms locate at Level 3 layer
	float levelX = ViewManager.convertToLevel(3, nearX);
	float levelY = ViewManager.convertToLevel(3, nearY);
	return room.pointerAt(levelX + current * ROOM_WIDTH , levelY);
    }

    public void draw(GL10 gl) {
	gl.glTranslatef(mPosition.x, mPosition.y, mDepth);
	Room room;
	synchronized(mAnimationLock) {
	    if (mAnimation != null) {
		mAnimation.applyAnimation(gl);
	    }
	}
	for (int i = 0; i < mRooms.size(); i++) {
	    room = mRooms.get(i);
	    gl.glPushMatrix();
	    room.draw(gl);
	    gl.glPopMatrix();
	}
    }

    public int getRoomNumber() {
	return mRooms.size();
    }

    public int getCurrentRoom() {
	return mCurrentRoom;
    }

    public void moveToNextRoom() {
	moveToRoom(mCurrentRoom + 1);
    }

    public void moveToPrevRoom() {
	moveToRoom(mCurrentRoom - 1);
    }

    public void moveToRoom(int newRoom) {
	int nextRoom = newRoom;
	long time = 100;

	if (nextRoom >= mRooms.size()) {
	    nextRoom = mRooms.size() -1;
	} else if (nextRoom < 0) {
	    nextRoom = 0;
	}

	mCurrentRoom = nextRoom;

	float endX = -1 * nextRoom * ROOM_WIDTH;
	GLTranslate ani = new GLTranslate(time, endX, 0);
	this.setAnimation(ani);
	Timeline.getInstance().addAnimation(ani);
    }
}

