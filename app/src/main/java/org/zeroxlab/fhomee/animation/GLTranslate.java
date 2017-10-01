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


package org.zeroxlab.fhomee.animation;

import android.util.Log;

import org.zeroxlab.fhomee.GLAnimation;
import org.zeroxlab.fhomee.GLObject;

import javax.microedition.khronos.opengles.GL10;

public class GLTranslate extends GLAnimation {

    final static String TAG = "GLTranslate";

    private float mStartX = 0f;
    private float mStartY = 0f;
    private float mEndX = 0f;
    private float mEndY = 0f;
    private float mGapX = 0f;
    private float mGapY = 0f;

    private float mStartAngle = 0f;
    private float mEndAngle = 0f;
    private float mIncludedAngle = 0f;

    public GLTranslate(long howlong, float endX, float endY) {
        super(howlong, 10);
        setDestination(endX, endY);
    }

    public void setAngle(float endAngle) {
        float startAngle = 0f;
        if (mObject != null) {
            startAngle = mObject.getAngle();
        }

        setAngle(startAngle, endAngle);
    }

    public void setAngle(float startAngle, float endAngle) {
        mStartAngle = startAngle;
        mEndAngle = endAngle;
        mIncludedAngle = mEndAngle - mStartAngle;
    }

    public void bindGLObject(GLObject object) {
        super.bindGLObject(object);
        mStartX = mObject.getX();
        mStartY = mObject.getY();
        setDestination(mEndX, mEndY);
        float angle = mObject.getAngle();
        Log.i(TAG, "Reset angle while binding object:" + mObject.getId());
        setAngle(mObject.getAngle(), mObject.getAngle());
    }

    public void setDestination(float destX, float destY) {
        mEndX = destX;
        mEndY = destY;
        mGapX = mEndX - mStartX;
        mGapY = mEndY - mStartY;
    }

    public void complete() {
    /* if this animation is interrupted, mObject becomes null */
        if (mObject != null) {
            mObject.setXY(mEndX, mEndY);
            mObject.setAngle(mEndAngle);
        }
        super.complete();
    }

    public boolean applyAnimation(GL10 gl) {
        boolean glObjectDrawItself = true;
        long elapse = GLAnimation.mNow - mStart;
        float x = ((mGapX * elapse) / mLife) + mStartX;
        float y = ((mGapY * elapse) / mLife) + mStartY;
        float angle = ((mIncludedAngle * elapse) / mLife) + mStartAngle;
        mObject.setXY(x, y);
        mObject.setAngle(angle);
        return glObjectDrawItself;
    }
}

