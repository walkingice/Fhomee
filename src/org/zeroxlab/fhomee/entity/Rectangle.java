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


package org.zeroxlab.fhomee.entity;

import org.zeroxlab.fhomee.time.GLAnimation;
import org.zeroxlab.fhomee.*;

import android.util.Log;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

/**
 * Rectangle is an Entity with Position and Width, Height
 */
public class Rectangle extends Particle {

    public final String TAG = "Rectangle";

    protected Rectangle mParent;
    protected RectF  mRect;
    protected RectF  mCoverage;
    protected RectF  mTmpRect;
    protected float  mAngle = 0f;
    protected Matrix mTranslate;
    protected Matrix mInvert;
    protected Matrix mAbsTranslate;

    protected float mPts[];

    float mViewport[];
    protected boolean inViewport = true;

    /* Stores the size of this Rectangle
     * These data represent in Pixel-base of Screen.
     */
    protected float mWidthPx  = UNDEFINE;
    protected float mHeightPx = UNDEFINE;


    public Rectangle(float x, float y, float width, float height) {
        super(x, y);
        mRect = new RectF(0, 0, width, height);
        mCoverage = new RectF(mRect);
        mTmpRect  = new RectF();

        mTranslate = new Matrix();
        mInvert = new Matrix();
        mAbsTranslate = new Matrix();
        mPts = new float[2];
        mViewport = new float[8];

    }

    public void setParent(Rectangle parent) {
        mParent = parent;
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

    public float getWidthPx() {
        return mWidthPx;
    }

    public float getHeightPx() {
        return mHeightPx;
    }

    public float getAngle() {
        return mAngle;
    }

    public void setXY(float x, float y) {
        super.setXY(x, y);
        resetInvertMatrix();
        resetTranslateMatrix();
    }

    public void setAngle(float angle) {
        mAngle = angle % 360;
        resetInvertMatrix();
        resetTranslateMatrix();
    }

    public void setSize(float width, float height) {
        mRect.set(0, 0, width, height);
    }

    public void setSizePx(float widthPx, float heightPx) {
        mWidthPx  = widthPx;
        mHeightPx = heightPx;
    }

    public boolean measure(float ratioX, float ratioY) {
        boolean updated = false;

        if (mXPx != UNDEFINE && mYPx != UNDEFINE) {
            setXY(ratioX * mXPx, ratioY * mYPx);
            updated = true;
        }

        if (mWidthPx != UNDEFINE && mHeightPx != UNDEFINE) {
            setSize(ratioX * mWidthPx, ratioY * mHeightPx);
            updated = true;
        }

        updated = updated || measureChildren(ratioX, ratioY);

        return updated;
    }

    /* Rectangle group overwrite this method */
    protected boolean measureChildren(float ratioX, float ratioY) {
        return false;
    }

    public boolean contains(float x, float y) {
        mPts[0] = x;
        mPts[1] = y;
        mInvert.mapPoints(mPts);
        return mRect.contains(mPts[0], mPts[1]);
    }

    public Matrix getAbsTranslateMatrix() {
        return mAbsTranslate;
    }

    public Matrix getTranslateMatrix() {
        return mTranslate;
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

        boolean inViewport = RectF.intersects(mCoverage, mTmpRect);

        if (inViewport) {
            checkChildrenViewport(mViewport);
        }

        return;
    }

    protected void checkChildrenViewport(float[] viewport) {
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

        id = pointerAtChildren(mPts[0], mPts[1]);

        if (id == -1 && mRect.contains(mPts[0], mPts[1])) {
            id = mID;
        }

        return id;
    }

    protected int pointerAtChildren(float convertedX, float convertedY) {
        return -1;
    }

    public void updateCoverage() {
        mCoverage.set(mRect);

        if (mParent != null) {
            mParent.updateCoverage();
        }

        Camera.sRefresh = true;
    }

    protected void updateChildrenCoverage() {
    }

    public void getCoverage(RectF dst) {
        dst.set(mCoverage);
    }

    public void getTranslatedCoverage(RectF dst) {
        mTranslate.mapRect(dst, mCoverage);
    }

    /* A Rectangle may be Translated or Rotated from Origin.
     * mInvert holds a reverse matrix that could invert a point.
     */
    private void resetInvertMatrix() {
        mInvert.reset();
        mInvert.postTranslate(-1 * mPosition.x, -1 * mPosition.y);
        mInvert.postRotate(-1 * mAngle);
    }

    protected void resetTranslateMatrix() {
        mTranslate.reset();
        mTranslate.postTranslate(mPosition.x, mPosition.y);
        mTranslate.postRotate(mAngle);
    }

    protected void resetAbsTranslateMatrix() {
        mAbsTranslate.reset();
        if (mParent != null) {
            mAbsTranslate.preConcat(mParent.getAbsTranslateMatrix());
        }
        mAbsTranslate.preTranslate(mPosition.x, mPosition.y);
        mAbsTranslate.preRotate(mAngle);

        resetChildrenAbsTranslateMatrix();
    }

    protected void resetChildrenAbsTranslateMatrix() {
    }

    /* This Rectangle locate at a position which relate to its parent
       Move the ModelView Matrix to the position */
    protected void moveModelViewToPosition(GL10 gl) {
        gl.glTranslatef(mPosition.x, mPosition.y, mDepth);
        gl.glRotatef(mAngle, 0, 0, 1f);
    }

    public void draw(GL10 gl) {
        moveModelViewToPosition(gl);
        drawMyself(gl);
        drawChildren(gl);
    }

    protected boolean applyAnimation(GL10 gl) {
        return true;
    }

    protected void drawMyself(GL10 gl) {
    }

    protected void drawChildren(GL10 gl) {
    }
}

