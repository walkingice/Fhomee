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

public class GestureManager {

    final String TAG="GestureManager";
    private static GestureManager mGestureMgr = new GestureManager();
    private final static int DEFAULT_WIDTH  = 320;
    private final static int DEFAULT_HEIGHT = 480;
    private int mScreenWidth;
    private int mScreenHeight;

    public final static int DRAG_H_THRESHOLD = 15; // 15px
    public final static int DRAG_V_THRESHOLD = 20; // 20px
    public boolean mIsDragging = false;
    public boolean mIsHDrag    = false;

    public final static long LONGCLICK_THRESHOLD = 1000;
    public boolean mIsLongClick = false;

    public boolean mSnapToNext = false;
    public boolean mSnapToPrev = false;

    private int mSwitchLeft;
    private int mSwitchRight;
    private int mSwitchTop;
    private int mSwitchBottom;
    public Rect mMiniSwitchOn;
    public Rect mMiniSwitchOff;
    public Rect mDockArea;
    public boolean mBumpDock    = false;
    public boolean mPressSwitch = false;
    public boolean mMiniMode    = false;
    public boolean mModeChange  = false;

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

    private GestureManager() {
	updateScreenSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	mWhere = TOPLEVEL;
    }

    synchronized static public GestureManager getInstance() {
	if (mGestureMgr == null) {
	    mGestureMgr = new GestureManager();
	}

	return mGestureMgr;
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

    private void updateSnappingState() {
	int deltaX = getDeltaX();
	int threshold = (int) (mScreenWidth / 2);
	if (Math.abs(deltaX) < threshold) {
	    mSnapToNext = false;
	    mSnapToPrev = false;
	} else if (deltaX > 0) {
	    mSnapToNext = false;
	    mSnapToPrev = true;
	} else {
	    mSnapToNext = true;
	    mSnapToPrev = false;
	}
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
		updateSnappingState();
		mPressSwitch = false;

		mModeChange = false;

		now = RELEASE;
		break;
	    case MotionEvent.ACTION_DOWN:
		mPressTime = SystemClock.uptimeMillis();
		mPressX = x;
		mPressY = y;
		mReleaseX = -1;
		mReleaseY = -1;

		mPressSwitch = mMiniSwitchOn.contains(x, y) || mMiniSwitchOff.contains(x, y);
		mMiniMode    = mMiniSwitchOn.contains(x, y);

		mBumpDock = mDockArea.contains(x, y);

		/* Reset flag */
		mIsDragging = false;
		mIsLongClick = false;
		mModeChange  = false;

		now = PRESS;
		break;
	    case MotionEvent.ACTION_MOVE:
		// Decide the state with priority
		// Dragging is the first, and then is HDragging, Long pressing
		if (!mIsDragging) {
		    now = PRESSING;
		    long time = SystemClock.uptimeMillis();
		    if (time - mPressTime > LONGCLICK_THRESHOLD) {
			mIsLongClick = true;
			now = LONGPRESSING;
		    }

		    if (Math.abs(y - mPressY) > DRAG_V_THRESHOLD) {
			mIsDragging = true;
			mIsHDrag    = false;
			now = DRAGGING;
		    } else if (Math.abs(x - mPressX) > DRAG_H_THRESHOLD) {
			mIsDragging = true;
			mIsHDrag    = true;
			now = HDRAGGING;
		    }
		} else if (!mIsHDrag && mPressSwitch) {
		    mModeChange = ((mMiniMode && mMiniSwitchOff.contains(x, y))
			    ||(!mMiniMode && mMiniSwitchOn.contains(x, y)));
		    if (mMiniSwitchOn.contains(x, y)) {
			mMiniMode = true;
		    } else if (mMiniSwitchOff.contains(x, y)) {
			mMiniMode = false;
		    }
		} else if (mIsDragging) {
		    mBumpDock = mDockArea.contains(x, y);
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

	int switchHeight = (int) (height * 0.25f);

	mSwitchLeft  = 0;
	mSwitchRight = (int) (width  * 0.33f);
	mSwitchTop   = (int) (height * 0.4f);
	mSwitchBottom = mSwitchTop + switchHeight;

	mMiniSwitchOff  = new Rect(mSwitchLeft, mSwitchTop, mSwitchRight, mSwitchBottom);
	mMiniSwitchOn = new Rect(mMiniSwitchOff);
	mMiniSwitchOn.offsetTo(mSwitchLeft, mSwitchBottom);

	mDockArea = new Rect(0,(int)(height * 0.8f), mScreenWidth, mScreenHeight);
    }
}

