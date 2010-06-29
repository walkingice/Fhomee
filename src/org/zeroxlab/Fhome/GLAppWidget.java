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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;
import android.appwidget.*;
import android.content.ComponentName;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;

import org.zeroxlab.Fhome.TextureManager.TextureObj;

public class GLAppWidget extends Poster {

    final String TAG = "GLAppWidget";
    private static Context sContext = null;

    private final static String WIDGET_CLASSNAME = "com.android.alarmclock";
    private final static String WIDGET_PROVIDER  = "com.android.alarmclock.AnalogAppWidgetProvider";

    public static void setContext(Context context) {
        sContext = context;
    }

    private final int size = 256;

    public FhomeAppWidget mWidget;
    public Bitmap mBitmap;
    public Canvas mCanvas;
    private int mId;

    GLAppWidget(int id) {
	super(200, 200);
        if (sContext == null) {
            Log.i(TAG, "GLAppWidget needs Context");
            this.clear();
            return;
        }

        mBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        mCanvas = new Canvas(mBitmap);

        mId = id;
        AppWidgetManager mgr       = Launcher.getWidgetManager();
        ComponentName component    = new ComponentName(WIDGET_CLASSNAME, WIDGET_PROVIDER);
        AppWidgetProviderInfo info = mgr.getAppWidgetInfo(mId);

        mWidget = (FhomeAppWidget)Launcher.getWidgetHost().createView(sContext, mId, info);
        mWidget.setAppWidget(mId, info);
        mWidget.setGLParent(this);
        mWidget.measure(size, size);
        mWidget.layout(0, 0, size, size);
        mWidget.draw(mCanvas);
        mWidget.setVisibility(View.VISIBLE);
        TextureObj obj = TextureMgr.getTextureObj(mBitmap, "hehehe");
        setTexture(obj);
    }

    public void onClick() {
        mWidget.invalidate();
    }
}

