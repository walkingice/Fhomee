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

    protected RectF  mRect;
    protected RectF  mCoverage;
    protected RectF  mTmpRect;
    protected float  mAngle = 0f;
    protected Matrix mTranslate;
    protected Matrix mInvert;

    protected float mPts[];

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
        mPts = new float[2];
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

    public boolean contains(float x, float y) {
        mPts[0] = x;
        mPts[1] = y;
        mInvert.mapPoints(mPts);
        return mRect.contains(mPts[0], mPts[1]);
    }

    public Matrix getTranslateMatrix() {
        return mTranslate;
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

}

