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

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import android.content.res.Resources;

import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

public class Bubble extends GLObject {

    final String TAG = "Bubble";
    public static String background = "bubble";
    private Owner mOwner;

    public final static int BUTTON_OK     = 1;
    public final static int BUTTON_CANCEL = 2;

    public Bubble() {
	super(1f, 1f);
	setDefaultTextureName(background);
    }

    public void setInformation() {
    }

    public void addButton(int type) {
    }

    public void clearBubble() {
	// recycle textures
    }

    @Override
    public void onClick() {
	if (mOwner != null) {
	    mOwner.onBubbleFinish(1);
	}

	clearBubble();
    }

    interface Owner {
	public void onBubbleFinish(int flag);
    }
}

