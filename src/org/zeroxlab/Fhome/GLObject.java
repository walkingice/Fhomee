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
    RectF  mCoverage;
    RectF  mTmpRect;
    float  mAngle = 0f;

    /* Stores the position, size of this GLObject
     * These data represent in Pixel-base of Screen.
     * Once method measure was called, reset position
     * and size according to these data
     */
    public final static float UNDEFINE = -1f;
    protected float mXPx = UNDEFINE;
    protected float mYPx = UNDEFINE;
    protected float mWidthPx  = UNDEFINE;
    protected float mHeightPx = UNDEFINE;

    protected boolean mVisible = false;

    Matrix mTranslate;
    Matrix mInvert;
    float mPts[];
    float mViewport[];
    protected boolean inViewport = true;

    private int mID = -1;
    protected boolean mChildrenVisible = true;
    protected boolean mHasChildren = false;
    protected GLObject mParent = null;
    LinkedList<GLObject> mChildren;

    protected GLAnimation mAnimation;
    protected Object mAnimationLock;
    protected Object mChildrenLock;

    GLObject(float width, float height) {
	this(0f, 0f, width, height);
    }

    GLObject(float x, float y, float width, float height) {
	mRect = new RectF(0, 0, width, height);
        mCoverage = new RectF(mRect);
        mTmpRect  = new RectF();
	mPosition = new PointF();
	mAnimationLock = new Object();
	mChildrenLock  = new Object();

	mID = ObjectManager.getInstance().register(this);

        mTranslate = new Matrix();
	mInvert = new Matrix();
	mPts    = new float[2];
        mViewport = new float[8];
	setXY(x, y);
	resetInvertMatrix();
        resetTranslateMatrix();
    }

    public void checkViewport(float[] viewport) {
        mViewport[0] = viewport[0];
        mViewport[1] = viewport[1];
        mViewport[2] = viewport[2];
        mViewport[3] = viewport[3];
        mViewport[4] = viewport[4];
        mViewport[5] = viewport[5];
        mViewport[6] = viewport[6];
        mViewport[7] = viewport[7];
        mInvert.mapPoints(mViewport);
        mTmpRect.setEmpty();
        mTmpRect.offset(mViewport[0], mViewport[1]);
        mTmpRect.union(mViewport[2], mViewport[3]);
        mTmpRect.union(mViewport[4], mViewport[5]);
        mTmpRect.union(mViewport[6], mViewport[7]);

        boolean tmp = RectF.intersects(mCoverage, mTmpRect);
        if (tmp != inViewport) {
            if (mGLView != null) {
                Log.i(TAG,mGLView.getTexture().getName() + " change visible to "+tmp);
            }
        }

        inViewport = tmp;

        if (inViewport && mHasChildren) {
            GLObject obj;
            for (int i = mChildren.size() - 1; i >= 0; i--) {
                obj = mChildren.get(i);
                obj.checkViewport(mViewport);
            }
        }

        return;
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
            /* ask the children by inverse ordering.
             * The last child will be drawed lastest
             * therefore it will be top than other children.
             * so we have to ask the last one first.
             */
	    for (int i = mChildren.size() - 1; i >= 0; i--) {
		obj = mChildren.get(i);
		id  = obj.pointerAt(mPts[0], mPts[1]);
		if (id != -1) {
		    i = -1; // break the loop
		}
	    }
	}

	if (id == -1 && mRect.contains(mPts[0], mPts[1])) {
	    id = mID;
	}

        /*
	if (id != -1) {
	    Log.i(TAG,"Pointer at "+ id);
	}
        */

	return id;
    }

    public void updateCoverage() {
        mCoverage.set(mRect);
        if (mHasChildren) {
            GLObject obj;
            for (int i = mChildren.size() - 1; i >= 0; i--) {
                obj = mChildren.get(i);
                obj.getTranslatedCoverage(mTmpRect);
                mCoverage.union(mTmpRect);
            }
        }

        if (mParent != null) {
            mParent.updateCoverage();
        }
    }

    public void getCoverage(RectF dst) {
        dst.set(mCoverage);
    }

    public void getTranslatedCoverage(RectF dst) {
        mTranslate.mapRect(dst, mCoverage);
    }

    public Matrix getTranslateMatrix() {
        return mTranslate;
    }

    private void resetTranslateMatrix() {
        mTranslate.reset();
        mTranslate.postTranslate(mPosition.x, mPosition.y);
        mTranslate.postRotate(mAngle);
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
        setTexture(null);
	destroyGLView();
    }

    public int getId() {
	return mID;
    }

    public float getXPx() {
        return mXPx;
    }

    public float getYPx() {
        return mYPx;
    }

    public float getX() {
	return mPosition.x;
    }

    public float getY() {
	return mPosition.y;
    }

    public void setVisible(boolean visible) {
	mVisible = visible;
    }

    public boolean getVisible() {
	return mVisible;
    }

    public void setChildrenVisible(boolean visible) {
        mChildrenVisible = visible;
    }

    public boolean getChildrenVisible(boolean visible) {
        return mChildrenVisible;
    }

    public void setSizePx(float widthPx, float heightPx) {
        mWidthPx  = widthPx;
        mHeightPx = heightPx;
    }

    public void setSize(float width, float height) {
	mRect.set(0, 0, width, height);
	if (mGLView != null) {
	    mGLView.setSize(mRect);
	}

        updateCoverage();
    }

    public void setAngle(float angle) {
	mAngle = angle % 360;
	resetInvertMatrix();
        resetTranslateMatrix();
    }

    public float getAngle() {
	return mAngle;
    }

    public float getWidthPx() {
        return mWidthPx;
    }

    public float getHeightPx() {
        return mHeightPx;
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

    public void setXYPx(float xPx, float yPx) {
        mXPx = xPx;
        mYPx = yPx;
    }

    public void setXY(float x, float y) {
	mPosition.x = x;
	mPosition.y = y;
	resetInvertMatrix();
        resetTranslateMatrix();
    }

    public boolean measure(float ratioX, float ratioY) {
        boolean updated = false;

        if (mXPx != UNDEFINE && mYPx != UNDEFINE) {
            setXY(ratioX * mXPx, ratioY * mYPx);
            Log.i(TAG,mGLView.getTexture().getName() + " set xy to ("+ratioX * mXPx +","+ratioY * mYPx+")");
            updated = true;
        }

        if (mWidthPx != UNDEFINE && mHeightPx != UNDEFINE) {
            setSize(ratioX * mWidthPx, ratioY * mHeightPx);
            Log.i(TAG,mGLView.getTexture().getName() + " set width to ("+ratioX * mWidthPx +","+ratioY * mHeightPx+")");
            updated = true;
        }

        if (mHasChildren) {
            updated = measureChildren(ratioX, ratioY);
        }

        return updated;
    }

    protected boolean measureChildren(float ratioX, float ratioY) {
	GLObject obj;
        boolean updated = false;
	for (int i = 0; i < mChildren.size(); i++) {
            obj = mChildren.get(i);
            boolean childUpdated = obj.measure(ratioX, ratioY);
            updated = (updated || childUpdated);
        }
        return updated;
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
     * Set texture to this GLObject.
     * The GLObject will draw itself with this texture.
     * @param name The name of the Drawable of the texture
     */
    public void setTextureByName(String name) {
        TextureObj texture;
        Bitmap bitmap;
        bitmap  = ResourcesMgr.getBitmapByName(name);
        texture = TextureMgr.getTextureObj(bitmap, name);
        bitmap.recycle();
        this.setTexture(texture);
    }

    public void setTexture(TextureObj obj) {
	if (mGLView == null) {
	    createGLView();
	}

        TextureObj old = mGLView.getTexture();
	mGLView.setTexture(obj);

        if (obj != null) {
            obj.increaseBinding();
        }
        if (old != null) {
            old.decreaseBinding();
        }
    }

    public TextureObj getTexture() {
	if (mGLView != null) {
	    return mGLView.getTexture();
	}

	return null;
    }

    protected void createGLView() {
	if (mGLView == null) {
	    mGLView = new GLView();
	    mGLView.setSize(mRect);
	}

	mVisible = true;
    }

    protected void destroyGLView() {
	if (mGLView != null) {
	    mGLView.clear();
	    mGLView  = null;
	}

	mVisible = false;
    }

    public GLObject getParent() {
        return mParent;
    }

    public void setParent(GLObject parent) {
        if (mParent != null) {
            mParent.removeChild(this);
        }

        mParent = parent;
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
	synchronized(mChildrenLock) {
	    int position = location;
	    if (mChildren == null) {
		mChildren = new LinkedList<GLObject>();
	    }

	    if (position < 0 || position > mChildren.size()) {
		position = mChildren.size(); // add to tail
	    }

            obj.setParent(this);
	    mChildren.add(position, obj);
	    mHasChildren = true;
	}
    }

    public GLObject removeChild(GLObject obj) {
	if (mChildren == null) {
	    return null;
	}

	int index = mChildren.indexOf(obj);
	return removeChild(index);
    }

    public GLObject removeChild(int index) {
	GLObject obj;
	synchronized(mChildrenLock) {
	    if (mChildren == null) {
		return null;
	    }

	    if (index < 0 || index >= mChildren.size()) {
		return null;
	    }

	    obj = mChildren.remove(index);

	    if (mChildren.size() == 0) {
		mHasChildren = false;
	    }
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

    protected void drawChildren(GL10 gl) {
	GLObject obj;
	for (int i = 0; i < mChildren.size(); i++) {
	    obj = mChildren.get(i);

	    gl.glPushMatrix();
	    obj.draw(gl);
	    gl.glPopMatrix();
	}
    }

    protected boolean applyAnimation(GL10 gl) {
	boolean drawGLView = true;
	synchronized (mAnimationLock) {
	    if (mAnimation != null) {
		drawGLView = mAnimation.applyAnimation(gl);
	    }
	}

	return drawGLView;
    }

    protected void drawMyself(GL10 gl) {
	if (mGLView == null) {
	    return;
	}

	mGLView.drawGLView(gl);
	/* Animation might change drawing color, reset it. */
	gl.glColor4f(1f, 1f, 1f, 1f);
    }

    public void draw(GL10 gl) {
	moveModelViewToPosition(gl);

	boolean drawGLView;
	drawGLView = applyAnimation(gl);

	if (mVisible && drawGLView) {
	    drawMyself(gl);
	}

	if (mHasChildren && mChildrenVisible) {
	    drawChildren(gl);
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

