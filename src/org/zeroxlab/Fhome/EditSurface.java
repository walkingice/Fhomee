/*
 * Authored By Julian Chu <walkingice@0xlab.org>
 *
 * Copyright (c) 2010 0xlab.org - http://0xlab.org/
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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import javax.microedition.khronos.opengles.GL10;

import java.util.LinkedList;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

import org.zeroxlab.Fhome.TextureManager.TextureObj;

public class EditSurface extends GLObject implements Touchable, GLObject.ClickListener {

    final String TAG = "EditSurface";

    Timeline    mTimeline;
    ViewManager mViewManager;

    public static String sTexBackground = "editlayer_background";
    public static String sTexDelete = "editlayer_delete";
    public static String sTexCreate = "editlayer_create_pet";
    public static final float sButtonWidth  = 100f;
    public static final float sButtonHeight = 100f;

    private static float sWidth  = ViewManager.mScreenWidth;
    private static float sHeight = ViewManager.mScreenHeight;

    private boolean mIsEditing = false;

    private Poster mTarget;
    private GLObject mEditing;
    private GLObject mCreate;
    private GLObject mDelete;

    EditSurface() {
        super(0, 0, sWidth, sHeight);

        mTimeline = Timeline.getInstance();
        mViewManager = ViewManager.getInstance();
        setDefaultTextureName(sTexBackground);
        mCreate = new GLObject(100, 100);
        mDelete = new GLObject(100, 100);
        mEditing = new GLObject(100, 100);
        mCreate.setDefaultTextureName(sTexCreate);
        mDelete.setDefaultTextureName(sTexDelete);
        addChild(mCreate);
        addChild(mDelete);
    }

    public void edit(Poster target) {
        if (mTarget != null) {
            Log.i(TAG, "ooops, there is existing a target");
        }

        mIsEditing = true;
        mTarget = target;
        TextureObj texture = mTarget.getDefaultTexture();
        float x = mTarget.getXPx();
        float y = mTarget.getYPx();
        float width  = mTarget.getWidthPx();
        float height = mTarget.getHeightPx();
        if (x == GLObject.UNDEFINE || y == GLObject.UNDEFINE) {
            x = 0f;
            y = 0f;
        }
        if (width == GLObject.UNDEFINE || height == GLObject.UNDEFINE) {
            width  = 150f;
            height = 150f;
        }

        mEditing.setDefaultTextureName(texture.getName());
        mEditing.setTexture(texture);
        mEditing.setXYPx(x, y);
        mEditing.setSizePx(width, height);
        addChild(mEditing);
    }

    public void finish() {
        mTarget.setXYPx(mEditing.getXPx(), mEditing.getYPx());
        mTarget.setSizePx(mEditing.getWidthPx(), mEditing.getHeightPx());
        mViewManager.addPosterToCurrentRoom(mTarget);
        mTarget = null;
        removeChild(mEditing);
        mIsEditing = false;
    }

    public boolean isEditing() {
        return mIsEditing;
    }

    public void resize(int screenWidth, int screenHeight) {
        sWidth = screenWidth;
        sHeight = screenHeight;
        resize();
    }

    private void resize() {
        setXYPx(0, 0);
        setSizePx(sWidth, sHeight);
        mCreate.setSizePx(sButtonWidth, sButtonHeight);
        mDelete.setSizePx(sButtonWidth, sButtonHeight);
        float height = sHeight - sButtonHeight;
        mDelete.setXYPx(0, height);
        mCreate.setXYPx(sWidth - sButtonWidth, height);
    }

    @Override
    public void draw(GL10 gl) {
        if (isEditing()) {
            super.draw(gl);
        }

        return;
    }

    @Override
    protected void drawMyself(GL10 gl) {
        gl.glColor4f(1f, 1f, 1f, 0.5f);
        super.drawMyself(gl);
        gl.glColor4f(1f, 1f, 1f, 1f);
    }

    public boolean onPressEvent(PointF point, MotionEvent event) {
        return false;
    }

    public boolean onReleaseEvent(PointF point, MotionEvent event) {
        return false;
    }

    public boolean onDragEvent(PointF point, MotionEvent event) {
        return false;
    }

    public boolean onLongPressEvent(PointF point, MotionEvent event) {
        return false;
    }

    public void onClick(GLObject obj) {
        if (obj == mEditing) {
            finish();
        }
    }
}

