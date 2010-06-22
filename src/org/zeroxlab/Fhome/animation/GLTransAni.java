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

import javax.microedition.khronos.opengles.GL10;

import org.zeroxlab.Fhome.TextureManager.TextureObj;

public class GLTransAni extends GLAnimation {

    final static String TAG="GLTransAni";
    final static public long TRANSITION_UPDATE = 300;

    protected GLTransition mTransition;

    GLTransAni(GLTransition transition) {
	super(transition.getLife());
	super.setUpdateTime(TRANSITION_UPDATE);
	mTransition = transition;
	mTransition.setStart(GLTransition.Now);
    }

    /* This method was called iff this Animation complete but not be interrupted.*/
    public void complete() {
	if (mObject != null) {
	    TextureObj obj = mObject.getTexture();
	    mObject.setTexture(obj);
	}
	super.complete();
    }

    public boolean applyAnimation(GL10 gl) {
	boolean glObjectDrawItself = true;
	TextureObj obj = mTransition.getNowTextureObj();
	mObject.setTexture(obj);
	return glObjectDrawItself;
    }
}

