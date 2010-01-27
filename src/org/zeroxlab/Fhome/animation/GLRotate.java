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

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

public class GLRotate extends GLAnimation{

    final static String TAG="GLRotate";

    public static int CLOCKWISE = 1;
    public static int COUNTERCLOCKWISE = 2;

    private float mStartAngle = 0f;
    private float mEndAngle   = 0f;
    private float mGapAngle   = 0f;

    GLRotate(long howlong, float endAngle, int direction) {
	super(howlong, 10);
	if (direction == CLOCKWISE) {
	    mEndAngle = endAngle;
	} else {
	    mEndAngle = -1 * endAngle;
	}
    }

    public void bindGLObject(GLObject object) {
	super.bindGLObject(object);
	mStartAngle = object.getAngle();
	mGapAngle   = mEndAngle - mStartAngle;
    }

    public void complete() {
	/* if this animation is interrupted, mObject becomes null */
	if (mObject != null) {
	    mObject.setAngle(mEndAngle);
	}
	super.complete();
    }

    public boolean applyAnimation(GL10 gl) {
	boolean glObjectDrawItself = true;
	float now  = GLAnimation.mNow;
	float life = mLife;
	float elapse = now - mStart;
	float ratio = elapse / life;
	float angle = mGapAngle * ratio + mStartAngle;
	mObject.setAngle(angle);
	return glObjectDrawItself;
    }
}

