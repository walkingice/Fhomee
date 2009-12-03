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


/* TextureManager is a singleton instance */
public class TextureManager {

    private static TextureManager manager = new TextureManager();
    final String TAG="TextureManager";
    private static Context          mContext;
    private static ResourcesManager mResManager;
    private HashMap mTextureMap;
    private TextureManager() {
	mTextureMap = new HashMap();
    }

    synchronized static public TextureManager getInstance(Context context) {
	if(manager == null) {
	    manager = new TextureManager();
	}

	mContext    = context;
	mResManager = ResourcesManager.getInstance(context);
	return manager;
    }

    public void setContext(Context context) {
    }

    public int generateOneTexture(GL10 gl, String name) {

	Integer textureId = (Integer)mTextureMap.get(name);

	if(textureId == null) {
	    int[] textures = new int[1];

	    gl.glGenTextures(1, textures, 0);
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
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

	    Bitmap bitmap = mResManager.getBitmapByName(name);
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

