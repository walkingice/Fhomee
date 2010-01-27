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

    public static boolean USE_ORTHO = false; // use Ortho or Frustum?
    public static float PROJ_LEFT   = -16f;
    public static float PROJ_RIGHT  = 16f;
    public static float PROJ_BOTTOM = -23f;
    public static float PROJ_TOP    = 23f;
    public static float PROJ_NEAR   = 3f;
    public static float PROJ_FAR    = 50f;

    public static float PROJ_WIDTH  = Math.abs(PROJ_RIGHT - PROJ_LEFT);
    public static float PROJ_HEIGHT = Math.abs(PROJ_TOP - PROJ_BOTTOM);

    public static float LEVEL_0     = 4f;  // TouchSurface
    public static float LEVEL_1     = 20f; // elf
    public static float LEVEL_2     = 28f; // poster
    public static float LEVEL_3     = 30f; // wall
    /*     Z +------ X'
     *       |     /
     *       |    /
     *  NEAR +---/ X
     *       |  /
     *       | /
     *       |/
     *
     * X' : X = Z : NEAR
     * X' = X * Z / NEAR
     *
     * X' - the X location we destination surface
     * X  - the X location at NEAR surface
     * NEAR - the Z location at NEAR surface
     * Z    - the Z location at destination surface
     */
    public static float ZN_LEVEL_0  = LEVEL_0 / PROJ_NEAR;
    public static float ZN_LEVEL_1  = LEVEL_1 / PROJ_NEAR;
    public static float ZN_LEVEL_2  = LEVEL_2 / PROJ_NEAR;
    public static float ZN_LEVEL_3  = LEVEL_3 / PROJ_NEAR;

    public static int mScreenWidth;
    public static int mScreenHeight;

    final String TAG="ViewManager";
    private Context mContext;
    private Rect mViewPort;
    private GLSurfaceView mSurfaceView;
    private WallRenderer  mRenderer;
    private Timeline mTimeline;
    private ResourcesManager mResourceManager;
    private TextureManager   mTextureManager;

    private TouchSurface mTouchSurface;
    private World mWorld;
    private Room room, room2, room3, room4, room5, room6;

    public final float FAREST_PUSHING_DEPTH = 10f;
    private float mDrawingDepth;

    GLTransition transition;
    GLObject wanted1;
    GLObject wanted2;
    GLObject wanted3;
    GLObject wanted4;
    GLObject wanted5;
    GLObject wanted6;
    GLObject wanted7;

    BottomBar mBar;
    Elf elf1;
    Elf elf2;
    Elf elf3;
    Elf elf4;

    public void pushWorld(float eventX, float eventY) {
	float ratio = eventY / mScreenHeight;
	mDrawingDepth = FAREST_PUSHING_DEPTH * ratio;
    }

    private boolean test = true;
    public void performClick(float screenX, float screenY) {
	Log.i(TAG,"click on screen x="+screenX+" y="+screenY);
	float nearX = PROJ_WIDTH  * screenX / mScreenWidth;
	float nearY = PROJ_HEIGHT * screenY / mScreenHeight;
	Log.i(TAG,"near x="+nearX+" y="+nearY);
	int id = mBar.pointerAt(convertToLevel(1, nearX)
		, convertToLevel(1, nearY));
	if (id == -1) {
	    id = mWorld.pointerAt(nearX, nearY);
	}

	Log.i(TAG,"Click on GLObject id = "+id);

	mTouchSurface.clickAt(nearX, nearY);

	if (id > 1) {
	    GLObject obj = ObjectManager.getInstance().getGLObjectById(id);

	    GLFade fade = new GLFade(1000, 1f, 1f, 1f);
	    obj.setAnimation(fade);
	    mTimeline.addAnimation(fade);
	    obj.onClick();
	} else if (id == 1){
	    GLTransAni tr = new GLTransAni(transition);
	    elf1.setAnimation(tr);
	    mTimeline.addAnimation(tr);
	}
    }

    public static float convertToLevel(int level, float from) {
	if (level == 0) {
	    return from * ZN_LEVEL_0;
	} else if (level == 1) {
	    return from * ZN_LEVEL_1;
	} else if (level == 2) {
	    return from * ZN_LEVEL_2;
	} else if (level == 3) {
	    return from * ZN_LEVEL_3;
	}

	return from;
    }

    public ViewManager(Context context,GLSurfaceView surface) {
	mContext     = context;
	mSurfaceView = surface;
	mRenderer = new WallRenderer(this);
	mSurfaceView.setEGLConfigChooser(false);
	mSurfaceView.setRenderer(mRenderer);

	ResourcesManager.setContext(mContext);
	mResourceManager = ResourcesManager.getInstance();
	mTextureManager  = TextureManager.getInstance();
	mTimeline        = Timeline.getInstance();
	mTimeline.monitor(mSurfaceView);

	initGLViews();
    }

    public void initGLViews() {
	room = new Room("wall_6", "ground_2");
	room2 = new Room("wall_2", "ground");
	room3 = new Room("wall_3", "ground");
	room4 = new Room("wall_4", "ground");
	room5 = new Room("wall_5", "ground");
	room6 = new Room("wall", "ground");

	String name[] = new String[] {"ani_1"
	    , "ani_2", "ani_3", "ani_4", "ani_5"};
	long time[] = new long[] {2000
	    , 100, 100, 100, 100};
	transition = new GLTransition(name, time);

	elf1 = new Elf();
	elf2 = new Elf();
	elf3 = new Elf();
	elf4 = new Elf();

	elf1.setDefaultTextureName("mario");
	elf2.setDefaultTextureName("luigi");
	elf3.setDefaultTextureName("peach");
	elf4.setDefaultTextureName("toad");

	float barWidth  = convertToLevel(1, PROJ_WIDTH);
	float barHeight = convertToLevel(1, PROJ_HEIGHT * 0.15f);
	mBar = new BottomBar(barWidth, barHeight);
	mBar.setXY(0, convertToLevel(1, PROJ_HEIGHT * 0.85f));
	mBar.addElf(elf1);
	mBar.addElf(elf2);
	mBar.addElf(elf3);
	mBar.addElf(elf4);

	wanted1 = new GLObject(1, 1, 100, 120);
	wanted2 = new GLObject(10, 1, 100, 100);
	wanted3 = new GLObject(1, 9, 100, 100);
	wanted4 = new GLObject(15, 8, 100, 100);
	wanted5 = new GLObject(1, 1, 150, 100);
	wanted6 = new GLObject(22, 15, 100, 100);
	wanted7 = new GLObject(0, 0, 250, 250);
	wanted1.setDefaultTextureName("luffy");
	wanted2.setDefaultTextureName("nami");
	wanted3.setDefaultTextureName("sanji");
	wanted4.setDefaultTextureName("robin");
	wanted5.setDefaultTextureName("buggy");
	wanted6.setDefaultTextureName("zoro");
	wanted7.setDefaultTextureName("bear");
	room4.addItem(wanted1, 100f, 100f, 0f);
	room4.addItem(wanted2, 150f, 10f, 15f);
	room4.addItem(wanted3, 30f, 150f, 33f);
	room5.addItem(wanted4, 150f, 210f, 3f);
	room2.addItem(wanted5, 10f, 10f, 20f);
	room2.addItem(wanted6, 220f, 150f, 180f);
	room3.addItem(wanted7, 130f, 30f, 30f);

	mWorld = new World();
	mWorld.addRoom(room);
	mWorld.addRoom(room2);
	mWorld.addRoom(room3);
	mWorld.addRoom(room4);
	mWorld.addRoom(room5);
	mWorld.addRoom(room6);

	mTouchSurface = new TouchSurface();
    }

    public void generateTextures() {
	transition.generateTextures();
	mTouchSurface.generateTextures();
	mBar.generateTextures();
	mWorld.generateTextures();
    }

    public void drawGLViews(GL10 gl) {
	Log.i(TAG,"Redraw!!");
	GLObject obj;

	gl.glMatrixMode(gl.GL_MODELVIEW);

	gl.glLoadIdentity();

	/* the coordinate of OpenGL is different from normal computer system
	 * We may rotate the coordinate so we don't have to worry about that.
	 */
	gl.glRotatef(180f, 1f, 0f, 0f); // now the +x is heading for right
					//         +y is heading for bottom

	// mDrawingDepth control the looking size of World
	gl.glTranslatef(0f, 0f, mDrawingDepth);

	/* Draw Level 3, the Rooms of World */
	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(3, PROJ_LEFT)
		, convertToLevel(3, PROJ_BOTTOM )
		, 0f);// move to Left-Top

	gl.glTranslatef(0f, 0f, LEVEL_3);   // after rotating, the Z-axis upside down
	mWorld.draw(gl);
	gl.glPopMatrix();

	/* Draw Level 1, the Bar and Elfs */
	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(1, PROJ_LEFT)
		, convertToLevel(1, PROJ_BOTTOM )
		, 0f);// move to Left-Top

	gl.glTranslatef(0f, 0f, LEVEL_1);   // after rotating, the Z-axis upside down

	mBar.draw(gl);
	gl.glPopMatrix();

	/* Draw the TouchSurface */
	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(0, PROJ_LEFT)
		, convertToLevel(0, PROJ_BOTTOM )
		, 0f);// move to Left-Top
	gl.glTranslatef(0f, 0f, LEVEL_0);   // after rotating, the Z-axis upside down

	mTouchSurface.draw(gl);
	gl.glPopMatrix();
    }

    public void jumpToRoomByX(int x) {
	int rooms = mWorld.getRoomNumber();
	int dest = (int)((rooms * x / Launcher.mDefaultWidth ) );
	if (dest != mWorld.getCurrentRoom()) {
	    Log.i(TAG,"Jump to "+dest);
	    mWorld.moveToRoom(dest);
	}
    }

    public void shiftWorldXY(int dx, int dy) {
	int current = mWorld.getCurrentRoom();
	float screenX = 3 * dx / PROJ_WIDTH;
	float level3X = convertToLevel(3, screenX);
	float endX = -1 * current * Room.WIDTH + level3X;
	float endY = 0;
	mWorld.setXY(endX, endY);

	int rooms = mWorld.getChildrenCount();
	float totalRoomWidth = Room.WIDTH * (rooms - 1);
	if (endX > 0 ||
		endX < -1 * totalRoomWidth) {
	    float barY = mBar.getY();
	    float level1X = convertToLevel(1, screenX);
	    mBar.setXY(level1X, barY);
	}
    }

    public void moveToOrigRoom() {
	this.moveToRoom(mWorld.getCurrentRoom());
    }

    public void moveToNextRoom() {
	this.moveToRoom(mWorld.getCurrentRoom() + 1);
    }

    public void moveToPrevRoom() {
	this.moveToRoom(mWorld.getCurrentRoom() - 1);
    }

    public void moveToRoom(int next) {
	mWorld.moveToRoom(next);
	mBar.backToCenter();
    }

    class WallRenderer implements GLSurfaceView.Renderer {
	private ViewManager mManager;

	public WallRenderer(ViewManager manager) {
	    mManager = manager;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

	    mTextureManager.setGLContext(gl);

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

	    mManager.generateTextures();
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
	    mScreenWidth  = w;
	    mScreenHeight = h;

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
