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
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

import java.util.LinkedList;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

import org.zeroxlab.Fhome.TextureManager.TextureObj;

/** 
 * GLObject represent any Object on the screen
 * it encapsulants the information including position, size...etc
 */
public class GLObject {

    final String TAG = "GLObject";

    protected static ResourcesManager ResourcesMgr = ResourcesManager.getInstance();
    protected static TextureManager   TextureMgr   = TextureManager.getInstance();

    protected float mDepth = 0f;

    ClickListener mListener;
    GLView mGLView;
    PointF mPosition;
    RectF  mRect;
    float  mAngle = 0f;

    protected boolean mVisible = false;

    Matrix mInvert;
    float mPts[];

    protected String mDefaultTextureName;

    private int mID = -1;
    protected boolean mHasChildren = false;
    LinkedList<GLObject> mChildren;

    protected GLAnimation mAnimation;
    protected Object mAnimationLock;

    GLObject(float width, float height) {
	this(0f, 0f, width, height);
    }

    GLObject(float x, float y, float width, float height) {
	mRect = new RectF(0, 0, width, height);
	mPosition = new PointF();
	mAnimationLock = new Object();

	mID = ObjectManager.getInstance().register(this);

	mInvert = new Matrix();
	mPts    = new float[2];
	setXY(x, y);
	resetInvertMatrix();
    }

    public boolean contains(float x, float y) {
	mPts[0] = x;
	mPts[1] = y;
	mInvert.mapPoints(mPts);
	return mRect.contains(mPts[0], mPts[1]);
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

	if (id == -1 && mRect.contains(mPts[0], mPts[1])) {
	    id = mID;
	}

	if (id != -1) {
	    Log.i(TAG,"Pointer at "+ id);
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
	destroyGLView();
    }

    public int getId() {
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
	if (mGLView != null) {
	    mGLView.setSize(mRect);
	}
    }

    public void setAngle(float angle) {
	mAngle = angle % 360;
	resetInvertMatrix();
    }

    public float getAngle() {
	return mAngle;
    }

    public float getWidth() {
	return this.width();
    }

    public float getHeight() {
	return this.height();
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

    /**
     * Set a texture id to this GLObject.
     * The GLObject will draw itself with this texture.
     * But this method NEVER change the Default Texture ID
     *
     * @param id The id of texture which will be drawed.
     */
    public void setTexture(TextureObj obj) {
	if (mVisible != true) {
	    createGLView();
	}

	mGLView.setTexture(obj);
    }

    public TextureObj getDefaultTexture() {
	if (mVisible) {
	    return mGLView.getTexture();
	}

	return null;
    }

    /**
     * Reset the Default Texture name *ONLY*.
     * The GL Context of TextureManager might change.
     * TextureMgr will assign the Texture Id to TextureObj
     *
     * @param name The Default texture name.
     */
    public void setDefaultTextureName(String name) {
	destroyGLView();
	mDefaultTextureName = name;
	createGLView();
    }

    protected void createGLView() {
	/* This GLObjew is visible and has texture, create a GLView */
	if (mGLView == null) {
	    mGLView = new GLView();
	    mGLView.setSize(mRect);

	    TextureObj texture;
	    Bitmap bitmap;
	    bitmap = ResourcesMgr.getBitmapByName(mDefaultTextureName);
	    texture= TextureMgr.getTextureObj(bitmap, mDefaultTextureName);
	    bitmap.recycle();
	    mGLView.setTexture(texture);
	}

	mVisible = true;
    }

    private void destroyGLView() {
	if (mVisible) {
	    TextureObj obj = mGLView.getTexture();
	    TextureMgr.removeTextureObj(obj);
	    mGLView  = null;
	    mVisible = false;
	}
    }

    /**
     * Add a GLObject as a child to tail of the list
     *
     * @param obj The child
     */
    public void addChild(GLObject obj) {
	this.addChild(-1, obj);
    }

    /**
     * Add a GLObject as a child to specified position
     *
     * @param location The specified position
     * @param obj The child
     */
    public void addChild(int location, GLObject obj) {
	int position = location;
	if (mChildren == null) {
	    mChildren = new LinkedList<GLObject>();
	}

	if (position < 0 || position > mChildren.size()) {
	    position = mChildren.size(); // add to tail
	}

	mChildren.add(position, obj);
	mHasChildren = true;
    }

    public GLObject removeChild(int index) {
	GLObject obj = mChildren.remove(index);

	if (mChildren.size() == 0) {
	    mHasChildren = false;
	}

	return obj;
    }

    public int getChildrenCount() {
	if (mChildren == null) {
	    return 0;
	}
	return mChildren.size();
    }

    public void setDepth(float depth) {
	mDepth = depth;
    }

    /* This GLObject locate at a position which relate to its parent
       Move the ModelView Matrix to the position */
    protected void moveModelViewToPosition(GL10 gl) {
	gl.glTranslatef(mPosition.x, mPosition.y, mDepth);
	gl.glRotatef(mAngle, 0, 0, 1f);
    }

    public void draw(GL10 gl) {
	moveModelViewToPosition(gl);
	if (mVisible) {
	    boolean drawMyself = true;
	    synchronized (mAnimationLock) {
		if (mAnimation != null) {
		    drawMyself = mAnimation.applyAnimation(gl);
		}
	    }

	    if (drawMyself) {
		mGLView.drawGLView(gl);
	    }

	    /* Animation might change drawing color, reset it. */
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

    public void setListener(ClickListener listener) {
	mListener = listener;
    }

    public void onClick() {
	if (mListener != null) {
	    mListener.onClick(this);
	}
    }

    interface ClickListener {
	public void onClick(GLObject obj);
    }
}

