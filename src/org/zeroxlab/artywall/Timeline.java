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
import android.opengl.GLSurfaceView;
import android.os.SystemClock;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.Iterator;
import javax.microedition.khronos.opengles.GL10;


/* Timeline is a singleton instance. It maintain OpenGL redraw rate */
public class Timeline {

    final String TAG="Timeline";

    public  static int ENDLESS = -1;

    private static Timeline mTimeline = new Timeline();
    private static Context mContext;
    private static int millisSecond = 35;

    private GLSurfaceView mSurface;
    private RedrawThread  mThread;
    private ArrayList<GLAnimation> mAnimations;

    private Timeline() {
    }

    synchronized static public Timeline getInstance(Context context) {
	if (mTimeline == null) {
	    mTimeline = new Timeline();
	}

	mContext = context;
	return mTimeline;
    }

    public void addAnimation(GLAnimation animation) {
	animation.setStart(SystemClock.uptimeMillis());
	mAnimations.add(animation);
    }

    public void monitor(GLSurfaceView surface) {
	mSurface = surface;
	mAnimations = new ArrayList<GLAnimation>();
	mThread = new RedrawThread();
	mThread.start();
    }

    private void clearExpiredAnimation() {
	if (mAnimations.isEmpty()) {
	    return;
	}
	long now = SystemClock.uptimeMillis();
	Iterator<GLAnimation> iterator = mAnimations.iterator();

	while (iterator.hasNext()) {
	    GLAnimation ani = iterator.next();
	    if(ani.isFinish(now)) {
		ani.callback();
		iterator.remove();
	    }
	}
    }

    private void processRedraw() {
	boolean haveToRedraw = false;
	clearExpiredAnimation();
	if (!mAnimations.isEmpty()) {
	    haveToRedraw = true;
	}

	if (haveToRedraw) {
	    mSurface.requestRender();
	}
    }

    private class RedrawThread extends Thread {
	private boolean keepRunning = true;
	RedrawThread() {
	}

	public void end() {
	    keepRunning = false;
	}

	public void run() {
	    while (keepRunning) {
		try {
		    sleep(millisSecond);
		    processRedraw();
		} catch (InterruptedException exception) {
		    Log.i(TAG,"ooops, RedrawThread was interrupted!");
		    exception.printStackTrace();
		}
	    }

	    // supposed you will never be here
	    Log.i(TAG," thread stopped. Anything go wrong?");
	}
    }
}

