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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class GestureInterpreter {

    final String TAG="GestureInterpreter";
    private static GestureInterpreter mGI = new GestureInterpreter();
    private final static int DEFAULT_WIDTH  = 320;
    private final static int DEFAULT_HEIGHT = 480;
    private int mScreenWidth;
    private int mScreenHeight;

    public final static int DRAG_THRESHOLD = 15; // 5px
    public boolean mIsDragging = false;
    public boolean mIsHDrag    = false;

    public final static long LONGCLICK_THRESHOLD = 1000;
    public boolean mIsLongClick = false;

    final private float triggerHorizontal = 0.25f;
    final private float triggerVertical   = 0.75f;
    private float triggerEnableLeft  = 0;
    private float triggerEnableRight = 100;
    private float triggerShiftTop    = 400;
    private float triggerShiftBottom = 480;
    public Rect triggerArea;
    public Rect scaleArea;
    public Rect shiftArea;

    private int mWhere;
    final static private int TOPLEVEL = 0;


    /* Only interpret the gesture  */
    private int mNow;
    public final static int RELEASE      = 1;  // Normal state
    public final static int PRESS        = 2;  // Start pressing
    public final static int PRESSING     = 3; // Keep pressing
    public final static int LONGPRESSING = 4; // Press for a while
    public final static int HDRAGGING    = 5; // Horizontal dragging
    public final static int DRAGGING     = 6; // Dragging except horizontal

    public int mNowX = -1;
    public int mNowY = -1;
    public int mPressX = -1;
    public int mPressY = -1;
    public int mReleaseX = -1;
    public int mReleaseY = -1;

    public long mPressTime   = 0;
    public long mReleaseTime = 0;

    private GestureInterpreter() {
	updateScreenSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	mWhere = TOPLEVEL;
    }

    synchronized static public GestureInterpreter getInstance() {
	if (mGI == null) {
	    mGI = new GestureInterpreter();
	}

	return mGI;
    }

    public int processMotionEvent(MotionEvent event) {
	if(mWhere == TOPLEVEL) {
	    mNow = eventAtToplevel(event);
	} else {
	    return RELEASE;
	}

	Log.i(TAG,"Now is:"+mNow+"\tDrag:"+mIsDragging+"\tHorizontal:"+mIsHDrag+"\tLong:"+mIsLongClick);
	return mNow;
    }

    public int getDeltaX() {
	return mNowX - mPressX;
    }

    public int getDeltaY() {
	return mNowY - mPressY;
    }

    public long pressTime() {
	return mReleaseTime - mPressTime;
    }

    private int eventAtToplevel(MotionEvent event) {
	int action = event.getAction();
	int x = (int) event.getX();
	int y = (int) event.getY();
	mNowX = x;
	mNowY = y;
	int now = mNow;

	switch (action) {
	    case MotionEvent.ACTION_UP:
		mReleaseTime = SystemClock.uptimeMillis();
		mReleaseX = x;
		mReleaseY = y;
		mPressX   = -1;
		mPressY   = -1;

		/* Reset flag */
		mIsDragging = false;
		mIsLongClick = false;

		now = RELEASE;
		break;
	    case MotionEvent.ACTION_DOWN:
		mPressTime = SystemClock.uptimeMillis();
		mPressX = x;
		mPressY = y;
		mReleaseX = -1;
		mReleaseY = -1;
		now = PRESS;

		break;
	    case MotionEvent.ACTION_MOVE:
		// Decide the state with priority
		// Dragging is the first, and then is HDragging, Long pressing
		long time = SystemClock.uptimeMillis();
		now = PRESSING;
		if (time - mPressTime > LONGCLICK_THRESHOLD) {
		    mIsLongClick = true;
		    now = LONGPRESSING;
		}

		if (!mIsDragging) {
		    if (Math.abs(y - mPressY) > DRAG_THRESHOLD) {
			mIsDragging = true;
			mIsHDrag    = false;
			now = DRAGGING;
		    } else if (Math.abs(x - mPressX) > DRAG_THRESHOLD) {
			mIsDragging = true;
			mIsHDrag    = true;
			now = HDRAGGING;
		    }
		}
		break;
	    default:
		now = RELEASE;
	}

	return now;
    }

    public void updateScreenSize(int width, int height) {
	mScreenWidth  = width;
	mScreenHeight = height;
	triggerEnableLeft = 0;
	triggerEnableRight = (int)(width  * triggerHorizontal);
	triggerShiftTop    = (int)(height * triggerVertical);
	triggerShiftBottom = height;
	triggerArea = new Rect(0, 0, 100, 100);
	scaleArea   = new Rect(0, 100, 100, 350);
	shiftArea   = new Rect(0,350, width, height);
    }
}

