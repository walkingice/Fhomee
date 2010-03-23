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

    public static float BAR_HEIGHT_RATIO = 0.15f;

    public final static int LEVEL_TOUCH  = 0;
    public final static int LEVEL_BAR    = 1;
    public final static int LEVEL_POSTER = 2;
    public final static int LEVEL_WORLD  = 3;

    public static float LEVEL_0     = 4f;  // TouchSurface
    public static float LEVEL_1     = 20f; // Pet
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

    private int mDownId;
    private int mUpId;
    private int mPressId;
    private int mReleaseId;

    final String TAG="ViewManager";
    private Context mContext;
    private Rect mViewPort;
    private GLSurfaceView mSurfaceView;
    private WallRenderer  mRenderer;
    private Timeline mTimeline;
    private ResourcesManager mResourceManager;
    private TextureManager   mTextureManager;
    private GestureManager   mGestureMgr;

    private boolean mMiniMode = false;

    private TouchSurface mTouchSurface;
    private World mWorld;
    private Room room, room2, room3, room4, room5, room6;

    public final float FAREST_PUSHING_DEPTH = 10f;

    GLObject wanted1;
    GLObject wanted2;
    GLObject wanted3;
    GLObject wanted4;
    GLObject wanted5;
    GLObject wanted6;
    GLObject wanted7;

    GLString mFont;
    String   mFontString;
    int mFontId;

    public static TopBar mTopBar;
    public static BottomBar mBottomBar;
    public static Dock mDock;
    /**
     * This method was called while user Pressing the screen.
     */
    public void onPress(int screenX, int screenY) {
	if (mMiniMode == true) {
	    mPressId = getObjectIdOfBar(screenX, screenY);
	    if (mPressId == mDock.getId()) {
		float nearX = PROJ_WIDTH * screenX / mScreenWidth;
		int levelX  = (int)convertToLevel(LEVEL_BAR, nearX);
		mDock.press(levelX);
	    }
	} else {
	    mPressId = getObjectIdOfWorld(screenX, screenY);
	}
    }

    /**
     * This method was called while user Leaving the screen.
     */
    public void onRelease(int screenX, int screenY) {
	mDock.bumpObjects(-1);

	if (mMiniMode == true) {
	    mReleaseId = getObjectIdOfBar(screenX, screenY);
	    if (mReleaseId == -1 && mGestureMgr.mIsVDrag) {
		turnOffMiniMode();
	    } else if (mReleaseId == mDock.getId()) {
		float nearX = PROJ_WIDTH * screenX / mScreenWidth;
		int levelX  = (int)convertToLevel(LEVEL_BAR, nearX);
		mDock.release(levelX);
	    }
	} else {
	    if (mGestureMgr.mSnapToNext) {
		moveToNextRoom();
	    } else if (mGestureMgr.mSnapToPrev) {
		moveToPrevRoom();
	    } else {
		moveToOrigRoom();
	    }
	    mReleaseId = getObjectIdOfWorld(screenX, screenY);
	}

	if (mPressId != -1 && mReleaseId == mPressId) {
	    GLObject obj = ObjectManager.getInstance().getGLObjectById(mPressId);
	    obj.onClick();

	    if (mReleaseId == mDock.getId() && !mGestureMgr.mIsLongClick) {
		int next = mDock.getSelectedRoom();
		if (next != -1) {
		    moveToRoom(next);
		    turnOffMiniMode();
		}
	    }
	}
    }

    /**
     * This method was called while user Moving on the screen.
     */
    public void onMove(int screenX, int screenY) {
	if(mGestureMgr.mModeChange) {
	    if (mGestureMgr.mMiniMode) {
		turnOnMiniMode();
	    } else {
		turnOffMiniMode();
	    }
	    return;
	}

	if (mMiniMode == true) {
	    if (mGestureMgr.mIsVDrag) {
		return;
	    }
	    float nearX = PROJ_WIDTH  * screenX / mScreenWidth;
	    float nearY = PROJ_HEIGHT * screenY / mScreenHeight;
	    int levelX  = (int)convertToLevel(LEVEL_BAR, nearX);
	    int levelY  = (int)convertToLevel(LEVEL_BAR, nearY);
	    if (mDock.contains(levelX, levelY)) {
		mDock.bumpObjects(levelX);
	    } else {
		mDock.bumpObjects(-1);
	    }
	} else {
	    if (mGestureMgr.mIsHDrag) {
		shiftWorldXY(mGestureMgr.getDeltaX(), 0);
	    } else if (mGestureMgr.mIsLongClick) {
		// do nothing yet
	    }
	}
    }

    private int getObjectIdOfBar(int screenX, int screenY) {
	float nearX = PROJ_WIDTH  * screenX / mScreenWidth;
	float nearY = PROJ_HEIGHT * screenY / mScreenHeight;
	float levelX = convertToLevel(LEVEL_BAR, nearX);
	float levelY = convertToLevel(LEVEL_BAR, nearY);

	if (mDock.contains(levelX, levelY)) {
	    return mDock.getId();
	}

	if (mTopBar.contains(levelX, levelY)) {
	    return mTopBar.pointerAt(levelX, levelY);
	}

	return -1;
    }

    private int getObjectIdOfWorld(int screenX, int screenY) {
	float nearX = PROJ_WIDTH  * screenX / mScreenWidth;
	float nearY = PROJ_HEIGHT * screenY / mScreenHeight;

	int id = mBottomBar.pointerAt(convertToLevel(LEVEL_BAR, nearX)
		, convertToLevel(LEVEL_BAR, nearY));
	if (id == -1) {
	    float worldX = convertToLevel(LEVEL_WORLD, nearX);
	    float worldY = convertToLevel(LEVEL_WORLD, nearY);
	    id = mWorld.pointerAt(worldX, worldY);
	}

	return id;
    }

    public void turnOnMiniMode() {
	if (mMiniMode == true) {
	    return;
	} else {
	    mMiniMode = true;
	    mWorld.setMiniMode();
	}
    }

    public void turnOffMiniMode() {
	if (mMiniMode == false) {
	    return;
	} else {
	    mMiniMode = false;
	    mWorld.setNormalMode();
	}
    }

    /**
     * Get the Z value of the specified level
     */
    public static float getZDepth(int level) {
	if (level == LEVEL_TOUCH) {
	    return LEVEL_0;
	} else if (level == LEVEL_BAR) {
	    return LEVEL_1;
	} else if (level == LEVEL_POSTER) {
	    return LEVEL_2;
	} else if (level == LEVEL_WORLD) {
	    return LEVEL_3;
	}

	return LEVEL_0;
    }

    /**
     * Convert the coordinate from Near surface to specified level
     */
    public static float convertToLevel(int level, float near) {
	if (level == 0) {
	    return near * ZN_LEVEL_0;
	} else if (level == 1) {
	    return near * ZN_LEVEL_1;
	} else if (level == 2) {
	    return near * ZN_LEVEL_2;
	} else if (level == 3) {
	    return near * ZN_LEVEL_3;
	}

	return near;
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
	mGestureMgr      = GestureManager.getInstance();

	initGLViews();
    }

    public void initGLViews() {
	room = new Room("wall_6", "ground_2");
	room2 = new Room("wall_2", "ground");
	room3 = new Room("wall_3", "ground");
	room4 = new Room("wall_4", "ground");
	room5 = new Room("wall_5", "ground");
	room6 = new Room("wall", "ground");

	float barWidth  = convertToLevel(LEVEL_BAR, PROJ_WIDTH);
	float barHeight = convertToLevel(LEVEL_BAR, PROJ_HEIGHT * BAR_HEIGHT_RATIO);
	mBottomBar = new BottomBar(barWidth, barHeight);
	mBottomBar.setXY(0, convertToLevel(LEVEL_BAR, PROJ_HEIGHT * 0.85f));

	mTopBar = new TopBar(barWidth, barHeight, "topbar_background");
	mTopBar.setXY(0, 0);

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

	mFont = new GLString("I like Super Mario");
	mFont.setColor(0x11FF33);
	mFont.setAlpha(0x99);
	mFont.setSize(300f, 80f);
	room.addItem(mFont, 20f, 60f, 10f);

	mWorld = new World();
	mWorld.addRoom(room);
	mWorld.addRoom(room2);
	mWorld.addRoom(room3);
	mWorld.addRoom(room4);
	mWorld.addRoom(room5);
	mWorld.addRoom(room6);

	mDock = new Dock(barWidth, barHeight, "topbar_background");
	mDock.setXY(0, convertToLevel(LEVEL_BAR, PROJ_HEIGHT * 0.85f));
	mDock.readThumbnails(mWorld);

	mTouchSurface = new TouchSurface();
    }

    public void generateTextures() {
	mTouchSurface.generateTextures();
	mTopBar.generateTextures();
	mBottomBar.generateTextures();
	mWorld.generateTextures();
	mDock.generateTextures();
    }

    private void drawDock(GL10 gl) {
	/* only draw Dock at mini mode*/
	if (!mMiniMode) {
	    return;
	}

	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(LEVEL_BAR, PROJ_LEFT)
		, convertToLevel(LEVEL_BAR, PROJ_BOTTOM)
		, 0f);// move to Left-Top

	gl.glTranslatef(0f, 0f, LEVEL_1);   // after rotating, the Z-axis upside down

	mDock.draw(gl);
	gl.glPopMatrix();
    }

    private void drawTopBar(GL10 gl) {
	/* only draw Top Bar at mini mode*/
	if (!mMiniMode) {
	    return;
	}

	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(LEVEL_BAR, PROJ_LEFT)
		, convertToLevel(LEVEL_BAR, PROJ_BOTTOM)
		, 0f);// move to Left-Top

	gl.glTranslatef(0f, 0f, LEVEL_1);   // after rotating, the Z-axis upside down

	mTopBar.draw(gl);
	gl.glPopMatrix();
    }

    private void drawBottomBar(GL10 gl) {
	/* We do not draw Bottom Bar at Mini mode */
	if (mMiniMode) {
	    return;
	}

	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(LEVEL_BAR, PROJ_LEFT)
		, convertToLevel(LEVEL_BAR, PROJ_BOTTOM )
		, 0f);// move to Left-Top

	gl.glTranslatef(0f, 0f, LEVEL_1);   // after rotating, the Z-axis upside down

	mBottomBar.draw(gl);
	gl.glPopMatrix();
    }

    private void drawTouchSurface(GL10 gl) {
	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(LEVEL_TOUCH, PROJ_LEFT)
		, convertToLevel(LEVEL_TOUCH, PROJ_BOTTOM )
		, 0f);// move to Left-Top
	gl.glTranslatef(0f, 0f, LEVEL_0);   // after rotating, the Z-axis upside down

	mTouchSurface.draw(gl);
	gl.glPopMatrix();
    }

    public void drawGLViews(GL10 gl) {
	GLObject obj;

	gl.glMatrixMode(gl.GL_MODELVIEW);

	gl.glLoadIdentity();

	/* the coordinate of OpenGL is different from normal computer system
	 * We may rotate the coordinate so we don't have to worry about that.
	 */
	gl.glRotatef(180f, 1f, 0f, 0f); // now the +x is heading for right
					//         +y is heading for bottom

	/* Draw Level 3, the Rooms of World */
	gl.glPushMatrix();
	gl.glTranslatef(
		convertToLevel(LEVEL_WORLD, PROJ_LEFT)
		, convertToLevel(LEVEL_WORLD, PROJ_BOTTOM )
		, 0f);// move to Left-Top

	gl.glTranslatef(0f, 0f, LEVEL_3);   // after rotating, the Z-axis upside down
	if (mMiniMode) {
	    gl.glTranslatef(0f, 0f, World.MINIMODE_DEPTH_OFFSET);
	}
	mWorld.draw(gl);
	gl.glPopMatrix();

	/* Draw Level 1, the Bar and Pets */
	drawTopBar(gl);
	drawDock(gl);
	drawBottomBar(gl);

	/* Draw the TouchSurface */
	drawTouchSurface(gl);
    }

    public void shiftWorldXY(int dx, int dy) {
	int current = mWorld.getCurrentRoom();
	int rooms = mWorld.getChildrenCount();
	float screenX = 3 * dx / PROJ_WIDTH;

	/* At leftest room and slide to right
	   Or at rightest romm and slide to left */
	if ((current == 0 && dx > 0)
		|| (current == rooms - 1 && dx < 0)) {
	    screenX = screenX / 3 ;
	}

	float level3X = convertToLevel(LEVEL_WORLD, screenX);
	float endX = -1 * current * Room.WIDTH + level3X;
	float endY = 0;

	mWorld.setXY(endX, endY);

	float totalRoomWidth = Room.WIDTH * (rooms - 1);
	if (endX > 0 ||
		endX < -1 * totalRoomWidth) {
	    float barY = mBottomBar.getY();
	    float level1X = convertToLevel(LEVEL_BAR, screenX);
	    mBottomBar.setXY(level1X, barY);
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
	mBottomBar.backToCenter();
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

