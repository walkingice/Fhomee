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

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import android.content.res.Resources;

import javax.microedition.khronos.opengles.GL10;

public class Ground extends GLObject {

    final String TAG = "Ground";
    final int mElfMax = 4;

    public Ground(float width, float height, String background) {
	this(-1, width, height, background);
    }

    public Ground(int id, float width, float height, String background) {
	super(id, 0, 0, width, height);

	setTextureName(background);
    }

    public void addElf(GLObject elf) {
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
	float bottom = mRect.height() * 0.75f;
	float y = bottom - elf.height();

	elf.setXY(x, y);
	addChild(elf);
    }
}

