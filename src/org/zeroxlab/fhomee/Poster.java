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

import org.zeroxlab.fhomee.core.GLObject;

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

import org.zeroxlab.fhomee.TextureManager.TextureObj;

public class Poster extends GLObject implements Bubble.BubbleOwner{

    final String TAG = "Poster";

    public boolean mHasBubble = false;
    private Bubble mBubble;

    protected int mNumber = 0;
    protected GLLabel mNumberLabel;
    protected Invoker mInvoker;

    Poster(float width, float height, String textureName) {
        this(0, 0, width, height, textureName);
    }

    Poster(float x, float y, float width, float height, String textureName) {
        super(x, y, width, height);
        setTextureByName(textureName);
    }

    public boolean invokable() {
        return (mInvoker != null);
    }

    public void invoke() {
        if (invokable()) {
            mInvoker.invoke();
        }
    }

    public void setInvoker(Invoker invoker) {
        mInvoker = invoker;
    }

    public void showBubble(String text, boolean showOption) {
        if (mHasBubble) {
            mBubble.clearBubble();
        }

        final float offset = 15f;
        mBubble = new Bubble(this);
        mBubble.setText(text);
        mBubble.showOptions(showOption);
        mHasBubble = true;
        mBubble.setSize(186f, 70f);
        mBubble.setXY(this.getWidth() - offset, -(mBubble.getHeight() - offset));
        addChild(mBubble);
        mHasBubble = true;
    }

    public void hideBubble() {
        if (mBubble != null) {
            mBubble.clearBubble();
            removeChild(mBubble);
        }
        mHasBubble = false;
    }

    public void resetNumber() {
        setNumber(0);
    }

    public void setNumber(int num) {
        if (num > 0) {
            mNumber = num;
            removeNumber();
            mNumberLabel = createNumber(num);
            mNumberLabel.setXY(-5f, -5f);
            addChild(mNumberLabel);
        } else if(num <= 0 ){
            removeNumber();
        }
    }

    private GLLabel createNumber(int num) {
        mNumber = num;
        GLLabel label = new GLLabel();
        String text = "" + num;
        label.setTextSize(14);
        label.setBackground("number");
        label.setSize(20f, 20f);
        label.setColor(0xFFFFFFFF); // White
        label.setText(text);
        return label;
    }

    private void removeNumber() {
        if (mNumberLabel == null) {
            return;
        }

        super.removeChild(mNumberLabel);
        mNumberLabel.clear();
        mNumberLabel = null;
        mNumber = 0;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (mHasBubble) {
            mBubble.setXY(this.getWidth(), 0);
        }
    }

    @Override
    public void drawMyself(GL10 gl) {
        super.drawMyself(gl);
    }

    @Override
    public void onClick() {
        if (mInvoker != null) {
            mInvoker.invoke();
            return;
        }

        if(mHasBubble) {
        } else {
            showBubble("Hi", true);
        }
    }

    public void onBubbleFinish(int flag) {
        hideBubble();
        /* Testing code */
        if (flag == Bubble.BUTTON_OK) {
            setNumber(++mNumber);
        } else if (flag == Bubble.BUTTON_CANCEL) {
            resetNumber();
        }
    }
}

