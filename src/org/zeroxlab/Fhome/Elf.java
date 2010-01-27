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
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import javax.microedition.khronos.opengles.GL10;

import java.util.LinkedList;
import java.io.InputStream;
import java.io.IOException;
import android.content.res.Resources;

public class Elf extends GLObject{

    final String TAG = "Elf";

    protected float mPositionX;
    protected float mPositionY;
    protected Jump mJumpAni;

    protected int mBorderID;
    protected String mBorderName = "elf_border";
    protected int mStandID;
    protected String mStandName  = "elf_stand";

    /*I want to draw a Square Body but a Elf has foot.
      If we scale the size of body, we get gap */
    protected float mGapX = 0f;
    protected float mGapY = 0f;
    protected GLView mBorder;
    protected GLView mFoot;
    protected RectF mBorderRect;
    protected RectF mFootRect;

    protected int[]    mWalkingFootID;
    protected String[] mWalkingFoot = {
	"elf_walk1"
	, "elf_walk2"
	, "elf_walk3"
	, "elf_walk4"
    };

    Elf() {
	super(0, 0, 20, 20);
	int footNum = mWalkingFoot.length;
	mWalkingFootID = new int[footNum];

	setDefaultTextureName("elf_body");

	mBorderRect = new RectF();
	mFootRect   = new RectF();

	mBorder = new GLView();
	mFoot   = new GLView();

	mJumpAni = new Jump(1000, mPositionX, mPositionY);
    }

    @Override
    public void setSize(float width, float height) {
	float bodyH = height * 0.75f; // 75%

	super.setSize(bodyH, bodyH);

	mGapX = (width - bodyH) / 2;
	mGapY = 0;
	mBorderRect.set(0, 0, bodyH, bodyH);
	mFootRect.set(0, 0, bodyH, height - bodyH);

	mBorder.setSize(mBorderRect);
	mFoot.setSize(mFootRect);
    }

    public void setPosition(float x, float y) {
	mPositionX = x;
	mPositionY = y;
	super.setXY(x, y);
    }

    public void generateTextures() {
	super.generateTextures();
	int length = mWalkingFoot.length;
	Bitmap bitmap;
	for (int i = 0; i < length; i++) {
	    bitmap = ResourcesMgr.getBitmapByName(mWalkingFoot[i]);
	    mWalkingFootID[i] = TextureMgr.generateOneTexture(bitmap, mWalkingFoot[i]);
	}

	bitmap    = ResourcesMgr.getBitmapByName(mBorderName);
	mBorderID = TextureMgr.generateOneTexture(bitmap, mBorderName);
    	bitmap    = ResourcesMgr.getBitmapByName(mStandName);
	mStandID  = TextureMgr.generateOneTexture(bitmap, mStandName);
	mBorder.setTextureID(mBorderID);
	mFoot.setTextureID(mStandID);
    }

    @Override
    public void draw(GL10 gl) {
	moveModelViewToPosition(gl);
	if (mVisible || true) { // || true for testing
	    boolean drawMyself = true;
	    synchronized (mAnimationLock) {
		if (mAnimation != null) {
		    drawMyself = mAnimation.applyAnimation(gl);
		}   
	    }   

	    if (drawMyself) {
		gl.glTranslatef(mGapX, 0f, 0f);
		mGLView.drawGLView(gl);
		mBorder.drawGLView(gl);
		//mGLView.drawGLView(gl);
		gl.glTranslatef(0f, mBorderRect.height(), 0f);
		mFoot.drawGLView(gl);
	    }   

	    /* Animation might change drawing color, reset it. */
	    gl.glColor4f(1f, 1f, 1f, 1f);
	}
    }

    @Override
    public void onClick() {
	mJumpAni.setDestination(mPositionX, mPositionY);
	setAnimation(mJumpAni);
	Timeline.getInstance().addAnimation(mJumpAni);
    }

    class Jump extends GLTranslate{
	int mTimes = 4;
	float mHeight = -30; // negative means up
	long mRoutine;

	Jump(long howlong, float endX, float endY) {
	    super(howlong, endX, endY);

	    int circle   = (int) mTimes / 2;
	    mRoutine = mLife / circle; //   time/per-circle
	}

	@Override
	public boolean applyAnimation(GL10 gl) {
	    boolean glObjectDrawItself = true;
	    float elapse = GLAnimation.mNow - mStart;
	    float percentOfTick = (float)(elapse % mRoutine) / mRoutine;
	    float percentOfAll  = (float)(elapse / mLife);
	    double radians = Math.toRadians(360 * percentOfTick);
	    float offset = (float) Math.abs(Math.sin(radians)) * mHeight * (1 - percentOfAll);
	    mObject.setXY(mObject.getX(), offset);
	    return glObjectDrawItself;
	}
    }
}
