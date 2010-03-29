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

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

import java.util.LinkedList;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

import org.zeroxlab.Fhome.TextureManager.TextureObj;

/** 
 * GLObject represent any Object on the screen
 * it encapsulants the information including position, size...etc
 */
public class GLString extends GLObject {

    final String TAG = "GLString";
    public final static float mMinWidth  = 20;
    public final static float mMinHeight = 20;

    protected Paint mTextPaint;
    protected float mDefaultTextSize = 30f;

    GLString(String string) {
	this(string, 255, 0, 0, 0);
    }

    GLString(String string, int a, int r, int g, int b) {
	this(mMinWidth, mMinHeight, string, a, r, g, b);
    }

    GLString(float width, float height, String string
	    , int a, int r, int g, int b) {
	this(0f, 0f, width, height, string, a, r, g, b);
    }

    GLString(float x, float y, float width, float height, String string
	    , int a, int r, int g, int b) {
	super(x, y, width, height);
	mTextPaint = new Paint();
	mTextPaint.setAntiAlias(true);
	mTextPaint.setARGB(a, r, g, b);
	mTextPaint.setTextSize(mDefaultTextSize);
	setDefaultTextureName(string);
    }

    public void setAlpha(int alpha) {
	mTextPaint.setAlpha(alpha);
    }

    public void setColor(int color) {
	mTextPaint.setColor(color);
    }

    @Override
    protected void createGLView() {
	/* This GLObjew is visible and has texture, create a GLView */
	if (mGLView == null) {
	    mGLView = new GLView();
	    mGLView.setSize(mRect);

	    TextureObj texture;
	    texture= TextureMgr.getStringTextureObj(mDefaultTextureName, mTextPaint);
	    mGLView.setTexture(texture);
	}

	mVisible = true;
    }
}

