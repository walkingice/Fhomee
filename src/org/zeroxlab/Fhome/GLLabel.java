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
import android.graphics.Rect;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

import java.util.LinkedList;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

import org.zeroxlab.Fhome.TextureManager.TextureObj;

/** 
 * GLLabel is a GLObject which contains a String text.
 *
 * The coordinate at OpenGL is different from Pixel-based skia.
 * If user set text size to GLLabel, GLLabel should convert the
 * size into OpenGL coordinate then display the text as User expect.
 * Text always display at the center of GLLabel, setSize never
 * effect the size of Text but only background.
 */
public class GLLabel extends GLObject {

    final String TAG = "GLLabel";
    public final static float mMinWidth  = 200;
    public final static float mMinHeight = 200;

    protected Paint mTextPaint;
    protected float mDefaultTextSize = 20f;
    protected int mLevel = ViewManager.LEVEL_POSTER;

    /* stores the text size in pixel */
    float mTextWidthPx  = 0f;
    float mTextHeightPx = 0f;

    /* stores the original texture size. User might
       change the size of GLLabel. Somehow we need
       the original size to display font size correctly */
    float mTextureWidth  = 0f;
    float mTextureHeight = 0f;

    /*TODO: A String texture needs a Name at the same time.
      I don't have any good ideat to generate a readable and
      unique name for a string. */
    String mString;
    String mStringName;
    GLView mTextView;
    RectF  mTextRect;
    float  mTextX = 0f;
    float  mTextY = 0f;

    GLLabel() {
	this("");
    }

    GLLabel(String string) {
	this(string, 255, 0, 0, 0);
    }

    GLLabel(String string, int a, int r, int g, int b) {
	this(mMinWidth, mMinHeight, string, a, r, g, b);
    }

    GLLabel(float width, float height, String string
	    , int a, int r, int g, int b) {
	this(0f, 0f, width, height, string, a, r, g, b);
    }

    GLLabel(float x, float y, float width, float height, String string
	    , int a, int r, int g, int b) {
	super(x, y, width, height);
	mTextPaint = new Paint();
	mTextPaint.setAntiAlias(true);
	mTextPaint.setARGB(a, r, g, b);
	setTextSize(mDefaultTextSize);
	if (!string.equals("")) {
	    setText(string);
	}
    }

    public void setLevel(int level) {
	mLevel = level;
    }

    public void setColor(int color) {
	mTextPaint.setColor(color);
    }

    public void setTextSize(float size) {
	mTextPaint.setTextSize(size);

	if (mTextView != null) {
	    setText(mString);
	}
    }

    public void setText(String text) {
	destroyText();
	mString = text;
	mStringName = text;
	createText();
    }

    /* setSize only change the size of background
       do not change the size of text */
    @Override
    public void setSize(float width, float height) {
	super.setSize(width, height);
	tweakTextPosition();
    }

    private void tweakTextPosition() {

	if (mTextView == null) {
	    return;
	}

	float textWidth  = mTextPaint.measureText(mString);
	float ascent     = Math.abs(mTextPaint.ascent());
	float descent    = Math.abs(mTextPaint.descent());
	float textHeight = ascent + descent;

	mTextWidthPx  = textWidth;
	mTextHeightPx = textHeight;

        /* sadly, width and height should be the power of 2 */
	/* Therefore, the size of TextView is usually larger than we seen */
	Bitmap textureBitmap = mTextView.getTexture().getBitmap();
        float textureWidth  = textureBitmap.getWidth();
        float textureHeight = textureBitmap.getHeight();

	int screenWidth  = ViewManager.mScreenWidth;
	int screenHeight = ViewManager.mScreenHeight;
	float nearWidth  = ViewManager.PROJ_WIDTH  * textureWidth / screenWidth;
	float nearHeight = ViewManager.PROJ_HEIGHT * textureHeight/ screenHeight;

	mTextureWidth  = ViewManager.convertToLevel(mLevel, nearWidth);
	mTextureHeight = ViewManager.convertToLevel(mLevel, nearHeight);
	mTextRect = new RectF(0, 0, mTextureWidth, mTextureHeight);
	mTextView.setSize(mTextRect);

	/* User only care about the visible area or text */
	nearWidth  = ViewManager.PROJ_WIDTH  * textWidth / screenWidth;
	nearHeight = ViewManager.PROJ_HEIGHT * textHeight/ screenHeight;
	float textW = ViewManager.convertToLevel(mLevel, nearWidth);
	float textH = ViewManager.convertToLevel(mLevel, nearHeight);

	/* If we have background, move text to the center */
	if (mGLView == null) {
	    mTextX = 0f;
	    mTextY = 0f;
	    // No background, GLLabel size equals to Text size
	    super.setSize(textW, textH);
	} else {
	    /* Put text in the center of background */
	    mTextX = (mRect.width() - textW)/ 2;
	    mTextY = (mRect.height()- textH)/ 2;
	}
    }

    public void setBackground(String background) {
	super.setDefaultTextureName(background);
	tweakTextPosition();
    }

    protected void createText() {
	if (mTextView == null) {
	    mTextView = new GLView();

	    TextureObj texture;
	    texture = TextureMgr.getStringTextureObj(mStringName, mString, mTextPaint);
	    mTextView.setTexture(texture);
	}

	tweakTextPosition();
	mVisible = true;
    }

    protected void destroyText() {
	if (mTextView != null) {
	    TextureObj obj = mTextView.getTexture();
	    TextureMgr.removeTextureObj(obj);
	    mTextView.clear();
	    mTextView = null;
	}

	mVisible = (mGLView != null); // do background?
    }

    @Override
    protected void drawMyself(GL10 gl) {
	if (mGLView != null) {
	    mGLView.drawGLView(gl);
	}

	if (mTextView != null) {
	    gl.glTranslatef(mTextX, mTextY, 0f);
	    mTextView.drawGLView(gl);
	}

	gl.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    protected void destroyGLView() {
	super.destroyGLView();

	mVisible = (mTextView != null); // no backgroud but has Text
    }
}

