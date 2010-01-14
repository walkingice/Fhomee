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
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

import java.util.LinkedList;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

/** 
 * GLObject represent any Object on the screen
 * it encapsulants the information including position, size...etc
 */
public class GLObject {

    final String TAG = "GLObject";

    protected float mDepth = 0f;

    GLView mGLView;
    PointF mPosition;
    RectF  mRect;
    float  mAngle = 0f;

    Matrix mInvert;
    float mPts[];

    protected int mTextureID = -1;
    String mTextureName = "zeroxdoll"; //default

    private int mID = -1;
    protected boolean mHasChildren = false;
    LinkedList<GLObject> mChildren;

    final static long DEFAULT_RATE = 100000; // 100secs
    protected GLAnimation mAnimation;
    protected GLTransition mTransition;
    protected Object mAnimationLock;

    GLObject(float x, float y, float width, float height) {
	this(-1, x, y, width, height);
    }

    GLObject(int id, float x, float y, float width, float height) {
	mRect = new RectF(0, 0, width, height);
	mPosition = new PointF(x, y);
	mAnimationLock = new Object();

	mID = ObjectManager.getInstance().register(this);

	mInvert = new Matrix();
	mPts    = new float[2];
	resetInvertMatrix();
    }

    public int pointerAt(float x, float y) {
	int id = -1;

	/* GLView is a rectangle. However, GLObject may be Rotated
	 * or Translated. If we got a Point, just apply a reverse
	 * matrix to the point then we can regard the GLObject
	 * is alinging the Origin.
	 */
	mPts[0] = x;
	mPts[1] = y;
	mInvert.mapPoints(mPts); // apply reverse matrix

	if (mRect.contains(mPts[0], mPts[1])) {
	    if (mHasChildren) {
		GLObject obj;
		for (int i = 0; i < mChildren.size(); i++) {
		    obj = mChildren.get(i);
		    id  = obj.pointerAt(mPts[0], mPts[1]);
		    if (id != -1) {
			i = mChildren.size(); // break the loop
		    }
		}
	    }
	    if (id == -1) {
		id = mID;
	    }
	} else {
	    id = -1;
	}

	return id;
    }

    /* A GLObject may be Translated or Rotated from Origin.
     * mInvert holds a reverse matrix that could invert a point.
     */
    private void resetInvertMatrix() {
	mInvert.reset();
	mInvert.postTranslate(-1 * mPosition.x, -1 * mPosition.y);
	mInvert.postRotate(-1 * mAngle);
    }

    /* Before you drop this GLObject, please call this method
     * for reducing your memory usage.
     */
    public void clear() {
	ObjectManager.getInstance().unregister(this);
	// Maybe we need clear up its texture like...
	// TextureManager.clearTexture(blahblah);
    }

    public int getID() {
	return mID;
    }

    public float getX() {
	return mPosition.x;
    }

    public float getY() {
	return mPosition.y;
    }

    public void setSize(float width, float height) {
	mRect.set(0, 0, width, height);
	mGLView.setSize(mRect);
    }

    public void setAngle(float angle) {
	mAngle = angle % 360;
	resetInvertMatrix();
    }

    public float getAngle() {
	return mAngle;
    }

    public float width() {
	return mRect.width();
    }

    public float height() {
	return mRect.height();
    }

    public void setXY(float x, float y) {
	mPosition.x = x;
	mPosition.y = y;
	resetInvertMatrix();
    }

    public void setTransition(GLTransition transition) {
	mTransition = transition;
    }

    public void clearTransition() {
	setTransition(null);
    }

    public void setAnimation(GLAnimation animation) {
	if (mAnimation != null) {
	    clearAnimation();
	}
	animation.bindGLObject(this);
	mAnimation = animation;
    }

    public void clearAnimation() {
	synchronized (mAnimationLock) {
	    if (mAnimation != null) {
		mAnimation.unbindGLObject();
	    }
	    mAnimation = null;
	}
    }

    private void setTextureID(int id) {
	mGLView.setTextureID(id);
    }

    public void setTextureName(String name) {
	/* This GLObjew is visible and has texture, create a GLView */
	if (mGLView == null) {
	    mGLView = new GLView();
	    mGLView.setSize(mRect);
	}

	mTextureName = name;
    }

    public void addChild(GLObject obj) {
	if (mChildren == null) {
	    mChildren = new LinkedList<GLObject>();
	}

	mChildren.add(obj);
	mHasChildren = true;
    }

    public GLObject removeChild(int index) {
	GLObject obj = mChildren.remove(index);

	if (mChildren.size() == 0) {
	    mHasChildren = false;
	}

	return obj;
    }

    public void setDepth(float depth) {
	mDepth = depth;
    }

    protected void moveModelViewToPosition(GL10 gl) {
	gl.glTranslatef(mPosition.x, mPosition.y, mDepth);
	gl.glRotatef(mAngle, 0, 0, 1f);
    }

    public void generateTextures(GL10 gl, ResourcesManager resM, TextureManager texM) {
	if (mGLView != null) {
	    int    id;
	    Bitmap bitmap;
	    bitmap = resM.getBitmapByName(mTextureName);
	    id     = texM.generateOneTexture(gl, bitmap, mTextureName);
	    mGLView.setTextureID(id);
	    mTextureID = id;

	    if (mTransition != null) {
		mTransition.generateTextures(gl, resM, texM);
	    }
	}

	if (mHasChildren) {
	    GLObject obj;
	    for (int i = 0; i < mChildren.size(); i++) {
		obj = mChildren.get(i);
		obj.generateTextures(gl, resM, texM);
	    }
	}
    }

    public long getUpdateRate() {
	long rate = -1;
	if (mTransition != null) {
	    rate = mTransition.getUpdateRate();
	}

	if (mHasChildren) {
	    long min = DEFAULT_RATE;
	    for (int i = 0; i < mChildren.size(); i++) {
		long childRate = mChildren.get(i).getUpdateRate();
		if (childRate < min && childRate != -1) {
		    min = childRate;
		}
	    }

	    if(min != DEFAULT_RATE) {
		rate = min;
	    }
	}

	return rate;
    }

    public void draw(GL10 gl) {
	moveModelViewToPosition(gl);
	if (mGLView != null) {
	    boolean drawMyself = true;
	    synchronized (mAnimationLock) {
		if (mAnimation != null) {
		    drawMyself = mAnimation.applyAnimation(gl);
		}
	    }

	    if (drawMyself) {
		if (mTransition != null) {
		    mGLView.setTextureID(mTransition.getNowTextureID());
		} else {
		    mGLView.setTextureID(mTextureID);
		}
		mGLView.drawGLView(gl);
	    }
	    gl.glColor4f(1f, 1f, 1f, 1f);
	}

	if (mHasChildren) {
	    GLObject obj;
	    for (int i = 0; i < mChildren.size(); i++) {
		obj = mChildren.get(i);

		gl.glPushMatrix();
		obj.draw(gl);
		gl.glPopMatrix();
	    }
	}

	return;
    }
}

