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

import android.opengl.*;
import android.graphics.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.*;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;
import android.os.SystemClock;

public class Triangle{

    final String TAG = "Triangle";
    private Context mContext;
    private Rect mViewPort;
    private int mTextureID;

    private final static int VERTS = 3;
    private FloatBuffer mFVertexBuffer;
    private FloatBuffer mTexBuffer;
    private ShortBuffer mIndexBuffer;
    private long mTime;

    public Triangle(Context context) {
	mContext = context;
	ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
	vbb.order(ByteOrder.nativeOrder());
	mFVertexBuffer = vbb.asFloatBuffer();

	ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
	tbb.order(ByteOrder.nativeOrder());
	mTexBuffer = tbb.asFloatBuffer();

	ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
	ibb.order(ByteOrder.nativeOrder());
	mIndexBuffer = ibb.asShortBuffer();

	float[] coords = {
	    // X, Y, Z
	    -0.5f, -0.25f, 0,
	    0.5f, -0.25f, 0,
	    0.0f,  0.559016994f, 0
	};

	for (int i = 0; i < VERTS; i++) {
	    for(int j = 0; j < 3; j++) {
		mFVertexBuffer.put(coords[i*3+j] * 2.0f);
	    }
	}

	for (int i = 0; i < VERTS; i++) {
	    for(int j = 0; j < 2; j++) {
		mTexBuffer.put(coords[i*3+j] * 2.0f + 0.5f);
	    }
	}

	for(int i = 0; i < VERTS; i++) {
	    mIndexBuffer.put((short) i);
	}

	mFVertexBuffer.position(0);
	mTexBuffer.position(0);
	mIndexBuffer.position(0);
	mTime = SystemClock.uptimeMillis();
    }

    public void createTextures(GL10 gl) {
	String imgName = "robot";
	ResourcesManager resManager = ResourcesManager.getInstance(mContext);
	TextureManager manager = TextureManager.getInstance();
	Bitmap bitmap = resManager.getBitmapByName(imgName);
	mTextureID = manager.generateOneTexture(gl, bitmap, imgName);
    }

    public void onDrawFrame(GL10 gl) {

	long now = SystemClock.uptimeMillis();
	Log.i(TAG,"now = "+now);
	float angle = 0.05f * (float)(now - mTime);
	gl.glActiveTexture(GL10.GL_TEXTURE0);
	gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
	gl.glTranslatef(-2.0f,0.0f,0.0f);
	gl.glRotatef(angle, 0, 1.0f, 1.0f);
	gl.glFrontFace(GL10.GL_CCW);
	gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
	gl.glEnable(GL10.GL_TEXTURE_2D);
	gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
	gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS,
		GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
    }
}
