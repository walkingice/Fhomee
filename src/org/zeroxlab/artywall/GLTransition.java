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
import android.graphics.Bitmap;

import javax.microedition.khronos.opengles.GL10;

public class GLTransition{

    final static String TAG="GLTransition";
    final static long DEFAULT_TIME = 500; // 500 ms
    protected static long Now = 0;

    protected long mStart  = 0;
    protected long mTotal  = 0;

    protected GLObject mObject;

    protected String[] mName;
    protected long[]   mTime;
    protected int[]    mTextures;

    GLTransition(String[] name, long[] time) {
	int length = name.length;
	mName = new String[length];
	mTime = new long[length];
	mTextures = new int[length];

	for (int i = 0; i < length; i++) {
	    mName[i] = name[i];
	    mTime[i] = DEFAULT_TIME;
	}

	int time_length = time.length;
	if (time_length > mTime.length) {
	    time_length = mTime.length;
	}

	mTotal = 0;
	for (int i = 0; i < time_length; i++) {
	    mTime[i] = time[i];
	    mTotal += mTime[i];
	}
    }

    public long getUpdateRate() {
	long rate = mTime[0];
	for (int i = 0; i < mTime.length; i++) {
	    if (rate > mTime[i]) {
		rate = mTime[i];
	    }
	}

	return rate;
    }

    public int getNowTextureID() {
	if (mStart + mTotal < Now) {
	    mStart = Now;
	}
	long period = Now - mStart;
	for (int i = 0; i < mTime.length; i++) {
	    period = period - mTime[i];
	    if(period <= 0) {
		//  we are at mTime[i] < Now < mTime[i+1]
		return mTextures[i];
	    }
	}

	return mTextures[0];
    }

    public void generateTextures(GL10 gl, ResourcesManager resM, TextureManager texM) {
	Bitmap bitmap;
	for (int i = 0; i < mName.length; i++) {
	    bitmap = resM.getBitmapByName(mName[i]);
	    mTextures[i] = texM.generateOneTexture(gl, bitmap, mName[i]);
	}
    }

    public static void setNow(long now) {
	Now = now;
    }
}
