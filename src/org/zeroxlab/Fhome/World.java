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

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.LinkedList;
import javax.microedition.khronos.opengles.GL10;

public class World extends GLObject implements Touchable {

    final String TAG = "World";

    private int mCurrentRoom = 0;

    public boolean mMiniMode = false;
    private final static int  LEVEL = ViewManager.LEVEL_WORLD;
    private final static float DEPTH = ViewManager.getZDepth(LEVEL);

    public final static float ROOM_WIDTH  = ViewManager.convertToLevel(
	    LEVEL, ViewManager.PROJ_WIDTH);
    public final static float ROOM_HEIGHT = ViewManager.convertToLevel(
	    LEVEL, ViewManager.PROJ_HEIGHT);

    /* Only draw rooms which is current, left and right */
    public final static int ROOM_VISIBLE_LEFT  = 1;
    public final static int ROOM_VISIBLE_RIGHT = 1;

    public static float BAR_HEIGHT = ROOM_HEIGHT * ViewManager.BAR_HEIGHT_RATIO;
    public static float MINIMODE_DEPTH_OFFSET;

    private static GestureManager mGestureMgr;

    public World() {
	super(0, 0, 0, 0);
	mChildren = new LinkedList<GLObject>();

	/* depth : height = LEVEL_3 : ROOM_HEIGHT */
	float height = BAR_HEIGHT + ROOM_HEIGHT + BAR_HEIGHT;
	float depth  = height * DEPTH / ROOM_HEIGHT;
	MINIMODE_DEPTH_OFFSET = depth - ViewManager.LEVEL_3;

	mGestureMgr = GestureManager.getInstance();
    }

    @Override
    public void addChild(GLObject obj) {
	Log.i(TAG, "class World only accept Room, use addRoom instead of this method");
    }

    @Override
    public void addChild(int location, GLObject obj) {
	Log.i(TAG, "class World only accept Room, use addRoom instead of this method");
    }

    public void addRoom(Room room) {
	this.addRoom(-1, room);
    }

    public void addRoom(int position, Room room) {
	super.addChild(position, room);
	resetRoomPosition();
    }

    private void resetRoomPosition() {
	for (int i = 0; i < mChildren.size(); i++) {
	    Room room = (Room)mChildren.get(i);
	    room.setXY(i * ROOM_WIDTH, 0f);
	}
    }

    public LinkedList<GLObject> createRoomThumbnails() {
	LinkedList<GLObject> list = new LinkedList<GLObject>();
	for (int i = 0; i < mChildren.size(); i++) {
	    Room room = (Room)mChildren.get(i);
	    list.add(room.createThumbnail());
	}

	return list;
    }

    public void setMiniMode() {
	mMiniMode = true;
    }

    public void setNormalMode() {
	mMiniMode = false;
    }

    @Override
    public int pointerAt(float x, float y) {
	int current = getCurrentRoom();
	Room room = (Room)mChildren.get(current);
	return room.pointerAt(x + current * ROOM_WIDTH , y);
    }

    @Override
    protected void drawChildren(GL10 gl) {
	Room room;
	int current = getCurrentRoom();
	int count   = getChildrenCount();
	int start = current - ROOM_VISIBLE_LEFT;
	int end   = count   + ROOM_VISIBLE_RIGHT;
	start = Math.max(start, 0);
	end   = Math.min(end, count);
	for (int i = start; i < end; i++) {
	    room = (Room)mChildren.get(i);
	    gl.glPushMatrix();
	    room.draw(gl);
	    gl.glPopMatrix();
	}
    }

    public int getRoomNumber() {
	return mChildren.size();
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

	if (nextRoom >= mChildren.size()) {
	    nextRoom = mChildren.size() -1;
	} else if (nextRoom < 0) {
	    nextRoom = 0;
	}

	mCurrentRoom = nextRoom;

	float endX = -1 * nextRoom * ROOM_WIDTH;
	GLTranslate ani = new GLTranslate(time, endX, 0);
	this.setAnimation(ani);
	Timeline.getInstance().addAnimation(ani);
    }

    public boolean onPressEvent(PointF point, MotionEvent event) {
	return true;
    }

    public boolean onReleaseEvent(PointF point, MotionEvent event) {
	if (mGestureMgr.mSnapToNext) {
	    this.moveToRoom(this.getCurrentRoom() + 1);
	} else if (mGestureMgr.mSnapToPrev) {
	    this.moveToRoom(this.getCurrentRoom() - 1);
	} else {
	    this.moveToRoom(this.getCurrentRoom());
	}

	return true;
    }

    public boolean onDragEvent(PointF point, MotionEvent event) {
	int dx = mGestureMgr.getDeltaX();
	float levelX = ViewManager.convertToLevel(ViewManager.LEVEL_WORLD, dx);
	int current = this.getCurrentRoom();
	int rooms   = this.getChildrenCount();

	/* At leftest room and slide to right
	   Or at rightest romm and slide to left */
	if ((current == 0 && dx > 0)
		|| (current == rooms - 1 && dx < 0)) {
	    levelX = levelX / ViewManager.PROJ_WIDTH;
	} else {
	    levelX = levelX * 3 / ViewManager.PROJ_WIDTH;
	}

	float endX = -1 * current * Room.WIDTH + levelX;
	float endY = 0;

	this.setXY(endX, endY);

	return true;
    }
}

