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

package org.zeroxlab.fhomee;

import android.content.Intent;
import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ViewManager {

    public static boolean USE_ORTHO = false; // use Ortho or Frustum?
    public static float PROJ_LEFT = -16f;
    public static float PROJ_RIGHT = 16f;
    public static float PROJ_BOTTOM = -23f;
    public static float PROJ_TOP = 23f;
    public static float PROJ_NEAR = 3f;
    public static float PROJ_FAR = 50f;

    public static float PROJ_WIDTH = Math.abs(PROJ_RIGHT - PROJ_LEFT);
    public static float PROJ_HEIGHT = Math.abs(PROJ_TOP - PROJ_BOTTOM);

    public static float BAR_HEIGHT_RATIO = 0.15f;

    public final static int LEVEL_TOUCH = 0;
    public final static int LEVEL_BAR = 1;
    public final static int LEVEL_POSTER = 2;
    public final static int LEVEL_WORLD = 3;

    public static float LEVEL_0 = 4f;  // TouchSurface
    public static float LEVEL_1 = 20f; // Pet
    public static float LEVEL_2 = 28f; // poster
    public static float LEVEL_3 = 30f; // wall
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
    public static float ZN_LEVEL_0 = LEVEL_0 / PROJ_NEAR;
    public static float ZN_LEVEL_1 = LEVEL_1 / PROJ_NEAR;
    public static float ZN_LEVEL_2 = LEVEL_2 / PROJ_NEAR;
    public static float ZN_LEVEL_3 = LEVEL_3 / PROJ_NEAR;

    public static int mScreenWidth;
    public static int mScreenHeight;

    /* convert Screen Location into Near surface*/
    private static PointF mNearPoint;
    /* convert Near location into specific Level */
    private static PointF mLevelPoint;

    private int mDownId;
    private int mUpId;
    private int mPressId;
    private int mReleaseId;

    private static final String TAG = "ViewManager";
    private static ViewManager mViewMgr;
    private Rect mViewPort;
    private static GLSurfaceView mSurfaceView;
    private WallRenderer mRenderer;
    private Timeline mTimeline;
    private ResourcesManager mResourceManager;
    private TextureManager mTextureManager;
    private GestureManager mGestureMgr;

    private boolean mMiniMode = false;

    private TouchSurface mTouchSurface;
    private EditSurface mEditSurface;
    private World mWorld;
    private Room room1, room2, room3, room4, room5, room6;

    public final float FAREST_PUSHING_DEPTH = 10f;

    Layer mWorldLayer;
    Layer mBarLayer;
    Layer mEditLayer;

    boolean mGLViewsCreated = false;
    Poster wanted1;
    Poster wanted2;
    Poster wanted3;
    Poster wanted4;
    Poster wanted5;
    Poster wanted6;
    Poster wanted7;

    Poster demo01;
    Poster demo02;
    Poster demo03;
    Poster demo04;
    Poster demo05;
    Poster demo06;
    Poster demo07;
    Poster demo08;
    Poster demo09;
    Poster demo10;
    Poster demo11;

    Pet pet01;
    Pet pet02;
    Pet pet03;

    public static TopBar mTopBar;
    public static PetBar mPetBar;
    public static Dock mDock;

    /**
     * This method was called while user Pressing the screen.
     */
    public void onPress(int screenX, int screenY, MotionEvent event) {
        getNearLocation(mNearPoint, screenX, screenY);

        if (mEditSurface.isEditing()) {
            mEditLayer.onPressEvent(mNearPoint, event, mEditSurface);
            mPressId = mEditLayer.getIdContains(mNearPoint, mEditSurface);
        } else if (mMiniMode == true) {
            mBarLayer.onPressEvent(mNearPoint, event, mDock);
            mPressId = mBarLayer.getIdContains(mNearPoint, mDock);
        } else {
            mPressId = mBarLayer.getIdContains(mNearPoint, mPetBar);
            mBarLayer.onPressEvent(mNearPoint, event, mPetBar);
            if (mPressId == -1) {
                mPressId = mWorldLayer.getIdContains(mNearPoint);
            }
        }
    }

    /**
     * This method was called while user Leaving the screen.
     */
    public void onRelease(int screenX, int screenY, MotionEvent event) {
        getNearLocation(mNearPoint, screenX, screenY);

        if (mEditSurface.isEditing()) {
            mEditLayer.onReleaseEvent(mNearPoint, event, mEditSurface);
            mReleaseId = mEditLayer.getIdContains(mNearPoint, mEditSurface);
        } else if (mMiniMode == true) {
            mBarLayer.onReleaseEvent(mNearPoint, event, mDock);
            mReleaseId = mBarLayer.getIdContains(mNearPoint, mDock);
        } else {
            mWorldLayer.onReleaseEvent(mNearPoint, event, mWorld);
            mBarLayer.onReleaseEvent(mNearPoint, event, mPetBar);
            mReleaseId = mBarLayer.getIdContains(mNearPoint, mPetBar);
            if (mReleaseId == -1) {
                mReleaseId = mWorldLayer.getIdContains(mNearPoint);
            }
        }

        if (mPressId != -1 && mReleaseId == mPressId && !mGestureMgr.mIsDragging) {
            GLObject obj = ObjectManager.getInstance().getGLObjectById(mPressId);
            obj.onClick();
        }
    }

    /**
     * This method was called while user Moving on the screen.
     */
    public void onMove(int screenX, int screenY, MotionEvent event) {
        getNearLocation(mNearPoint, screenX, screenY);

        if (mEditSurface.isEditing()) {
            mEditLayer.onDragEvent(mNearPoint, event, mEditSurface);
            return;
        }

        if (mMiniMode == true) {
            if (mGestureMgr.mIsVDrag) {
                return;
            }
            if (mBarLayer.getIdContains(mNearPoint, mDock) != -1) {
                mBarLayer.onDragEvent(mNearPoint, event, mDock);
            } else {
                mDock.bumpObjects(-1);
            }
        } else {
            if (mGestureMgr.mIsHDrag) {
                getLevelLocation(LEVEL_WORLD, mLevelPoint, mNearPoint.x, mNearPoint.y);
                mWorldLayer.onDragEvent(mNearPoint, event, mWorld);
                mBarLayer.onDragEvent(mNearPoint, event, mPetBar);
            }
        }
    }

    public void onLongPressing(int screenX, int screenY, MotionEvent event) {
        getNearLocation(mNearPoint, screenX, screenY);
        if (mEditSurface.isEditing()) {
            mEditLayer.onLongPressEvent(mNearPoint, event, mEditSurface);
        } else if (mMiniMode == true) {
            mBarLayer.onLongPressEvent(mNearPoint, event, mDock);
        } else {
            int id = mBarLayer.getIdContains(mNearPoint, mPetBar);
            if (id == -1) {
                mWorldLayer.onLongPressEvent(mNearPoint, event, mWorld);
            } else {
                mBarLayer.onLongPressEvent(mNearPoint, event, mPetBar);
            }
        }
    }

    /* Convert location from Screen to Near and store in PointF near */
    private void getNearLocation(PointF near, int screenX, int screenY) {
        near.x = PROJ_WIDTH * screenX / mScreenWidth;
        near.y = PROJ_HEIGHT * screenY / mScreenHeight;
    }

    /* Convert location from Near to Level */
    private void getLevelLocation(int level, PointF levelP, float nearX, float nearY) {
        levelP.x = convertToLevel(level, nearX);
        levelP.y = convertToLevel(level, nearY);
    }

    public void turnOnMiniMode() {
        if (mEditSurface.isEditing()) {
            /* no mini mode when editing */
            return;
        }
        mMiniMode = true;
        mDock.setVisible(true);
        mDock.setChildrenVisible(true);
        mTopBar.setVisible(true);
        mTopBar.setChildrenVisible(true);
        mPetBar.setVisible(false);
        mPetBar.setChildrenVisible(false);
        mWorld.setMiniMode();
    }

    public void turnOffMiniMode() {
        mMiniMode = false;
        mDock.setVisible(false);
        mDock.setChildrenVisible(false);
        mTopBar.setVisible(false);
        mTopBar.setChildrenVisible(false);
        mPetBar.setVisible(true);
        mPetBar.setChildrenVisible(true);
        mWorld.setNormalMode();
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

    public static void setSurface(GLSurfaceView surface) {
        mSurfaceView = surface;
        mViewMgr = null; // reset
    }

    public void editPoster(Poster poster) {
        mEditSurface.edit(poster);
        mEditLayer.measure();
    }

    public void addPosterToCurrentRoom(Poster poster) {
        mWorld.addPoster(poster, 0, 0);
        mWorldLayer.measure();
    }

    public void addPet(Pet pet) {
        mPetBar.addPet(pet);
    }

    private ViewManager() {
        mRenderer = new WallRenderer();
        mSurfaceView.setEGLConfigChooser(false);
        mSurfaceView.setRenderer(mRenderer);

        mResourceManager = ResourcesManager.getInstance();
        mTextureManager = TextureManager.getInstance();
        mTimeline = Timeline.getInstance();
        mTimeline.monitor(mSurfaceView);
        mGestureMgr = GestureManager.getInstance();

        mNearPoint = new PointF();
        mLevelPoint = new PointF();

        Layer.sProjNear = PROJ_NEAR;
        Layer.sProjLeft = PROJ_LEFT;
        Layer.sProjBottom = PROJ_BOTTOM;
        Layer.sProjNearWidth = PROJ_WIDTH;
        Layer.sProjNearHeight = PROJ_HEIGHT;

        mWorldLayer = new Layer(LEVEL_3);
        mBarLayer = new Layer(LEVEL_1);
        mEditLayer = new Layer(LEVEL_0);
    }

    public static ViewManager getInstance() {
        if (mViewMgr != null) {
            return mViewMgr;
        }

        if (mSurfaceView == null) {
            Log.i(TAG, "OOOOps..you should set Surface before getInstance");
        }

        mViewMgr = new ViewManager();
        return mViewMgr;
    }

    public void initGLViews() {
        room1 = new Room("lohas_wall", "lohas_ground");
        room2 = new Room("lohas_wall", "lohas_ground");
        room3 = new Room("lohas_wall", "lohas_ground");
        room4 = new Room("lohas_wall", "lohas_ground");
        room5 = new Room("lohas_wall", "lohas_ground");
        room6 = new Room("lohas_wall", "lohas_ground");

        float barWidth = convertToLevel(LEVEL_BAR, PROJ_WIDTH);
        float barHeight = convertToLevel(LEVEL_BAR, PROJ_HEIGHT * BAR_HEIGHT_RATIO);
        mPetBar = new PetBar(barWidth, barHeight);
        mPetBar.setXY(0, convertToLevel(LEVEL_BAR, PROJ_HEIGHT * 0.85f));

        mTopBar = new TopBar(barWidth, barHeight, "topbar_background");
        mTopBar.setXY(0, 0);

        wanted1 = new Poster(100, 120, "luffy");
        wanted2 = new Poster(100, 100, "nami");
        wanted3 = new Poster(100, 100, "sanji");
        wanted4 = new Poster(100, 100, "robin");
        wanted5 = new Poster(150, 100, "buggy");
        wanted6 = new Poster(100, 100, "zoro");
        wanted7 = new Poster(250, 250, "bear");
        room4.addPoster(wanted1, 100f, 100f);
        room4.addPoster(wanted2, 150f, 10f);
        room5.addPoster(wanted3, 30f, 150f);
        room5.addPoster(wanted4, 150f, 210f);
        room6.addPoster(wanted5, 10f, 10f);
        room6.addPoster(wanted6, 220f, 150f);
        room6.addPoster(wanted7, 130f, 30f);

        demo01 = new Poster(183f, 181f, "shelf");
        demo02 = new Poster(123f, 185f, "window");
        demo03 = new Poster(100f, 120f, "plant");
        demo04 = new Poster(120f, 140f, "paint");
        demo05 = new Poster(75f, 75f, "clock");
        demo06 = new Poster(94f, 78f, "picture");
        demo07 = new Poster(67f, 54f, "frame");
        demo08 = new Poster(256f, 128f, "desk");
        demo09 = new Poster(86f, 156f, "light");
        demo10 = new Poster(123f, 188f, "window");
        demo11 = new Poster(240f, 125f, "sofa");
        room1.addPoster(demo01, 34f, 230f);
        room1.addPoster(demo02, 160f, 30f);
        room1.addPoster(demo03, 200f, 292f);
        room2.addPoster(demo04, 32f, 27f);
        room2.addPoster(demo05, 205f, 70f);
        room2.addPoster(demo06, 90f, 194f);
        room2.addPoster(demo07, 212f, 230f);
        room2.addPoster(demo08, 64f, 286f);
        room3.addPoster(demo09, 17f, 0f);
        room3.addPoster(demo10, 160f, 30f);
        room3.addPoster(demo11, 19f, 288f);

        Poster p1 = new Poster(10f, 10f, "icon_phone");
        Poster p2 = new Poster(10f, 10f, "icon_camera");
        Poster p3 = new Poster(10f, 10f, "icon_music");
        pet01 = new Pet(p1);
        pet02 = new Pet(p2);
        pet03 = new Pet(p3);
        mPetBar.addPet(pet01);
        mPetBar.addPet(pet02);
        mPetBar.addPet(pet03);

        Invoker invoker01 = new Invoker("com.android.contacts", "com.android.contacts.DialtactsActivity");
        p1.setInvoker(invoker01);
        Invoker invoker02 = new Invoker("com.google.android.music", "com.android.music.AlbumBrowserActivity");
        p3.setInvoker(invoker02);
        Invoker invoker03 = new Invoker("com.google.android.deskclock", "com.android.deskclock.DeskClock");
        demo05.setInvoker(invoker03);

        Invoker invoker04 = new Invoker(new Intent(Intent.ACTION_DIAL));
        demo02.setInvoker(invoker04);

        mWorld = new World();
        mWorld.addRoom(room1);
        mWorld.addRoom(room2);
        mWorld.addRoom(room3);
        mWorld.addRoom(room4);
        mWorld.addRoom(room5);
        mWorld.addRoom(room6);

        mPetBar.setWorld(mWorld);

        mDock = new Dock(barWidth, barHeight, "topbar_background");
        mDock.setXY(0, convertToLevel(LEVEL_BAR, PROJ_HEIGHT * 0.85f));
        mDock.readThumbnails(mWorld);

        mWorldLayer.addChild(mWorld, true);
        mBarLayer.addChild(mDock, true);
        mBarLayer.addChild(mPetBar, true);
        mBarLayer.addChild(mTopBar, false);
        turnOffMiniMode();
        mTouchSurface = new TouchSurface();
        mEditSurface = new EditSurface();
        mEditSurface.resize(mScreenWidth, mScreenHeight);
        mEditLayer.addChild(mEditSurface, true);
        mEditLayer.measure();

        /* Do not create GLViews twice */
        mGLViewsCreated = true;
    }

    private void drawTouchSurface(GL10 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(
                convertToLevel(LEVEL_TOUCH, PROJ_LEFT)
                , convertToLevel(LEVEL_TOUCH, PROJ_BOTTOM)
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

        mWorldLayer.onDraw(gl);

	/* Draw Level 1, the Bar and Pets */
        mBarLayer.onDraw(gl);

        mEditLayer.onDraw(gl);
	/* Draw the TouchSurface */
        drawTouchSurface(gl);
    }

    class WallRenderer implements GLSurfaceView.Renderer {

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            mTextureManager.setGLContext(gl);

            String version = gl.glGetString(gl.GL_VERSION);
            String test = gl.glGetString(gl.GL_EXTENSIONS);
            String result[] = test.split(" ");
            Log.i(TAG, "Version: " + version);
            for (int i = 0; i < result.length; i++) {
                Log.i(TAG, "Extension: " + result[i]);
            }

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
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            mScreenWidth = w;
            mScreenHeight = h;

            Layer.sRatioH = PROJ_WIDTH / mScreenWidth;
            Layer.sRatioV = PROJ_HEIGHT / mScreenHeight;

            if (mEditSurface != null) {
                mEditSurface.resize(mScreenWidth, mScreenHeight);
                mEditLayer.measure();
            }

            GestureManager.getInstance().updateScreenSize(mScreenWidth, mScreenHeight);

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

            if (!mGLViewsCreated) {
                initGLViews();
            }
        }

        public void onDrawFrame(GL10 gl) {
            gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                    GL10.GL_MODULATE);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            if (mTextureManager.hasPendingTextures()) {
                mTextureManager.updateTexture();
            }

            drawGLViews(gl);
        }
    }
}

