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

public class GLAnimation {

    final static String TAG="GLAnimation";
    public final static int CONTINUOUS = -1;

    private GLAnimationListener mListener;
    private long mStart  = 0;
    private long mEnd    = 0;
    private long mLife   = 0;
    private int  mRepeat = 1;

    GLAnimation(long howLong) {
	this(howLong, 1);
    }

    GLAnimation(long howLong, int repeatTimes) {
	mLife   = howLong;
	mRepeat = repeatTimes;
    }

    public void setListener(GLAnimationListener listener) {
	mListener = listener;
    }

    public void setStart(long start) {
	mStart = start;
	mEnd   = mStart + mLife;
    }

    public boolean isFinish(long now) {
	if (now > mEnd) {
	    return true;
	}
	return false;
    }

    public void callback() {
	if (mListener != null) {
	    mListener.onAnimationEnd();
	}
    }

    interface GLAnimationListener {
	public void onAnimationEnd();
    }
}

