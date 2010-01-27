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

    private HashMap mTextureMap;
    private TextureManager() {
	mTextureMap = new HashMap();
    }

    synchronized static public TextureManager getInstance() {
	if(manager == null) {
	    manager = new TextureManager();
	}

	return manager;
    }

    public void setGLContext(GL10 gl) {
	mGLContext = gl;
	clearAll();
    }

    public void clearAll() {
	mTextureMap.clear();
    }

    public int generateOneTexture(Bitmap bitmap, String name) {

	Integer textureId = (Integer)mTextureMap.get(name);

	if(textureId == null) {
	    int[] textures = new int[1];

	    mGLContext.glGenTextures(1, textures, 0);
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

	    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	    bitmap.recycle();

	    textureId = new Integer(textures[0]);
	    recordTextureName(name, textureId);
	} else {
	    Log.i(TAG, name + "created");
	}

	return textureId.intValue();
    }

    private void recordTextureName(String name, Integer index) {
	mTextureMap.put(name, index);
    }
}

