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
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import android.graphics.*;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.zeroxlab.Fhome.TextureManager.TextureObj;

public class GLView {

    final static String TAG = "GLView";
    final static int VERTEX_DIMENSION  = 3;
    final static int TEXTURE_DIMENSION = 2;
    final static int COLOR_SIZE = 4; // RGBA

    private boolean visible = true;
    private float updateRate = 1f;

    private int mVertexNum = 4;
    private ByteBuffer  mIndexBuf;
    private FloatBuffer mVertexBuf;
    private FloatBuffer mTextureBuf;
    private FloatBuffer mColorBuf;

    private TextureObj mTextureObj;

    public static TextureManager TextureMgr = TextureManager.getInstance();

    RectF mArea;// quick hack, it supposed to be 4 arbitrary points
		// but not a Rectangle. Maybe a GLView will compose
		// arbitrary triangle in the feature...hope so.

    public GLView()  {
	ByteBuffer index;
	ByteBuffer vertex;
	ByteBuffer texture;
	ByteBuffer color;
	// 1short = 2byte, 1float = 4 byte
	index   = ByteBuffer.allocateDirect(6);
	vertex  = ByteBuffer.allocateDirect(mVertexNum * VERTEX_DIMENSION * 4);
	texture = ByteBuffer.allocateDirect(mVertexNum * TEXTURE_DIMENSION * 4);
	color   = ByteBuffer.allocateDirect(mVertexNum * COLOR_SIZE * 4);

	index.order(ByteOrder.nativeOrder());
	vertex.order(ByteOrder.nativeOrder());
	texture.order(ByteOrder.nativeOrder());
	color.order(ByteOrder.nativeOrder());

	mIndexBuf   = index;
	mVertexBuf  = vertex.asFloatBuffer();
	mTextureBuf = texture.asFloatBuffer();
	mColorBuf   = color.asFloatBuffer();

	mIndexBuf.position(0);
	mIndexBuf.put((byte)0);
	mIndexBuf.put((byte)1);
	mIndexBuf.put((byte)3);
	mIndexBuf.put((byte)1);
	mIndexBuf.put((byte)2);
	mIndexBuf.put((byte)3);
	mIndexBuf.position(0);

	// clean all to zero
	mVertexBuf.position(0);
	for (int i=0; i< mVertexNum; i++) {
	    mVertexBuf.put((float)0);
	    mVertexBuf.put((float)0);
	    mVertexBuf.put((float)0);
	}
	mVertexBuf.position(0);

	// (0, 0) is Left-Top corner of Texture image
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
	for (int i=0; i< mVertexNum; i++) {
	    mColorBuf.put(0f);
	    mColorBuf.put(0f);
	    mColorBuf.put(i*0.5f);
	    mColorBuf.put(0.7f);
	}
	mColorBuf.position(0);

	mArea = new RectF();
    }

    public void setTexture(TextureObj obj) {
	mTextureObj = obj;
    }

    public void setSize(RectF rect) {
	/*  0      1
	 *  +------+
	 *  |      |
	 *  |      |
	 *  +------+
	 *  3      2
	 */
	mArea.set(rect);
	float height = mArea.height();
	float width  = mArea.width();

	mVertexBuf.position(0);
	// point 0
	//mVertexBuf.put(0, (float)0);
	//mVertexBuf.put(1, (float)0);
	//mVertexBuf.put(2, (float)0);
	// point 1
	mVertexBuf.put(3, (float)width);
	//mVertexBuf.put(4, (float)0);
	//mVertexBuf.put(5, (float)0);
	// point 2
	mVertexBuf.put(6, (float)width);
	mVertexBuf.put(7, (float)height);
	//mVertexBuf.put(8, (float)0);
	// point 3
	//mVertexBuf.put(9, (float)0);
	mVertexBuf.put(10, (float)height);
	//mVertexBuf.put(11,(float)0);

	mVertexBuf.position(0);
    }

    public TextureObj getTexture() {
	return mTextureObj;
    }

    public void drawGLView(GL10 gl) {
	if (!visible) {
	    return;
	}

	gl.glFrontFace(gl.GL_CW);
	gl.glVertexPointer(VERTEX_DIMENSION, GL10.GL_FLOAT, 0, mVertexBuf);
	if (mTextureObj != null) {
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureObj.getTexture());
	} else {
	    Log.i(TAG, "Oooops, texture object is null");
	}
	gl.glTexCoordPointer(TEXTURE_DIMENSION, GL10.GL_FLOAT, 0, mTextureBuf);
	gl.glDrawElements(gl.GL_TRIANGLES, 6, gl.GL_UNSIGNED_BYTE, mIndexBuf);
    }

    //FIXME: should we have to perform any clear to ByteBuffer? I am not sure
    public void clear() {
    }

    public void hide() {
	visible = false;
    }

    public void show() {
	visible = true;
    }
}

