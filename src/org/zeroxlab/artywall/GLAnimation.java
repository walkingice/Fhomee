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

public class GLAnimation {

    final static String TAG="GLAnimation";
    public final static int CONTINUOUS = -1;
    protected static long mNow = 0;

    private GLAnimationListener mListener;
    protected long mStart  = 0;
    protected long mEnd    = 0;
    protected long mLife   = 0;
    protected long mUpdate = 0;
    protected int  mRepeat = 1;

    protected GLObject mObject;

    GLAnimation(long howLong) {
	this(howLong,500, 1);
    }

    GLAnimation(long howLong,long update, int repeatTimes) {
	mLife   = howLong;
	mUpdate = update;
	mRepeat = repeatTimes;
    }

    public static void setNow(long now) {
	mNow = now;
    }

    public void setListener(GLAnimationListener listener) {
	mListener = listener;
    }

    public void setStart(long start) {
	mStart = start;
	mEnd   = mStart + mLife;
    }

    public long getUpdateTime() {
	return mUpdate;
    }

    public long getEndTime() {
	return mEnd;
    }

    public boolean isFinish(long now) {
	if (now > mEnd) {
	    return true;
	}
	return false;
    }

    public void bindGLObject(GLObject object) {
	if (mObject != null) {
	    unbindGLObject();
	}

	mObject = object;
	mObject.setAnimation(this);
    }

    public void unbindGLObject() {
	if (mObject != null) {
	    mObject.clearAnimation();
	}

	mObject = null;
    }

    public void callback() {
	if (mListener != null) {
	    mListener.onAnimationEnd();
	}
    }

    public boolean applyAnimation(GL10 gl) {
	// do nothing yet, subclass overwrite me
	boolean glObjectDrawItself = true;
	return glObjectDrawItself;
    }

    interface GLAnimationListener {
	public void onAnimationEnd();
    }
}

