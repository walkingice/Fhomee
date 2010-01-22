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

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

public class GLTranslate extends GLAnimation{

    final static String TAG="GLTranslate";

    private float mStartX = 0f;
    private float mStartY = 0f;
    private float mEndX = 0f;
    private float mEndY = 0f;
    private float mGapX = 0f;
    private float mGapY = 0f;

    GLTranslate(long howlong, float endX, float endY) {
	super(howlong, 10);
	mEndX = endX;
	mEndY = endY;
    }

    public void bindGLObject(GLObject object) {
	super.bindGLObject(object);
	mStartX = mObject.getX();
	mStartY = mObject.getY();
	mGapX = mEndX - mStartX;
	mGapY = mEndY - mStartY;
    }

    public void complete() {
	/* if this animation is interrupted, mObject becomes null */
	if (mObject != null) {
	    mObject.setXY(mEndX, mEndY);
	}
	super.complete();
    }

    public boolean applyAnimation(GL10 gl) {
	boolean glObjectDrawItself = true;
	float now  = GLAnimation.mNow;
	float life = mLife;
	float elapse = now - mStart;
	float ratio = elapse / life;
	float x = mGapX * ratio + mStartX;
	float y = mGapY * ratio + mStartY;
	mObject.setXY(x, y);
	return glObjectDrawItself;
    }
}

