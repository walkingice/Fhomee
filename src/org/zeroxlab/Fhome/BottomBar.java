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

import javax.microedition.khronos.opengles.GL10;

public class BottomBar extends GLObject {

    final String TAG = "BottomBar";
    final int mElfMax = 4;

    final public static int STANDING = 0;
    final public static int WALKING  = 1;
    private int mStatus = STANDING;

    private GLTranslate mShiftAnimation;

    public BottomBar(float width, float height) {
	super(0, 0, width, height);
	mShiftAnimation = new GLTranslate(100, 0, 0);
    }

    public void standing() {
	mStatus = STANDING;
    }

    public void walking() {
	mStatus = WALKING;
    }

    public void backToCenter() {
	float x = 0;
	float y = super.getY();
	mShiftAnimation.setDestination(x, y);
	this.setAnimation(mShiftAnimation);
	Timeline.getInstance().addAnimation(mShiftAnimation);
    }

    public void addElf(Elf elf) {
	float elfSpace = mRect.width() / mElfMax;
	float finalWidth = elfSpace * 0.8f; // 80%
	float ratio = finalWidth / elf.width();
	float finalHeight = ratio * elf.height();

	elf.setSize(finalWidth, finalHeight);

	int count = 0;
	if (mChildren != null) {
	    count = mChildren.size();
	}

	float gapX = elfSpace * 0.1f; // 10%
	float x = count * elfSpace + gapX;

	elf.setPosition(x, 0);
	addChild(elf);
    }

    public void draw(GL10 gl) {
	moveModelViewToPosition(gl);

	if (mHasChildren) {
	    GLObject obj;
	    for (int i = 0; i < mChildren.size(); i++) {
		obj = mChildren.get(i);
		gl.glPushMatrix();
		obj.draw(gl);
		gl.glPopMatrix();
	    }
	}

	return;
    }
}

