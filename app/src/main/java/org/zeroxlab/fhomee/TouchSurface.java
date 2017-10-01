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

package org.zeroxlab.fhomee;

import android.util.Log;

import org.zeroxlab.fhomee.animation.GLTransAni;

import javax.microedition.khronos.opengles.GL10;

/**
 * TouchSurface is the first surface glue on the Near surface
 */
public class TouchSurface extends GLObject {

    final String TAG = "TouchSurface";

    Timeline mTimeline;

    private static float Width = ViewManager.convertToLevel(0, ViewManager.PROJ_WIDTH);
    private static float Height = ViewManager.convertToLevel(0, ViewManager.PROJ_HEIGHT);
    private GLObject mSight;
    private GLTransition mNotify;
    private float mSightWidth = 20f;
    private float mSightHeight = 20f;

    TouchSurface() {
        super(0, 0, Width, Height);

        mTimeline = Timeline.getInstance();

        mSight = new GLObject(0f, 0f, mSightWidth, mSightHeight);
        mSight.setTextureByName("sight00");

        String name[] = new String[]{"sight01", "sight02", "sight03", "sight04"};
        long time[] = new long[]{80, 50, 50, 50};
        mNotify = new GLTransition(name, time);
    }

    public void clickAt(float x, float y) {
        x = x - mSightWidth / 2;
        y = y - mSightHeight / 2;
        GLTransAni notify = new GLTransAni(mNotify);
        mSight.setXY(x, y);
        Log.i(TAG, "Set to " + x + " " + y);
        mSight.setAnimation(notify);
        mTimeline.addAnimation(notify);
    }

    public void draw(GL10 gl) {
        mSight.draw(gl);
    }
}

