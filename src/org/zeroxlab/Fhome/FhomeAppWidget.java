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
import android.widget.RemoteViews;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;

import org.zeroxlab.Fhome.TextureManager.TextureObj;
class FhomeAppWidget extends AppWidgetHostView {
    GLAppWidget mGLParent;

    final static String TAG = "FhomeAppWidget";
    static TextureManager TextureMgr = TextureManager.getInstance();
    int counter = 0;
    int size = 256;

    FhomeAppWidget(Context context) {
        super(context);
    }

    public void setGLParent(GLAppWidget p) {
        mGLParent = p;
    }

    public void updateAppWidget(RemoteViews remoteViews) {
        super.updateAppWidget(remoteViews);
        Log.i(TAG, "update appwidget");
        if (mGLParent != null) {
            Log.i(TAG,"update...Am I visible? " + getVisibility());
            draw(mGLParent.mCanvas);
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "app widget on draw");
    }

    public void invalidate() {
        super.invalidate();
        this.setVisibility(View.VISIBLE);
        Log.i(TAG, "I am dirty");
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (mGLParent == null) {
            return false;
        }
        this.measure(size, size);
        this.layout(0, 0, size, size);
        mGLParent.mCanvas.drawARGB(0, 255, 255, 255);
        boolean b = super.drawChild(mGLParent.mCanvas, child, drawingTime);
        Log.i(TAG,"draw...Am I visible? " + getVisibility());
        Log.i(TAG,"draw child");
        TextureObj old = mGLParent.getTexture();
        if (old != null) {
            TextureMgr.removeTextureObj(old);
        }
        counter++;
        TextureObj obj = TextureMgr.getTextureObj(mGLParent.mBitmap, "hehehe"+counter);
        mGLParent.setTexture(obj);
        Launcher.redraw();
        return b;
    }
}

