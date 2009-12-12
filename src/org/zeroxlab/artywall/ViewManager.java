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

    private GLView view1;

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
    }

    public void initGLViews(GL10 gl) {
	/*
	view1 = new GLView(mContext);
	String imgName = "robot";
	ResourcesManager resManager = ResourcesManager.getInstance(mContext);
	TextureManager manager = TextureManager.getInstance();
	Bitmap bitmap = resManager.getBitmapByName(imgName);
	int id = manager.generateOneTexture(gl, bitmap, imgName);
	view1.setTextureID(id);

	RectF rect = new RectF(0f, 0f, 2.5f, 2.5f);
	view1.setSize(rect);
	*/
    }

    public void drawGLViews(GL10 gl) {
	gl.glLoadIdentity();
	gl.glTranslatef(-0.0f, 0.5f, -4.0f);
	view1.drawGLView(gl);
    }

    class WallRenderer implements GLSurfaceView.Renderer {
	private ViewManager mManager;

	public WallRenderer(ViewManager manager) {
	    mManager = manager;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	    gl.glDisable(GL10.GL_DITHER);
	    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
		    GL10.GL_FASTEST);
	    gl.glClearColor(.5f, .1f, .1f, 1);
	    gl.glShadeModel(GL10.GL_SMOOTH);
	    gl.glEnable(GL10.GL_DEPTH_TEST);
	    gl.glEnable(GL10.GL_TEXTURE_2D);

	    mManager.initGLViews(gl);
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
	    gl.glViewport(0, 0, w, h);
	/*    float ratio = (float) w / h;
	    gl.glMatrixMode(GL10.GL_PROJECTION);
	    gl.glLoadIdentity();
	    gl.glFrustumf(-ratio-1, ratio+1, -2f, 2f, 0.5f, 10f);
	    Log.i(TAG,"Set frustumf to: ratio="+ ratio);
*/
	}

	public void onDrawFrame(GL10 gl) {
       IntBuffer   mVertexBuffer;
        IntBuffer   mColorBuffer;
        ByteBuffer  mIndexBuffer;

           int one = 0x10000;
            /* Every vertex got 3 values, for
             * x / y / z position in the kartesian space.
             */
            int vertices[] = { 
                -one, -one, -one,
                one, -one, -one,
                one,  one, -one,
                -one,  one, -one,
                -one, -one,  one,
                one, -one,  one,
                one,  one,  one,
                -one,  one,  one,
            };  
            int colors[] = { 
             0,    0,    0,  one,
             one,    0,    0,  one,
             one,  one,    0,  one,
             0,  one,    0,  one,
             0,    0,  one,  one,
             one,    0,  one,  one,
             one,  one,  one,  one,
             0,  one,  one,  one,
             };
           byte indices[] = {
                0, 4, 5,
                0, 5, 1,
                1, 5, 6,
                1, 6, 2,
                2, 6, 7,
                2, 7, 3,
                3, 7, 4,
                3, 4, 0,
                4, 7, 6
            };
           ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
            vbb.order(ByteOrder.nativeOrder());
            mVertexBuffer = vbb.asIntBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);
            ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
            cbb.order(ByteOrder.nativeOrder());
            mColorBuffer = cbb.asIntBuffer();
            mColorBuffer.put(colors);
            mColorBuffer.position(0);
            mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
            mIndexBuffer.put(indices);
            mIndexBuffer.position(0);

	    gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		    GL10.GL_MODULATE);
	    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
int w=200, h=200;
gl.glViewport(0, 0, w, h);

       float ratio = (float)w / h;
       gl.glMatrixMode(gl.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 2, 12);

gl.glDisable(gl.GL_DITHER);

        gl.glMatrixMode(gl.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -3.0f-3);
        //gl.glScalef(0.5f, 0.5f, 0.5f);
        gl.glRotatef(mX, 0, 1, 0);
        gl.glRotatef(mY, 0, 0, 1);
        gl.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);

	    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	    gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
	    gl.glEnable(gl.GL_CULL_FACE);
	
		gl.glFrontFace(gl.GL_CW);
           gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer);
            gl.glColorPointer(4, gl.GL_FIXED, 0, mColorBuffer);
            gl.glDrawElements(gl.GL_TRIANGLES, 3, gl.GL_UNSIGNED_BYTE, mIndexBuffer);

	    gl.glLoadIdentity();
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

