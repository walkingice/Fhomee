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

import org.zeroxlab.fhomee.entity.GLObject;

import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import android.content.res.Resources;

import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

public class TopBar extends GLObject {

    final String TAG = "TopBar";
    protected int mObjMax = 4;

    public final static float BORDER_RATIO = 0.1f; // 10%
    protected float mHGap = 0f;
    protected float mVGap = 0f;
    protected static float mObjWidth  = 0f;
    protected static float mObjHeight = 0f;

    protected LinkedList<GLObject> mList;

    public TopBar(float width, float height) {
        this(width, height, null);
    }

    public TopBar(float width, float height, String background) {
        super(0, 0, width, height);

        if (background != null) {
            super.setTextureByName(background);
        }

        resetSizeParameters();
    }

    protected void resetSizeParameters() {
        float width  = mRect.width();
        float height = mRect.height();

        mHGap = width  * BORDER_RATIO;
        mVGap = height * BORDER_RATIO;

        float total = width - (mObjMax + 1) * mHGap;
        mObjWidth = total / mObjMax;
        mObjHeight = height - mVGap * 2; // top and bottom
    }

    public static float objWidth() {
        return mObjWidth;
    }

    public static float objHeight() {
        return mObjHeight;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        resetSizeParameters();
    }

    public void displayObj(LinkedList<GLObject> list) {
        mList = list;
        mHasChildren = true;  //therefore drawChildren will be called
        resetObjPosition();
    }

    protected void resetObjPosition() {
        if (mList != null) {
            GLObject obj;
            for (int i = 0; i < getChildrenCount(); i++) {
                obj = (GLObject)mChildren.get(i);
                float x = mHGap + i * (mHGap + mObjWidth);
                float y = mVGap;
                obj.setXY(x, y);
            }
        }
    }

    @Override
    protected void drawChildren(GL10 gl) {
        if (mList == null) {
            return;
        }

        GLObject obj;
        for (int i = 0; i < mList.size(); i++) {
            obj = mList.get(i);
            gl.glPushMatrix();
            obj.draw(gl);
            gl.glPopMatrix();
        }
    }
}

