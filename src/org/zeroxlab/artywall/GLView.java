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

import android.graphics.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GLView {

    final static String TAG = "GLView";
    final static int VERTEX_DIMENSION  = 3;
    final static int TEXTURE_DIMENSION = 2;

    private boolean visible = true;
    private float updateRate = 1f;
    private Context mContext;

    private int mVertexNum = 3;
    private ShortBuffer mIndexBuf;
    private FloatBuffer mVertexBuf;
    private FloatBuffer mTextureBuf;
    private FloatBuffer mColorBuf;

    private int mTextureID;

    RectF mArea;// quick hack, it supposed to be 4 arbitrary points
		// but not a Rectangle. Maybe a GLView will compose
		// arbitrary triangle in the feature...hope so.

    public GLView(Context context)  {
	mContext = context;

	ByteBuffer index;
	ByteBuffer vertex;
	ByteBuffer texture;
	ByteBuffer color;
	// 1short = 2byte, 1float = 4 byte
	index   = ByteBuffer.allocateDirect(3 * 2);
	vertex  = ByteBuffer.allocateDirect(mVertexNum * VERTEX_DIMENSION * 4);
	texture = ByteBuffer.allocateDirect(4 * TEXTURE_DIMENSION * 4);
	color   = ByteBuffer.allocateDirect(mVertexNum * 4 * 4);

	mIndexBuf   = index.asShortBuffer();
	mVertexBuf  = vertex.asFloatBuffer();
	mTextureBuf = texture.asFloatBuffer();
	mColorBuf   = color.asFloatBuffer();

	mIndexBuf.position(0);
	mIndexBuf.put((short)0);
	mIndexBuf.put((short)1);
	mIndexBuf.put((short)2);
	mIndexBuf.position(0);

	mTextureBuf.position(0);
	mTextureBuf.put(0f); // (0, 0)
	mTextureBuf.put(0f);
	mTextureBuf.put(1f); // (1, 0)
	mTextureBuf.put(0f);
	mTextureBuf.put(1f); // (1, 1)
	mTextureBuf.put(1f);
	mTextureBuf.put(0f); // (0, 1)
	mTextureBuf.put(1f);
	mTextureBuf.position(0);

	mColorBuf.position(0);
	for (int i=0; i< 3; i++) {
	    mColorBuf.put(1f);
	    mColorBuf.put(1f);
	    mColorBuf.put(0.5f);
	    mColorBuf.put(0.7f);
	}
	mColorBuf.position(0);
    }

    public void setTextureID(int texture) {
	mTextureID = texture;
    }

    public void setSize(RectF rect) {
	mArea = rect;
	float height = mArea.height();
	float width  = mArea.width();

	Log.i(TAG,"width:"+width+" height:"+height);

	mVertexBuf.position(0);

	mVertexBuf.put(-0.5f*2);
	mVertexBuf.put(0.25f*2);
	mVertexBuf.put(0);

	mVertexBuf.put(0.5f*2);
	mVertexBuf.put(0.25f*2);
	mVertexBuf.put(0);
/*
	mVertexBuf.put(width);
	mVertexBuf.put(-height);
	mVertexBuf.put(0);
*/
	mVertexBuf.put(0);
	mVertexBuf.put(0.55f*2);
	mVertexBuf.put(0);

	mVertexBuf.position(0);
    }

    public void drawGLView(GL10 gl) {
	if (!visible) {
	    return;
	}
	gl.glActiveTexture(GL10.GL_TEXTURE0);
	gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
	gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
	gl.glRotatef(15f,1f,1f,1f);
	gl.glFrontFace(GL10.GL_CCW);
	gl.glVertexPointer(mVertexNum, GL10.GL_FLOAT, 0, mVertexBuf);
	gl.glEnable(GL10.GL_TEXTURE_2D);
	gl.glTexCoordPointer(TEXTURE_DIMENSION, GL10.GL_FLOAT, 0, mTextureBuf);
	gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuf);

	gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 3,
		GL10.GL_UNSIGNED_SHORT, mIndexBuf);
	mVertexBuf.position(0);
	mTextureBuf.position(0);
	mIndexBuf.position(0);
	mColorBuf.position(0);
    }

    public void hide() {
	visible = false;
    }

    public void show() {
	visible = true;
    }
}

