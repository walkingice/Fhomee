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

import java.util.LinkedList;

import android.graphics.*;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

import android.graphics.RectF;

import java.nio.*;

public class ViewManager {

    final String TAG="ViewManager";
    private Context mContext;
    private Rect mViewPort;
    private GLSurfaceView mSurfaceView;
    private WallRenderer  mRenderer;
    private Timeline mTimeline;
    private ResourcesManager mResourceManager;
    private TextureManager   mTextureManager;

    private LinkedList<GLObject> mGLObjects;

    float mX=0, mY=0;
    public void setXY(float x, float y) {mX =x; mY = y;};

    public ViewManager(Context context,GLSurfaceView surface) {
	mContext     = context;
	mSurfaceView = surface;
	mRenderer = new WallRenderer(this);
	mSurfaceView.setEGLConfigChooser(false);
	mSurfaceView.setRenderer(mRenderer);

	mResourceManager = ResourcesManager.getInstance(mContext);

	mTextureManager  = TextureManager.getInstance();
	mTimeline        = Timeline.getInstance();
	mTimeline.monitor(mSurfaceView);

	mGLObjects = new LinkedList<GLObject>();
	initGLViews();
    }

    public void initGLViews() {
	GLObject obj;

	obj = new GLObject(-5, 5, 5, -5);
	obj.setTextureName("robot");
	mGLObjects.add(obj);

	obj = new GLObject(-6, 4, 4, -6);
	obj.setTextureName("flower");
	mGLObjects.add(obj);

	obj = new GLObject(-3, -10, 9, -22);
	mGLObjects.add(obj);
    }

    public void generateTextures(GL10 gl) {
	for (int i = 0; i < mGLObjects.size(); i++) {
	    GLObject obj = mGLObjects.get(i);
	    obj.generateTextures(gl, mResourceManager, mTextureManager);
	}
    }

    public void drawGLViews(GL10 gl) {
	GLObject obj;

	gl.glMatrixMode(gl.GL_MODELVIEW);

	for (int i = 0; i < mGLObjects.size(); i++) {
	    obj = mGLObjects.get(i);

	    gl.glLoadIdentity();
	    obj.moveModelViewToPosition(gl);
	    gl.glRotatef(mX, 0, 1, 0);
	    gl.glRotatef(mY, 0, 0, 1);
	    obj.draw(gl);
	}
    }

    class WallRenderer implements GLSurfaceView.Renderer {
	private ViewManager mManager;

	public WallRenderer(ViewManager manager) {
	    mManager = manager;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

	    mTextureManager.clearAll();

	    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
		    GL10.GL_FASTEST);
	    gl.glClearColor(.5f, .1f, .1f, 1);
	    gl.glShadeModel(GL10.GL_SMOOTH);
	    gl.glEnable(GL10.GL_DEPTH_TEST);
	    gl.glEnable(GL10.GL_TEXTURE_2D);

	    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	    gl.glEnable(gl.GL_BLEND);
	    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

	    mManager.generateTextures(gl);
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
	    gl.glLoadIdentity();
	    gl.glViewport(0, 0, w, h);
	    gl.glMatrixMode(gl.GL_PROJECTION);
	    gl.glLoadIdentity();
	    gl.glOrthof(-16, 16, -23, 23, 3.0f, 50.0f);

	}

	public void onDrawFrame(GL10 gl) {
	    gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		    GL10.GL_MODULATE);
	    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	    mManager.drawGLViews(gl);
	}
    }
}

