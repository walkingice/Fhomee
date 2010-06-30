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

import org.zeroxlab.Fhome.TextureManager.TextureObj;

public class Pet extends GLObject{

    final String TAG = "Pet";

    protected float mPositionX;
    protected float mPositionY;
    protected Jump mJumpAni;

    protected String mBorderName = "pet_border";
    protected String mStandName  = "pet_stand";
    protected String mBackgroundName = "white";

    protected GLObject mBorder;
    protected GLObject mFoot;
    protected GLView mBackground;
    /* Offset between the Left-top of Icon to Left-top of Border*/
    protected float offsetX;
    protected float offsetY;

    protected Poster mPoster;

    protected int[]    mWalkingFootID;
    protected String[] mWalkingFoot = {
	"elf_walk1"
	, "elf_walk2"
	, "elf_walk3"
	, "elf_walk4"
    };

    Pet() {
        this(null);
    }

    Pet(Poster poster) {
	super(0, 0, 20, 20);
	int footNum = mWalkingFoot.length;
	mWalkingFootID = new int[footNum];

        mPoster = poster;

	setTextureByName("elf_body");
        if (mPoster != null) {
            setTexture(mPoster.getTexture());
        }

	mBorder = new GLObject(10, 10);
	mFoot   = new GLObject(10, 10);
        mBackground = new GLView();
        mBackground.setSize(super.mRect);
        TextureObj obj = TextureMgr.getTextureObj(
                ResourcesMgr.getBitmapByName(mBackgroundName), mBackgroundName);
        mBackground.setTexture(obj);

	mBorder.setTextureByName(mBorderName);
	mFoot.setTextureByName(mStandName);
        addChild(mBorder);
        addChild(mFoot);

	mJumpAni = new Jump(1000, 0, 0);
    }

    @Override
    public void setSize(float width, float height) {
        float bodyW = width;
        float bodyH = height * 0.8f;
        float iconW = bodyW * 0.7f;
        float iconH = bodyH * 0.7f; // 70%

        super.setSize(iconW, iconH);
        mBackground.setSize(super.mRect);

        if (mBorder != null && mFoot != null) {
            mBorder.setSize(bodyW, bodyH);
            mFoot.setSize(bodyW, height - bodyH);

            offsetX = mBorder.getWidth() * 0.15f;
            offsetY = mBorder.getHeight() * 0.27f;
            mBorder.setXY(-offsetX, -offsetY);
            mFoot.setXY(mBorder.getX(), mBorder.getY() + mBorder.getHeight());
        }
    }

    public void setPosition(float x, float y) {
        mPositionX = x;
        mPositionY = y;
        this.setXY(mPositionX, mPositionY);
    }

    @Override
    protected void destroyGLView() {
        super.destroyGLView();
        mBackground.clear();
    }

    @Override
    protected void drawMyself(GL10 gl) {
        mBackground.drawGLView(gl);
        super.drawMyself(gl);
    }

    public int pointerAt(float x, float y) {
        float id = super.pointerAt(x, y);
        if (id != -1) {
            return this.getId();
        }
        return -1;
    }

    public Poster getPoster() {
        return mPoster;
    }

    @Override
    public void onClick() {
        if (mPoster != null && mPoster.invokable()) {
            mPoster.invoke();
            return;
        }

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
            /* 5f is a dirty hack cause PetBar setXY(x, -5f) to this one */
	    float offset = (float) Math.abs(Math.sin(radians)) * mHeight * (1 - percentOfAll) - 5f;
	    mObject.setXY(mObject.getX(), offset);
	    return glObjectDrawItself;
	}
    }
}

