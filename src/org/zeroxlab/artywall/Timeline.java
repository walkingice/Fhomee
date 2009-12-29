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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.microedition.khronos.opengles.GL10;


/* Timeline is a singleton instance. It maintain OpenGL redraw rate */
public class Timeline {

    final String TAG="Timeline";

    public  static int ENDLESS = -1;
    private int DEFAULT_UPDATE = 5000; // 5 secs

    private static Timeline mTimeline = new Timeline();
    private static int sleepingPeriod = 35;

    private long mLastRedraw;
    private long mUpdate = DEFAULT_UPDATE;

    private GLSurfaceView mSurface;
    private RedrawThread  mThread;

    private Object mLocker;
    private LinkedList<GLAnimation> mAnimations;
    private LinkedList<GLAnimation> mUpdateTime;

    private boolean processing = false;

    private Timeline() {
    }

    synchronized static public Timeline getInstance() {
	if (mTimeline == null) {
	    mTimeline = new Timeline();
	}

	return mTimeline;
    }

    public void addAnimation(GLAnimation animation) {
	animation.setStart(SystemClock.uptimeMillis());
	synchronized(mLocker) {
	    mUpdateTime.add(animation);
	    int position = linearSearchEndTime(animation.getEndTime());
	    mAnimations.add(position, animation);
	    updateFrequency(animation.getUpdateTime());
	}
    }

    public void monitor(GLSurfaceView surface) {
	mSurface = surface;
	mLastRedraw = SystemClock.uptimeMillis();
	mThread = new RedrawThread();
	mThread.start();

	mLocker = new Object();
	mAnimations = new LinkedList<GLAnimation>();
	mUpdateTime = new LinkedList<GLAnimation>();
	//UpdateComparator update = new UpdateComparator();
	//mUpdateTime = new TreeSet<GLAnimation>(update);
    }

    /* Find out the position for new Animation by EndTime*/
    private int linearSearchEndTime(long endTime) {
	long end;
	int counter = 0;
	for (counter = mAnimations.size() - 1; counter > 0;counter--) {
	    end = mAnimations.get(counter).getEndTime();
	    if (end > endTime) {
		return counter;
	    }
	}
	return 0;
    }

    private boolean clearExpiredAnimation(long now) {
	if (mAnimations.isEmpty()) {
	    return false;
	}
	boolean redraw = false;
	long minimal = DEFAULT_UPDATE;

	boolean keepWalking = true;
	while (keepWalking) {
	    GLAnimation ani = mAnimations.getFirst();
	    if (ani.isFinish(now)) {
		ani.complete();
		redraw = true;

		synchronized(mLocker) {
		    mAnimations.remove(ani);
		    mUpdateTime.remove(ani);
		    updateFrequency(ani.getUpdateTime());
		}

		if(mAnimations.isEmpty()) {
		    keepWalking = false;
		}
	    } else {
		keepWalking = false;
	    }
	}

	return redraw;
    }

    private void updateFrequency(long oldFrequency) {
	/*
	if (oldFrequency == mUpdate) {
	    mUpdate = minimalFrequency();
	} else if (oldFrequency < mUpdate) {
	    mUpdate = newFrequency;
	} else {
	    mUpdate = DEFAULT_UPDATE;
	}
	*/

	mUpdate = minimalFrequency();
	return;
    }

    private long minimalFrequency() {
	long minimal = DEFAULT_UPDATE;
	Iterator<GLAnimation> iterator = mUpdateTime.iterator();
	while (iterator.hasNext()) {
	    GLAnimation ani = iterator.next();
	    long update = ani.getUpdateTime();

	    if(update < minimal) {
		minimal = update;
	    }
	}

	return minimal;
    }

    private void processRedraw() {
	/* Making sure the redraw thread will not keep asking redraw*/
	if (processing) {
	    return;
	}
	processing = true;

	boolean haveToRedraw = false;
	long now = SystemClock.uptimeMillis();
	GLAnimation.setNow(now);
	haveToRedraw = clearExpiredAnimation(now);
	if (mAnimations.isEmpty()) {
	    //No animation, do nothing
	} else {
	    // FIXME: the time may be reset. maybe use Math.abs?
	    if (mLastRedraw + mUpdate < now) {
		haveToRedraw = true;
	    }
	}

	if (haveToRedraw) {
	    mSurface.requestRender();
	    mLastRedraw = now;
	}

	processing = false;
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
		    sleep(sleepingPeriod);
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

    private class AnimationComparator implements Comparator<GLAnimation> {
	private final int GREATER = 1;
	private final int EQUAL   = 0;
	private final int LESS    = -1;

	public int compare(GLAnimation ani1, GLAnimation ani2) {
	    long end1 = ani1.getEndTime();
	    long end2 = ani2.getEndTime();
	    if(ani1.equals(ani2)) {
		return EQUAL;
	    }
	    if(end1 >= end2) {
		return GREATER;
	    } else if (end1 < end2) {
		return LESS;
	    } else {
		return EQUAL;
	    }
	}

	public boolean equals(Object obj) {
	    // FIXME: why do I need this?
	    return false;
	}
    }

// Maybe we need it in the future
//
//    private class UpdateComparator implements Comparator<GLAnimation> {
//
//	/* do not return 0, we may add two similar Animation into TreeSet.
//	   You cannot add Animation into TreeSet if compare() return 0 */
//	public int compare(GLAnimation ani1, GLAnimation ani2) {
//	    long update1 = ani1.getUpdateTime();
//	    long update2 = ani2.getUpdateTime();
//	    if (ani1.mId == ani2.mId) {
//		return 0; // equal
//	    }
//	    if(update1 >= update2) {
//		return 1;
//	    } else {
//		return -1;
//	    }
//	}
//
//	public boolean equals(Object obj) {
//	    // FIXME: why do I need this?
//	    return false;
//	}
//    }
}

