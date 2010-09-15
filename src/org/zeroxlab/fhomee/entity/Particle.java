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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

/**
 * Particle is an Entity with Position
 */
public class Particle extends Entity {

    public final String TAG = "Particle";

    protected PointF mPosition;
    protected float mDepth = 0f;

    /* Stores the position of this Particle
     * These data represent in Pixel-base of Screen.
     */
    protected float mXPx = UNDEFINE;
    protected float mYPx = UNDEFINE;

    public Particle() {
        this(0f, 0f);
    }

    public Particle(float x, float y) {
        super();
        mPosition = new PointF(x, y);
    }

    public float getX() {
        return mPosition.x;
    }

    public float getY() {
        return mPosition.y;
    }

    public void setXY(float x, float y) {
        mPosition.x = x;
        mPosition.y = y;
    }

    public void setDepth(float depth) {
        mDepth = depth;
    }

    public void setXYPx(float xPx, float yPx) {
        mXPx = xPx;
        mYPx = yPx;
    }

    public float getXPx() {
        return mXPx;
    }

    public float getYPx() {
        return mYPx;
    }
}

