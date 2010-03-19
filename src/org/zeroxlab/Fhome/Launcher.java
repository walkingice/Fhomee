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
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import android.graphics.*;

/* Arty Wall (Temporary Name) is the Launcher of 0xLab for Android.  *\
\* This Launcher is disigned by CMLab of National Taiwan University. */

public class Launcher extends Activity {

    final String TAG="Launcher";
    TotalScreen mScreen;
    GestureManager mGestureMgr;
    public static int mDefaultWidth  = 320;
    public static int mDefaultHeight = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	LinearLayout layout = new LinearLayout(this);
	mScreen = new TotalScreen(this);
	mScreen.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	mGestureMgr = GestureManager.getInstance();
	layout.addView(mScreen);
	setContentView(layout);
    }

    @Override
    protected void onPause() {
	super.onPause();
	mScreen.onPause();
    }

    @Override
    protected void onResume() {
	super.onResume();
	mScreen.onResume();
    }

    class TotalScreen extends GLSurfaceView implements View.OnClickListener {
	ViewManager mViewManager;
	TotalScreen(Context context) {
	    super(context);
	    mViewManager = new ViewManager(context, this);
	    setOnClickListener(this);
	}

	public void onPause() {
	    super.onPause();
	}

	public void onResume() {
	    super.onResume();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    int result = mGestureMgr.processMotionEvent(event);
	    int x = mGestureMgr.mNowX;
	    int y = mGestureMgr.mNowY;
	    int action = event.getAction();
	    if (action == MotionEvent.ACTION_MOVE && mGestureMgr.mIsDragging) {
		mViewManager.onMove(x, y);
	    } else if (action == MotionEvent.ACTION_UP) {
		mViewManager.onRelease(x, y);
	    } else if (action == MotionEvent.ACTION_DOWN) {
		mViewManager.onPress(x, y);
	    }
	    mScreen.requestRender();
	    return true;
	}

	public void onClick(View v) {
	}
    }
}

