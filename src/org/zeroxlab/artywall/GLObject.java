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

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

/** 
 * GLObject represent any Object on the screen
 * it encapsulants the information including position, size...etc
 */
public class GLObject {

    final String TAG = "GLObject";

    protected float mDepth = -20f;

    GLView mGLView;
    PointF mPosition;
    RectF  mRect;

    String mTextureName = "zeroxdoll"; //default

    GLObject(GLView view, PointF position, RectF rect) {
	mGLView   = view;
	mPosition = position;
	mRect     = rect;

	view.setSize(mRect);
    }

    GLObject(float l, float t, float r, float b) {
	this(null, l, t, r, b);
    }

    GLObject(GLView view, float l, float t, float r, float b) {
	if (view == null) {
	    view = new GLView();
	}

	float width  = Math.abs(r - l);
	float height = Math.abs(b - t);

	mGLView = view;
	mRect = new RectF(0, 0, width, height);
	mPosition = new PointF(l, t);

	view.setSize(mRect);
    }

    public float getPositionX() {
	return mPosition.x;
    }

    public float getPositionY() {
	return mPosition.y;
    }

    public void setTextureID(int id) {
	mGLView.setTextureID(id);
    }

    public void setTextureName(String name) {
	mTextureName = name;
    }

    public void moveModelViewToPosition(GL10 gl) {
	gl.glTranslatef(mPosition.x, mPosition.y, mDepth);
    }

    public void generateTextures(GL10 gl, ResourcesManager resM, TextureManager texM) {
	int    id;
	Bitmap bitmap;
	bitmap = resM.getBitmapByName(mTextureName);
	id     = texM.generateOneTexture(gl, bitmap, mTextureName);
	mGLView.setTextureID(id);
    }

    public void draw(GL10 gl) {
	mGLView.drawGLView(gl);
    }
}

