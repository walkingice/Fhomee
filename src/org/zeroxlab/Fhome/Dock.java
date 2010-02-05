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

    public void readThumbnails(World world) {
	if (world == null) {
	    Log.i(TAG, "Error: World is null");
	    return;
	}

	LinkedList<GLObject> list = world.createRoomThumbnails();
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

    @Override
    public void draw(GL10 gl) {
	moveModelViewToPosition(gl);
	if (!mVisible) {
	    return;
	}

	boolean drawMyself = true;
	synchronized (mAnimationLock) {
	    if (mAnimation != null) {
		drawMyself = mAnimation.applyAnimation(gl);
	    }
	}

	if (drawMyself) {
	    mGLView.drawGLView(gl);

	    if (!mHasChildren) {
		return;
	    }

	    GLObject obj;
	    for (int i = 0; i < mChildren.size(); i++) {
		obj = mChildren.get(i);
		gl.glPushMatrix();
		obj.draw(gl);
		gl.glPopMatrix();
	    }
	}
    }
}
