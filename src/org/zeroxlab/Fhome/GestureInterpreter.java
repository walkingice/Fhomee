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
    private int mScreenWidth;
    private int mScreenHeight;

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

    private int mNow;
    final static int NOTHING       = 0;
    final static int NORMAL        = 1;
    final static int SCALING       = 2;
    final static int SHIFTING      = 4;
    final static int MOVE_NEXT     = 5;
    final static int MOVE_PREV     = 6;
    final static int MOVE_ORIG     = 7;

    public int mNowX = -1;
    public int mNowY = -1;
    public int mPressX = -1;
    public int mPressY = -1;
    public int mReleaseX = -1;
    public int mReleaseY = -1;

    public long mPressTime   = 0;
    public long mReleaseTime = 0;

    GestureInterpreter(int width, int height) {
	updateScreenSize(width, height);
	mWhere = TOPLEVEL;
    }

    public int processMotionEvent(MotionEvent event) {
	if(mWhere == TOPLEVEL) {
	    mNow = eventAtToplevel(event);
	} else {
	    return NOTHING;
	}

	return mNow;
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
		mReleaseX = x;
		mReleaseY = y;
		mReleaseTime = SystemClock.uptimeMillis();

		if (now == NORMAL) {
		    int deltaX = mPressX - mReleaseX;
		    int delteY = mPressY - mReleaseY;
		    int threshold = (int) (mScreenWidth / 2);
		    if (Math.abs(deltaX) > threshold) {
			if (deltaX > 0) {
			    Log.i(TAG, "Move to next room");
			    now = MOVE_NEXT;
			} else {
			    Log.i(TAG, "Move to previous room");
			    now = MOVE_PREV;
			}
		    } else {
			Log.i(TAG, "back to original room");
			now = MOVE_ORIG;
		    }
		} else {
		    now = NOTHING;
		}

		mPressX = -1;
		mPressY = -1;
		break;
	    case MotionEvent.ACTION_DOWN:
		mPressTime = SystemClock.uptimeMillis();
		mPressX = x;
		mPressY = y;
		mReleaseX = -1;
		mReleaseY = -1;
		if (triggerArea.contains(x,y)) {
		    now = SCALING;
		} else {
		    now = NORMAL;
		}
		break;
	    case MotionEvent.ACTION_MOVE:
		if (now == SCALING && shiftArea.contains(x,y)) {
		    now = SHIFTING;
		} else if (now == SHIFTING && shiftArea.contains(x,y)) {
		    now = SHIFTING;
		} else if (now == SHIFTING || now == SCALING) {
		    // back to Scaling
		    now = SCALING;
		} else if (now == NORMAL) {
		    // Normal dragging
		} else {
		    now = NOTHING;
		}
		break;
	    default:
		now = NOTHING;
	}

	return now;
    }

    private void updateScreenSize(int width, int height) {
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

