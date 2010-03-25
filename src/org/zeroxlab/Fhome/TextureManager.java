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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.opengl.GLUtils;

import android.graphics.*;
import java.io.InputStream;
import java.io.IOException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.nio.IntBuffer;

/* TextureManager is a singleton instance */
public class TextureManager {

    private static TextureManager manager = new TextureManager();
    final String TAG="TextureManager";

    private GL10 mGLContext;

    private boolean mHasPending = false;
    private HashMap<String, TextureObj>  mTextureMap;
    private HashMap<String, TextureObj>  mPendingMap;

    private Object mLocker;

    private TextureManager() {
	mTextureMap = new HashMap<String, TextureObj>();
	mPendingMap = new HashMap<String, TextureObj>();
	mLocker = new Object();
    }

    synchronized static public TextureManager getInstance() {
	if(manager == null) {
	    manager = new TextureManager();
	}

	return manager;
    }

    public void setGLContext(GL10 gl) {
	mGLContext = gl;
	clearTextures();
    }

    private void clearTextures() {
	synchronized (mLocker) {
	    Iterator<TextureObj> iterator = mTextureMap.values().iterator();
	    while(iterator.hasNext()) {
		TextureObj obj = iterator.next();
		mPendingMap.put(obj.getName(), obj);
	    }

	    mTextureMap.clear();
	    mHasPending = true;
	}
    }

    public void clearAll() {
	Collection<TextureObj> collection = mTextureMap.values();
	TextureObj[] array = new TextureObj[collection.size()];
	array = collection.toArray(array);
	for (int i = 0; i < array.length; i++) {
	    array[i].destroy();
	}

	collection = mPendingMap.values();
	array = new TextureObj[collection.size()];
	array = collection.toArray(array);
	for (int i = 0; i < array.length; i++) {
	    array[i].destroy();
	}

	mTextureMap.clear();
	mPendingMap.clear();
    }

    public TextureObj getStringTextureObj(String string, Paint paint) {
	float width   = paint.measureText(string);
	float ascent  = Math.abs(paint.ascent());
	float descent = Math.abs(paint.descent());
	float height = ascent + descent;

	/* width and height should be the power of 2 */
	float temp = 16;
	while (temp < width) {
	    temp = temp * 2;
	}
	width = temp;
	temp = 16;
	while (temp < height) {
	    temp = temp * 2;
	}
	height = temp;

	Bitmap bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_4444);
	Canvas canvas = new Canvas(bitmap);
	bitmap.eraseColor(Color.TRANSPARENT);
	canvas.drawText(string, 0, ascent, paint);
	TextureObj obj = getTextureObj(bitmap, string);
	bitmap.recycle();
	return obj;
    }

    public TextureObj getTextureObj(Bitmap bitmap, String name) {

	TextureObj obj;

	obj = mTextureMap.get(name);
	if (obj != null) {
	    obj.increaseBinding(); // one more GLObject bind to this texture
	    return obj;
	}

	obj = mPendingMap.get(name);
	if (obj != null) {
	    obj.increaseBinding(); // one more GLObject bind to this texture
	    return obj;
	}

	obj = new TextureObj(name, bitmap, -1);
	synchronized (mLocker) {
	    mPendingMap.put(name, obj);
	    mHasPending = true;
	}

	return obj;
    }

    public void removeTextureObj(TextureObj obj) {
	obj.decreaseBinding();
	if (obj.bindingCount() == 0) {
	    synchronized(mLocker) {
		mPendingMap.put(obj.getName(), obj);
		mTextureMap.remove(obj.getName());
		mHasPending = true;
	    }
	}
    }

    public boolean hasPendingTextures() {
	return mHasPending;
    }

    public void updateTexture() {
	if (!mHasPending) {
	    return;
	}

	synchronized(mLocker) {
	    Collection<TextureObj> collection = mPendingMap.values();
	    TextureObj[] array = new TextureObj[collection.size()];
	    array = collection.toArray(array);
	    int length = array.length;

	    for (int i = 0; i < length; i++) {
		TextureObj obj = array[i];
		if (obj.bindingCount() > 0) {
		    generateTexture(obj);
		    mTextureMap.put(obj.getName(), obj);
		} else {
		    deleteTexture(obj);
		    obj.destroy();
		}
	    }

	    mPendingMap.clear();
	    mHasPending = false;
	}
    }

    private void generateTexture(TextureObj obj) {
	int length     = 1;
	int[] textures = new int[length];
	mGLContext.glGenTextures(length, textures, 0);
	mGLContext.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

	mGLContext.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
		GL10.GL_NEAREST);
	mGLContext.glTexParameterf(GL10.GL_TEXTURE_2D,
		GL10.GL_TEXTURE_MAG_FILTER,
		GL10.GL_LINEAR);
	mGLContext.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
		GL10.GL_CLAMP_TO_EDGE);
	mGLContext.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
		GL10.GL_CLAMP_TO_EDGE);
	mGLContext.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
		GL10.GL_REPLACE);
	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, obj.getBitmap(), 0);

	obj.setTexture(textures[0]);
    }

    private void deleteTexture(TextureObj obj) {
	int length     = 1;
	int[] textures = new int[length];
	textures[0] = obj.getTexture();
	mGLContext.glDeleteTextures(length, textures, 0);
    }

    class TextureObj {
	int    mId;
	int    mBinding;
	String mName;
	Bitmap mBitmap;

	TextureObj(String name, Bitmap bitmap) {
	    this(name, bitmap, -1);
	}

	TextureObj(String name, Bitmap bitmap, int id) {
	    mId     = id;
	    mName   = name;
	    Bitmap.Config config = bitmap.getConfig();
	    if (config == null) {
		Log.i(TAG, "could not get bitmap config from " + name);
		config = Bitmap.Config.ARGB_4444;
	    }
	    mBitmap = bitmap.copy(config, true);

	    mBinding = 1;
	}

	Bitmap getBitmap() {
	    return mBitmap;
	}

	void setTexture(int id) {
	    mId = id;
	}

	int getTexture() {
	    return mId;
	}

	String getName() {
	    return mName;
	}

	void increaseBinding() {
	    mBinding++;
	}

	void decreaseBinding() {
	    mBinding--;
	}

	int bindingCount() {
	    return mBinding;
	}

	void destroy() {
	    mBitmap.recycle();
	}
    }
}

