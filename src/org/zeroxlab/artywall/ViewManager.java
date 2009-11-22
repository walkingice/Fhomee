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

public class ViewManager {

    final String TAG="ViewManager";
    private Context mContext;
    private Rect mViewPort;
    private GLSurfaceView mSurfaceView;
    private WallRenderer  mRenderer;

    public ViewManager(Context context,GLSurfaceView surface) {
	mContext     = context;
	mSurfaceView = surface;
	mRenderer = new WallRenderer(mContext);
	mSurfaceView.setEGLConfigChooser(false);
	mSurfaceView.setRenderer(mRenderer);
    }

    class WallRenderer implements GLSurfaceView.Renderer {
	private Context mContext;
	private int mTextureID;

	private final static int VERTS = 3;
	private FloatBuffer mFVertexBuffer;
	private FloatBuffer mTexBuffer;
	private ShortBuffer mIndexBuffer;

	public WallRenderer(Context context) {
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
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	    gl.glDisable(GL10.GL_DITHER);
	    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
		    GL10.GL_FASTEST);
	    gl.glClearColor(.5f, .1f, .1f, 1);
	    gl.glShadeModel(GL10.GL_SMOOTH);
	    gl.glEnable(GL10.GL_DEPTH_TEST);
	    gl.glEnable(GL10.GL_TEXTURE_2D);
	    
	    TextureManager manager = TextureManager.getInstance();
	    manager.setContext(mContext);
	    mTextureID = manager.generateOneTexture(gl, R.drawable.robot);
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
	    gl.glViewport(0, 0, w, h);
	    float ratio = (float) w / h;
	    gl.glMatrixMode(GL10.GL_PROJECTION);
	    gl.glLoadIdentity();
	    gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7); 

	}

	public void onDrawFrame(GL10 gl) {
	    gl.glDisable(GL10.GL_DITHER);

	    gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		    GL10.GL_MODULATE);
	    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	    gl.glMatrixMode(GL10.GL_MODELVIEW);
	    gl.glLoadIdentity();

	    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	    GLU.gluLookAt(gl, 0, 0, -5, -2f, 0f, 0f, 0f, 1.0f, 0.0f);

	    gl.glActiveTexture(GL10.GL_TEXTURE0);
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
	    gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
	    gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
	    gl.glTranslatef(-2.0f,0.0f,0.0f);
	    gl.glRotatef(2, 0, 1.0f, 1.0f);
	    gl.glFrontFace(GL10.GL_CCW);
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
	    gl.glEnable(GL10.GL_TEXTURE_2D);
	    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
	    gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS,
		    GL10.GL_UNSIGNED_SHORT, mIndexBuffer);

	}
    }

    class GLObject {
	GLView mGLView;
	Point mPosition;
	GLObject(GLView view, Point position) {
	    mGLView   = view;
	    mPosition = position;
	}
    }
}

