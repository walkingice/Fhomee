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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
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
    final static int SCALING       = 2;
    final static int SHIFTING      = 4;

    public int mPressX = -1;
    public int mPressY = -1;
    public int mReleaseX = -1;
    public int mReleaseY = -1;

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

	String out="";
	if(mNow == NOTHING) {
	    out = "nothing";
	} else if (mNow == TRIGGER_SCALE) {
	    out = "trigger scale";
	} else if (mNow == SCALING) {
	    out = "scaling";
	} else if (mNow == TRIGGER_SHIFT) {
	    out = "trigger shift";
	} else if (mNow == SHIFTING) {
	    out = "shifting";
	}
	Log.i(TAG,"State is "+out+" X:"+event.getX()+" Y:"+event.getY());
	return mNow;
    }

    private int eventAtToplevel(MotionEvent event) {
	int action = event.getAction();
	int x = (int) event.getX();
	int y = (int) event.getY();
	int now = mNow;

	switch (action) {
	    case MotionEvent.ACTION_UP:
		mPressX = -1;
		mPressY = -1;
		mReleaseX = x;
		mReleaseY = y;
		now = NOTHING;
		break;
	    case MotionEvent.ACTION_DOWN:
		mPressX = x;
		mPressY = y;
		mReleaseX = -1;
		mReleaseY = -1;
		if (triggerArea.contains(x,y)) {
		    now = SCALING;
		} else {
		    now = NOTHING;
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

