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

import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

public class Dock extends GLObject {

    final String TAG = "Dock";

    public final static float BORDER_RATIO = 0.1f; // 10%
    protected float mHGap = 0f;
    protected float mVGap = 0f;
    protected int mObjNum = 0;
    protected static float mObjWidth  = 0f;
    protected static float mObjHeight = 0f;

    protected int mPressObj;
    protected int mReleaseObj;
    protected int mSelectRoom;

    protected World mWorld;

    /* If we never rearrange the objects position, let call it Normal */
    protected boolean mNormal = true;

    public Dock(float width, float height) {
	this(width, height, null);
    }

    public Dock(float width, float height, String background) {
	super(0, 0, width, height);

	if (background != null) {
	    super.setDefaultTextureName(background);
	}
    }

    protected void resetSizeParameters(int size) {
	mObjNum = size;
	float width  = mRect.width();
	float height = mRect.height();

	mHGap = width  * BORDER_RATIO * 0.1f;
	mVGap = height * BORDER_RATIO;

	float total = width - (mObjNum + 1) * mHGap;
	mObjWidth = total / mObjNum;
	mObjHeight = height - mVGap * 2; // top and bottom
    }

    public static float objWidth() {
	return mObjWidth;
    }

    public static float objHeight() {
	return mObjHeight;
    }

    @Override
    public void setSize(float width, float height) {
	super.setSize(width, height);
    }

    public void bumpObjects(int press) {
	int x = press;

	if (mChildren == null || mWorld == null) {
	    return;
	}

	/* If out of bounds, do not bump any objects */
	if (x < super.getX() || x > super.width()) {
	    x = -1;
	}

	if (x == -1 && mNormal == true) {
	    return;
	} else {
	    mNormal = false;
	}

	GLObject obj;
	for (int i = 0; i < getChildrenCount(); i++) {
	    obj = mChildren.get(i);
	    if (x == -1 ) {
		float objX  = obj.getX();
		obj.setXY(objX, mVGap);
		obj.setDepth(0);
	    } else {
		int offset = Math.abs((int)obj.getX() - x);
		float ratio = offset / super.width();
		float objX  = obj.getX();
		obj.setXY(objX, mVGap + 40*(ratio - 1));
		obj.setDepth(5*(ratio - 1));
	    }
	}

	if (mWorld != null && x != -1) {
	    int now = mWorld.getCurrentRoom();
	    int next = (int)(x * mWorld.getChildrenCount()  / super.width());
	    if (now != next) {
		mWorld.moveToRoom(next);
	    }
	}

	if (x == -1) {
	    /* Reset whole objects to normal state */
	    mNormal = true;
	}
    }

    public void readThumbnails(World world) {
	if (world == null) {
	    Log.i(TAG, "Error: World is null");
	    return;
	}

	mWorld = world;

	LinkedList<GLObject> list = mWorld.createRoomThumbnails();
	int size = list.size();

	if (size == 0) {
	    Log.i(TAG, "Warning: No Thumbnail");
	    return;
	}

	resetSizeParameters(size);

	for (int i = 0; i < list.size(); i++) {
	    GLObject obj = list.get(i);
	    obj.setSize(mObjWidth, mObjHeight);
	    super.addChild(obj);
	}

	resetObjPosition();

	list.clear();
	list = null;
    }

    protected void resetObjPosition() {
	if (mChildren != null) {
	    GLObject obj;
	    for (int i = 0; i < getChildrenCount(); i++) {
		obj = mChildren.get(i);
		float x = mHGap + i * (mHGap + mObjWidth);
		float y = mVGap;
		obj.setXY(x, y);
	    }
	}
    }

    public void release(int x) {
	mReleaseObj = getTarget(x);
    }

    public void press(int x) {
	mPressObj = getTarget(x);
    }

    private int getTarget(int x) {
	if (x > super.width() || x <  getX()) {
	    return -1;
	}
	float ratio = x / super.width();
	return (int)(ratio * mObjNum);
    }

    public int getSelectedRoom() {
	return mSelectRoom;
    }

    @Override
    public void onClick() {
	if (mReleaseObj == mPressObj && mPressObj != -1) {
	    mSelectRoom = mReleaseObj;
	} else {
	    mSelectRoom = -1;
	}

	mReleaseObj = -1;
	mPressObj   = -1;
    }
}

