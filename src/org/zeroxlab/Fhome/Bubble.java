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

    public final static int BORDER_PX = 15;
    public final static int BUTTON_WIDTH_PX  = 80;
    public final static int BUTTON_HEIGHT_PX = 20;
    public final static int BUTTON_TEXT_SIZE = 12;
    public final static int BUTTON_OK     = 1;
    public final static int BUTTON_CANCEL = 2;

    protected int mLevel = ViewManager.LEVEL_POSTER;
    protected float mButtonWidth  = 80f;
    protected float mButtonHeight = 20f;
    protected float mBorder  = 15f;

    GLLabel mLabel;
    public final static int mDefaultLabelColor = 0xFF000000;

    public boolean mShowOptions = false;
    GLLabel mOk;
    GLLabel mCancel;

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

	mOk     = new GLLabel("OK");
	mCancel = new GLLabel("Cancel");
	mOk.setColor(mDefaultLabelColor);
	mCancel.setColor(mDefaultLabelColor);
	mOk.setBackground("button");
	mCancel.setBackground("button");
	mOk.setVisible(false);
	mCancel.setVisible(false);
	addChild(mOk);
	addChild(mCancel);

	mClose = new GLObject(mCloseWidth, mCloseHeight);
	mClose.setDefaultTextureName(mCloseTexture);
	float x = this.getWidth()  - mCloseWidth;
	float y = this.getHeight() - mCloseHeight;
	mClose.setXY(x, y);
	addChild(mClose);

	setLevel(mLevel);
    }

    @Override
    public void setSize(float width, float height) {
	super.setSize(width, height);
	updateLayout();
    }

    public void setLevel(int level) {
	mLevel = level;
	mOk.setLevel(mLevel);
	mCancel.setLevel(mLevel);
	mLabel.setLevel(mLevel);
	float tmp;
	tmp = ViewManager.PROJ_WIDTH * BORDER_PX / ViewManager.mScreenWidth;
	mBorder= ViewManager.convertToLevel(mLevel, tmp);
	tmp = ViewManager.PROJ_WIDTH * BUTTON_WIDTH_PX / ViewManager.mScreenWidth;
	mButtonWidth = ViewManager.convertToLevel(mLevel, tmp);
	tmp = ViewManager.PROJ_HEIGHT * BUTTON_HEIGHT_PX / ViewManager.mScreenHeight;
	mButtonHeight= ViewManager.convertToLevel(mLevel, tmp);

	mOk.setSize(mButtonWidth, mButtonHeight);
	mCancel.setSize(mButtonWidth, mButtonHeight);
	updateLayout();
    }

    public void showOptions(boolean display) {
	mShowOptions = display;

	mOk.setVisible(mShowOptions);
	mCancel.setVisible(mShowOptions);
	updateLayout();
    }

    public void setText(String text) {
	mLabel.setText(text);
	updateLayout();
    }

    public void addButton(int type) {
    }

    public void clearBubble() {
	// recycle textures
    }

    private void updateLayout() {
	float w = getWidth();
	float h = getHeight();
	float center_x = w / 2;
	float center_y = h / 2;

	float x = w - mCloseWidth;
	float y = 0;
	mClose.setXY(x, y);

	if (mShowOptions) {
	    float bottomHeight = mButtonHeight + mBorder * 2;
	    x = center_x - mBorder - mButtonWidth;
	    y = h - bottomHeight + mBorder;
	    mOk.setXY(x, y);

	    x = center_x + mBorder;
	    mCancel.setXY(x, y);

	    h = h - bottomHeight;
	    center_y = h / 2;
	}

	x = (w - mLabel.getWidth()) / 2;
	y = (h - mLabel.getHeight()) / 2;
	mLabel.setXY(x, y);
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

