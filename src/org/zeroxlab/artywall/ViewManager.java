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

    public static boolean USE_ORTHO = false; // use Ortho or Frustum?
    public static float PROJ_LEFT   = -16f;
    public static float PROJ_RIGHT  = 16f;
    public static float PROJ_BOTTOM = -23f;
    public static float PROJ_TOP    = 23f;
    public static float PROJ_NEAR   = 3f;
    public static float PROJ_FAR    = 50f;

    public static float PROJ_WIDTH  = Math.abs(PROJ_RIGHT - PROJ_LEFT);
    public static float PROJ_HEIGHT = Math.abs(PROJ_TOP - PROJ_BOTTOM);

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

    private LinkedList<Room> mRooms;
    private World world;
    private Room room, room2, room3, room4, room5;

    GLObject wanted1;
    GLObject wanted2;
    GLObject wanted3;
    GLObject wanted4;
    GLObject wanted5;
    GLObject wanted6;
    GLObject wanted7;

    BottomBar mBar;
    GLObject elf1;
    GLObject elf2;
    GLObject elf3;
    GLObject elf4;

    private float ratio = 1.0f;
    public void updateRatio(float x, float y) {
	float temp = (1000 - y) / 1000;
	temp = Math.max(0.5f, temp);
	temp = Math.min(1.0f, temp);
	ratio = temp;
    }

    private boolean test = true;
    public void performClick(float screenX, float screenY) {
	Log.i(TAG,"click on screen x="+screenX+" y="+screenY);
	float nearX = PROJ_WIDTH  * screenX / mScreenWidth;
	float nearY = PROJ_HEIGHT * screenY / mScreenHeight;
	Log.i(TAG,"near x="+nearX+" y="+nearY);
	int id = world.pointerAt(nearX, nearY);
	Log.i(TAG,"Click on GLObject id = "+id);

	if (id > 1) {
	    GLObject obj = ObjectManager.getInstance().getGLObjectById(id);

	    GLFade fade = new GLFade(1000, 1f, 1f, 1f);
	    obj.setAnimation(fade);
	    mTimeline.addAnimation(fade);
	} else if (id == 1){
	    GLTransition trans = elf1.getTransition();
	    GLTransAni tr = new GLTransAni(trans);
	    elf1.setAnimation(tr);
	    mTimeline.addAnimation(tr);
	}
    }

    public static float convertToLevel(int level, float from) {
	if (level == 1) {
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

	mResourceManager = ResourcesManager.getInstance(mContext);

	mTextureManager  = TextureManager.getInstance();
	mTimeline        = Timeline.getInstance();
	mTimeline.monitor(mSurfaceView);

	mRooms = new LinkedList<Room>();
	initGLViews();
    }

    public void initGLViews() {
	room = new Room(0, "wall", "ground");
	room2 = new Room(1, "wall_2", "ground");
	room3 = new Room(2, "wall_3", "ground");
	room4 = new Room(3, "wall_4", "ground");
	room5 = new Room(4, "wall_5", "ground");

	elf1 = new GLObject(0, 0, 1, 1);
	elf2 = new GLObject(0, 0, 1, 1);
	elf3 = new GLObject(0, 0, 1, 1);
	elf4 = new GLObject(0, 0, 1, 1);

	elf1.setTextureName("zeroxdoll");
	elf2.setTextureName("android");
	elf3.setTextureName("beagle");
	elf4.setTextureName("flower");

	String name[] = new String[] {"ani_1"
	    , "ani_2", "ani_3", "ani_4", "ani_5"};
	long time[] = new long[] {2000
	    , 100, 100, 100, 100};

	GLTransition transition = new GLTransition(name, time);
	elf1.setTransition(transition);

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
	wanted1.setTextureName("luffy");
	wanted2.setTextureName("nami");
	wanted3.setTextureName("sanji");
	wanted4.setTextureName("robin");
	wanted5.setTextureName("buggy");
	wanted6.setTextureName("zoro");
	wanted7.setTextureName("bear");
	room.addItem(wanted1, 100f, 100f, 0f);
	room.addItem(wanted2, 150f, 10f, 15f);
	room.addItem(wanted3, 30f, 150f, 33f);
	room.addItem(wanted4, 150f, 210f, 3f);
	room2.addItem(wanted5, 10f, 10f, 20f);
	room2.addItem(wanted6, 220f, 150f, 180f);
	room3.addItem(wanted7, 130f, 30f, 30f);

	mRooms.add(room);
	mRooms.add(room2);
	mRooms.add(room3);
	mRooms.add(room4);
	mRooms.add(room5);

	world = new World();
	for (int i = 0; i< mRooms.size(); i++) {
	    world.addRoom(mRooms.get(i));
	}
    }

    public void generateTextures(GL10 gl) {
	mBar.generateTextures(gl
		, mResourceManager
		, mTextureManager);

	for (int i = 0; i< mRooms.size(); i++) {
	    mRooms.get(i).generateTextures(gl
		    , mResourceManager
		    , mTextureManager);
	}
    }

    public void drawGLViews(GL10 gl) {
	Log.i(TAG,"Redraw!!");
	GLObject obj;

	gl.glMatrixMode(gl.GL_MODELVIEW);

	gl.glLoadIdentity();
	gl.glScalef(ratio, ratio, ratio);

	/* the coordinate of OpenGL is different from normal computer system
	 * We may rotate the coordinate so we don't have to worry about that.
	 */
	gl.glRotatef(180f, 1f, 0f, 0f); // now the +x is heading for right
					//         +y is heading for bottom

	// Scaling the world, makes it looks smaller
	gl.glTranslatef(0f, 0f, 30f*(1-ratio));

	/* Draw Level 3, the Rooms of World */
	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(3, PROJ_LEFT)
		, convertToLevel(3, PROJ_BOTTOM )
		, 0f);// move to Left-Top

	gl.glTranslatef(0f, 0f, LEVEL_3);   // after rotating, the Z-axis upside down
	world.draw(gl);
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
    }

    public void jumpToRoomByX(int x) {
	int rooms = world.getRoomNumber();
	int dest = (int)((rooms * x / Launcher.mDefaultWidth ) );
	if (dest != world.getCurrentRoom()) {
	    Log.i(TAG,"Jump to "+dest);
	    world.moveToRoom(dest);
	}
    }

    public void shiftWorldXY(int dx, int dy) {
	int current = world.getCurrentRoom();
	float screenX = 3 * dx / PROJ_WIDTH;
	screenX = convertToLevel(3, screenX);
	float endX = -1 * current * Room.WIDTH + screenX;
	float endY = 0;
	world.setXY(endX, endY);
    }

    public void moveToOrigRoom() {
	this.moveToRoom(world.getCurrentRoom());
    }

    public void moveToNextRoom() {
	this.moveToRoom(world.getCurrentRoom() + 1);
    }

    public void moveToPrevRoom() {
	this.moveToRoom(world.getCurrentRoom() - 1);
    }

    public void moveToRoom(int next) {
	world.moveToRoom(next);
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

