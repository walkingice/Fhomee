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
	mSurfaceView.setRenderer(mRenderer);
    }

    class WallRenderer implements GLSurfaceView.Renderer {
	private Context mContext;
	private int mTextureID;
	public WallRenderer(Context context) {
	    mContext = context;
	}
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	    gl.glDisable(GL10.GL_DITHER);
	    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
		    GL10.GL_FASTEST);
	    gl.glClearColor(.5f, .1f, .1f, 1);
	    gl.glShadeModel(GL10.GL_SMOOTH);
	    gl.glEnable(GL10.GL_DEPTH_TEST);
	    gl.glEnable(GL10.GL_TEXTURE_2D);
	    int[] textures = new int[1];
	    gl.glGenTextures(1, textures, 0);
	    mTextureID = textures[0];
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
		    GL10.GL_NEAREST);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D,
		    GL10.GL_TEXTURE_MAG_FILTER,
		    GL10.GL_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
		    GL10.GL_CLAMP_TO_EDGE);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
		    GL10.GL_CLAMP_TO_EDGE);
	    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		    GL10.GL_REPLACE);
	    /*
	    InputStream is = mContext.getResources()
		.openRawResource(R.drawable.robot);
	    Bitmap bitmap;
	    try { 
		bitmap = BitmapFactory.decodeStream(is);
	    } finally {
		try {
		    is.close();
		} catch(IOException e) {
		    // ignore
		}
	    }
	    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	    bitmap.recycle();
	    */
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
	    gl.glViewport(0, 0, w, h);
	}

	public void onDrawFrame(GL10 gl) {
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

