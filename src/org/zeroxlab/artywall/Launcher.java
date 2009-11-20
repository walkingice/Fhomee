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
    TotalScreen screen;
    GestureInterpreter mInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	LinearLayout layout = new LinearLayout(this);
	screen = new TotalScreen(this);
	mInterpreter = new GestureInterpreter(320,480);
	layout.addView(screen);
	setContentView(layout);
    }

    @Override
    protected void onPause() {
	super.onPause();
	screen.onPause();
    }

    @Override
    protected void onResume() {
	super.onResume();
	screen.onResume();
    }

    class TotalScreen extends GLSurfaceView {
	ViewManager mViewManager;
	TotalScreen(Context context) {
	    super(context);
	    mViewManager = new ViewManager(context, this);
	}

	public void onPause() {
	}

	public void onResume() {
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    mInterpreter.processMotionEvent(event);
	    return true;
	}

	protected void dispatchDraw(Canvas canvas) {
	    Paint p = new Paint();
	    p.setColor(Color.BLUE);
	    canvas.drawRect(mInterpreter.scaleArea,p);
	    p.setColor(Color.YELLOW);
	    canvas.drawRect(mInterpreter.shiftArea, p);
	    p.setColor(Color.RED);
	    canvas.drawRect(mInterpreter.triggerArea, p);
	}
    }
}

