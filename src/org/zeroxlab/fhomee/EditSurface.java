/*
 * Authored By Julian Chu <walkingice@0xlab.org>
 *
 * Copyright (c) 2010 0xlab.org - http://0xlab.org/
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


package org.zeroxlab.fhomee;

import org.zeroxlab.fhomee.core.GLObject;

import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import javax.microedition.khronos.opengles.GL10;

import java.util.LinkedList;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

import org.zeroxlab.fhomee.TextureManager.TextureObj;

public class EditSurface extends GLObject implements Touchable, GLObject.ClickListener {

    final String TAG = "EditSurface";

    Timeline    mTimeline;
    ViewManager mViewMgr;
    GestureManager mGestureMgr;

    /* The minimal size of scaling  */
    final static float MINIMAL_SIZE = 1f;

    public static String sTexBackground = "editlayer_background";
    public static String sTexDelete = "editlayer_delete";
    public static String sTexCreate = "editlayer_create_pet";
    public static String sTexRotate = "editlayer_rotate";
    public static String sTexResize = "editlayer_resize";
    public static final float sButtonWidth  = 100f;
    public static final float sButtonHeight = 100f;

    private static float sWidth  = ViewManager.mScreenWidth;
    private static float sHeight = ViewManager.mScreenHeight;

    private boolean mIsEditing = false;

    private Poster mTarget;
    private GLObject mEditing;
    private GLObject mCreate;
    private GLObject mDelete;
    private GLObject mRotate;
    private GLObject mResize;

    private GLObject mPressing;
    private GLObject mHover;

    /* Stored the initial value of Editing Target */
    private float mStartX;
    private float mStartY;
    private float mStartWidth;
    private float mStartHeight;
    private float mStartXPx;
    private float mStartYPx;
    private float mStartWidthPx;
    private float mStartHeightPx;
    private float mStartAngle;

    private float mDeltaX;
    private float mDeltaY;
    private static float[] mTmp = new float[2];
    private static Matrix mInvert = new Matrix();


    EditSurface() {
        super(0, 0, sWidth, sHeight);

        mTimeline = Timeline.getInstance();
        mViewMgr= ViewManager.getInstance();
        mGestureMgr = GestureManager.getInstance();
        setTextureByName(sTexBackground);
        mCreate = new GLObject(100, 100);
        mDelete = new GLObject(100, 100);
        mRotate = new GLObject(100, 100);
        mResize = new GLObject(100, 100);
        mEditing = new GLObject(100, 100);
        mCreate.setTextureByName(sTexCreate);
        mDelete.setTextureByName(sTexDelete);
        mRotate.setTextureByName(sTexRotate);
        mResize.setTextureByName(sTexResize);
        mRotate.setSizePx(60f, 60f);
        mResize.setSizePx(60f, 60f);
        addChild(mCreate);
        addChild(mDelete);
        mEditing.addChild(mRotate);
        mEditing.addChild(mResize);
        mEditing.setListener(this);
    }

    public void edit(Poster target) {
        if (mTarget != null) {
            Log.i(TAG, "ooops, there is existing a target");
        }

        mIsEditing = true;
        mTarget = target;
        TextureObj texture = mTarget.getTexture();
        float mStartXPx = mTarget.getXPx();
        float mStartYPx = mTarget.getYPx();
        float mStartWidthPx  = mTarget.getWidthPx();
        float mStartHeightPx = mTarget.getHeightPx();

        if (mStartXPx == GLObject.UNDEFINE || mStartYPx == GLObject.UNDEFINE) {
            mStartXPx = 0f;
            mStartYPx = 0f;
        }
        if (mStartWidthPx == GLObject.UNDEFINE || mStartHeightPx == GLObject.UNDEFINE) {
            mStartWidthPx  = texture.getTextureWidth();
            mStartHeightPx = texture.getTextureHeight();
        }

        mEditing.setTexture(texture);
        mEditing.setXYPx(mStartXPx, mStartYPx);
        mEditing.setSizePx(mStartWidthPx, mStartHeightPx);
        mEditing.setAngle(mTarget.getAngle());

        mRotate.setXYPx(mStartWidthPx * 0.8f, 0f);
        mResize.setXYPx(mStartWidthPx * 0.8f, mStartHeightPx * 0.8f);
        addChild(0, mEditing);
    }

    public void finish() {
        mTarget.setXYPx(mEditing.getXPx(), mEditing.getYPx());
        mTarget.setSizePx(mEditing.getWidthPx(), mEditing.getHeightPx());
        mTarget.setAngle(mEditing.getAngle());
        mViewMgr.addPosterToCurrentRoom(mTarget);
        mTarget = null;
        removeChild(mEditing);
        mIsEditing = false;
    }

    public boolean isEditing() {
        return mIsEditing;
    }

    public void resize(int screenWidth, int screenHeight) {
        sWidth = screenWidth;
        sHeight = screenHeight;
        resize();
    }

    private void resize() {
        setXYPx(0, 0);
        setSizePx(sWidth, sHeight);
        mCreate.setSizePx(sButtonWidth, sButtonHeight);
        mDelete.setSizePx(sButtonWidth, sButtonHeight);
        float height = sHeight - sButtonHeight;
        mDelete.setXYPx(0, height);
        mCreate.setXYPx(sWidth - sButtonWidth, height);
    }

    @Override
    public void draw(GL10 gl) {
        if (isEditing()) {
            super.draw(gl);
        }

        return;
    }

    @Override
    protected void drawMyself(GL10 gl) {
        gl.glColor4f(1f, 1f, 1f, 0.5f);
        super.drawMyself(gl);
        gl.glColor4f(1f, 1f, 1f, 1f);
    }

    public boolean onPressEvent(PointF point, MotionEvent event) {
        int id = pointerAt(point.x, point.y);
        if (id == mResize.getId()) {
            mPressing = mResize;
        } else if (id == mRotate.getId()) {
            mPressing = mRotate;
        } else if (id == mEditing.getId()) {
            mPressing = mEditing;
        }

        mStartX = mEditing.getX();
        mStartY = mEditing.getY();
        mDeltaX = mStartX - point.x;
        mDeltaY = mStartY - point.y;

        return false;
    }

    public boolean onReleaseEvent(PointF point, MotionEvent event) {
        if (mPressing == mEditing) {
            float ratioX = mEditing.getX() / getWidth();
            float ratioY = mEditing.getY() / getHeight();
            mEditing.setXYPx(ratioX * sWidth, ratioY * sHeight);
            if(mCreate.contains(point.x, point.y)) {
                Pet pet = new Pet(mTarget);
                mViewMgr.addPet(pet);
                mTarget = null;
                removeChild(mEditing);
                mIsEditing = false;
            } else if (mDelete.contains(point.x, point.y)) {
                removeChild(mEditing);
                mTarget.clear();
                mTarget = null;
                mIsEditing = false;
            }
        } else if (mPressing == mRotate) {
        } else if (mPressing == mResize) {
            float ratioW = mEditing.getWidth() / getWidth();
            float ratioH = mEditing.getHeight() / getHeight();
            mEditing.setSizePx(ratioW * sWidth, ratioH * sHeight);
        }

        mPressing = null;
        onHoverOut(mHover);
        mHover = null;
        return false;
    }

    public boolean onDragEvent(PointF point, MotionEvent event) {
        if (mPressing == mEditing) {
            mEditing.setXY(point.x + mDeltaX, point.y + mDeltaY);
            if (mDelete.contains(point.x, point.y)) {
                if (mHover != mDelete) {
                    onHoverOut(mHover);
                    mHover = mDelete;
                    onHoverIn(mHover);
                }
            } else if (mCreate.contains(point.x, point.y)) {
                if (mHover != mCreate) {
                    onHoverOut(mHover);
                    mHover = mCreate;
                    onHoverIn(mHover);
                }
            } else if (mHover != null){
                onHoverOut(mHover);
                mHover = null;
            }
        } else if (mPressing == mRotate) {
            /* Assume vector a = (x, y), vector b = (1, 0)
             * theta = arc cos (x / length of a)
             */
            float x = point.x - mStartX;
            float y = point.y - mStartY;
            double length  = Math.hypot(x, y);
            double radians = Math.acos(x / length);
            double degrees = Math.toDegrees(radians);
            if (y < 0) {
                degrees = 360 - degrees;
            }
            mEditing.setAngle((float)degrees);
        } else if (mPressing == mResize) {
            mTmp[0] = point.x - mStartX;
            mTmp[1] = point.y - mStartY;
            mInvert.reset();
            mInvert.postRotate(-1 * mEditing.getAngle());
            mInvert.mapPoints(mTmp);
            mTmp[0] = Math.max(mTmp[0], MINIMAL_SIZE);
            mTmp[1] = Math.max(mTmp[1], MINIMAL_SIZE);
            mEditing.setSize(mTmp[0], mTmp[1]);
            mRotate.setXY(mTmp[0] * 0.8f, 0f);
            mResize.setXY(mTmp[0] * 0.8f, mTmp[1] * 0.8f);
        }

        return false;
    }

    public boolean onLongPressEvent(PointF point, MotionEvent event) {
        return false;
    }

    public void onClick(GLObject obj) {
        if (obj == mEditing) {
            finish();
        }
    }

    public void onHoverIn(GLObject obj) {
        if (obj == null) {
            return;
        }

        if (obj == mDelete) {
            mDelete.setDepth(1f);
        } else if (obj == mCreate) {
            mCreate.setDepth(1f);
        }
    }

    public void onHoverOut(GLObject obj) {
        if (obj == null) {
            return;
        }

        if (obj == mDelete) {
            mDelete.setDepth(0f);
        } else if (obj == mCreate) {
            mCreate.setDepth(0f);
        }
    }
}

