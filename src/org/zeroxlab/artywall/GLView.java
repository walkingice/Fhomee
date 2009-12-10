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
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import android.graphics.*;

public class GLView {

    private boolean visible = true;
    final String TAG = "GLView";
    private float updateRate = 1f;

    Rect mArea; // quick hack, it supposed to be 4 arbitrary points
		// but not a Rectangle. Maybe a GLView will compose
		// arbitrary triangle in the feature...hope so.

    public void drawGLView() {
	if(!visible) {
	    return;
	}
    }

    public void hide() {
	visible = false;
    }

    pubic void show() {
	visible = true;
    }
}

