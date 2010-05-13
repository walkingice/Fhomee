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

    GLLabel mLabel;
    public final static int mDefaultLabelColor = 0xFF000000;

    GLObject mClose;
    public final static String mCloseTexture = "close";
    public final static float mCloseWidth  = 25f;
    public final static float mCloseHeight = 25f;

    public Bubble() {
	super(1f, 1f);
	setDefaultTextureName(background);

	mLabel = new GLLabel();
	mLabel.setColor(mDefaultLabelColor);
	addChild(mLabel);

	mClose = new GLObject(mCloseWidth, mCloseHeight);
	mClose.setDefaultTextureName(mCloseTexture);
	float x = this.getWidth()  - mCloseWidth;
	float y = this.getHeight() - mCloseHeight;
	mClose.setXY(x, y);
	addChild(mClose);
    }

    @Override
    public void setSize(float width, float height) {
	super.setSize(width, height);
	float x = this.getWidth()  - mCloseWidth;
	float y = 0;
	mClose.setXY(x, y);

	mLabel.setSize(width * 0.8f, height * 0.8f);
	x = (this.getWidth()  - mLabel.getWidth())  / 2;
	y = (this.getHeight() - mLabel.getHeight()) / 2;
	mLabel.setXY(x, y);
    }

    public void setText(String text) {
	mLabel.setText(text);
	mLabel.setXY(0, 0);
	//setSize(getWidth(), getHeight()); // reset text size
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

