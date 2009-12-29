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

    public static boolean USE_ORTHO = true;
    public static float PROJ_LEFT   = -16f;
    public static float PROJ_RIGHT  = 16f;
    public static float PROJ_BOTTOM = -23f;
    public static float PROJ_TOP    = 23f;
    public static float PROJ_NEAR   = 3f;
    public static float PROJ_FAR    = 50f;

    final String TAG="ViewManager";
    private Context mContext;
    private Rect mViewPort;
    private GLSurfaceView mSurfaceView;
    private WallRenderer  mRenderer;
    private Timeline mTimeline;
    private ResourcesManager mResourceManager;
    private TextureManager   mTextureManager;

    private LinkedList<GLObject> mGLObjects;
    private Room room, room2;

    GLObject wanted1;
    GLObject wanted2;
    GLObject wanted3;
    GLObject wanted4;
    GLObject wanted5;
    GLObject wanted6;

    private float ratio = 1.0f;
    public void updateRatio(float x, float y) {
	float temp = (1000 - y) / 1000;
	temp = Math.max(0.5f, temp);
	temp = Math.min(1.0f, temp);
	ratio = temp;
    }

    private boolean test = true;
    public void performClick() {
	long time = 300;
	if (test) {
	    GLTranslate test = new GLTranslate(time, 15f, 10f);
	    wanted1.setAnimation(test);
	    mTimeline.addAnimation(test);
	} else {
	    GLTranslate test = new GLTranslate(time, 5f, 0f);
	    wanted1.setAnimation(test);
	    mTimeline.addAnimation(test);
	}

	GLRotate rr = new GLRotate(time, wanted2.getAngle()+90f, GLRotate.CLOCKWISE);
	wanted2.setAnimation(rr);
	mTimeline.addAnimation(rr);

	test = !test;
    }

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
	room = new Room(0, "wall", "ground");
	room2 = new Room(1, "wall_2", "ground");

	GLObject elf1 = new GLObject(0, 0, 1, 1);
	GLObject elf2 = new GLObject(0, 0, 1, 1);
	GLObject elf3 = new GLObject(0, 0, 1, 1);
	GLObject elf4 = new GLObject(0, 0, 1, 1);

	elf1.setTextureName("zeroxdoll");
	elf2.setTextureName("android");
	elf3.setTextureName("beagle");
	elf4.setTextureName("flower");

	room.addElf(elf1);
	room.addElf(elf2);
	room.addElf(elf3);
	room2.addElf(elf4);

	wanted1 = new GLObject(1, 1, 13, 13);
	wanted2 = new GLObject(10, 1, 14, 13);
	wanted3 = new GLObject(1, 9, 13, 13);
	wanted4 = new GLObject(15, 8, 14, 13);
	wanted5 = new GLObject(1, 1, 15, 15);
	wanted6 = new GLObject(22, 15, 13, 13);
	wanted1.setTextureName("luffy");
	wanted2.setTextureName("nami");
	wanted3.setTextureName("sanji");
	wanted4.setTextureName("robin");
	wanted5.setTextureName("buggy");
	wanted6.setTextureName("zoro");
	room.addItem(wanted1, 1f, 1f, 0f);
	room.addItem(wanted2, 15f, 1f, 15f);
	room.addItem(wanted3, 3f, 15f, 33f);
	room.addItem(wanted4, 15f, 21f, 3f);
	room2.addItem(wanted5, 1f, 1f, 20f);
	room2.addItem(wanted6, 22f, 15f, 180f);

    }

    public void generateTextures(GL10 gl) {
	/*
	for (int i = 0; i < mGLObjects.size(); i++) {
	    GLObject obj = mGLObjects.get(i);
	    obj.generateTextures(gl, mResourceManager, mTextureManager);
	}
	*/
	room.generateTextures(gl, mResourceManager, mTextureManager);
	room2.generateTextures(gl, mResourceManager, mTextureManager);
    }

    public void drawGLViews(GL10 gl) {
	Log.i(TAG,"DRAW!!!!!");
	GLObject obj;

	gl.glMatrixMode(gl.GL_MODELVIEW);

	/*
	for (int i = 0; i < mGLObjects.size(); i++) {
	    obj = mGLObjects.get(i);

	    gl.glLoadIdentity();
	    obj.draw(gl);
	}
	*/
	gl.glLoadIdentity();
	gl.glScalef(ratio, ratio, ratio);

	/* the coordinate of OpenGL is different from normal computer system
	 * We may rotate the coordinate so we don't have to worry about that.
	 */
	gl.glRotatef(180f, 1f, 0f, 0f); // now the +x is heading for right
					//         +y is heading for bottom
	gl.glTranslatef(-16f, -23f, 0f);// move to Left-Top
	gl.glTranslatef(0f, 0f, 20f);   // after rotating, the Z-axis upside down

	gl.glPushMatrix();
	room.draw(gl);
	gl.glPopMatrix();

	// Move to next room
	gl.glTranslatef(32f, 0f, 0f);

	gl.glPushMatrix();
	room2.draw(gl);
	gl.glPopMatrix();
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
	    if (USE_ORTHO) {
		gl.glOrthof(PROJ_LEFT, PROJ_RIGHT
			, PROJ_BOTTOM, PROJ_TOP
			, PROJ_NEAR, PROJ_FAR);
	    } else {
		gl.glFrustumf(PROJ_LEFT, PROJ_RIGHT
			, PROJ_BOTTOM, PROJ_TOP
			, PROJ_NEAR, PROJ_FAR);
	    }
	}

	public void onDrawFrame(GL10 gl) {
	    gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		    GL10.GL_MODULATE);
	    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	    mManager.drawGLViews(gl);
	}
    }
}

