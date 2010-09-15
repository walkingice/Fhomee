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

import android.util.Log;

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

public class GLFade extends GLAnimation{

    final static String TAG="GLFade";

    private float mR = 1f;
    private float mG = 1f;
    private float mB = 1f;

    GLFade(long howlong) {
	this(howlong, 1f, 1f, 1f);
    }

    GLFade(long howlong, float r, float g, float b) {
	super(howlong, 10);
	mR = r;
	mG = g;
	mB = b;
    }

    public boolean applyAnimation(GL10 gl) {
	boolean glObjectDrawItself = true;
	float now  = GLAnimation.mNow;
	float life = mLife;
	float elapse = now - mStart;
	float ratio = elapse / life;
	float alpha = 1f - ratio;
	gl.glColor4f(mR, mG, mB, alpha);
	return glObjectDrawItself;
    }
}

