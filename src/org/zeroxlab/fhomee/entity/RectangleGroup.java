/*
 * Authored By Julian Chu <walkingice@0xlab.org>
 *
 * Copyright (c) 2010 0xlab.org - http://0xlab.org/
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


package org.zeroxlab.fhomee.entity;

import org.zeroxlab.fhomee.time.GLAnimation;
import org.zeroxlab.fhomee.*;

import android.util.Log;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.LinkedList;
import javax.microedition.khronos.opengles.GL10;

public class RectangleGroup extends Rectangle {

    public final String TAG = "RectangleGroup";

    protected LinkedList<Rectangle> mChildren;
    protected Object mChildrenLock;

    public RectangleGroup(float x, float y, float width, float height) {
        super(x, y, width, height);
        mChildrenLock = new Object();
        mChildren = new LinkedList<Rectangle>();
    }

    public boolean contains(float x, float y) {
        mPts[0] = x;
        mPts[1] = y;
        mInvert.mapPoints(mPts);
        return mRect.contains(mPts[0], mPts[1]);
    }

    protected void resetChildAbsTranslatematrix() {
        for (int i = 0; i < mChildren.size(); i++) {
            Rectangle child = mChildren.get(i);
            child.resetAbsTranslateMatrix();
        }
    }

    protected void drawChildren(GL10 gl) {
        Rectangle child;
        for (int i = 0; i < mChildren.size(); i++) {
            child = mChildren.get(i);

            gl.glPushMatrix();
            child.draw(gl);
            gl.glPopMatrix();
        }
    }

    protected void checkChildrenViewport(float[] viewport) {
        Rectangle child;
        for (int i = mChildren.size() - 1; i >= 0; i--) {
            child = mChildren.get(i);
            child.checkViewport(viewport);
        }
    }

    protected int pointerAtChildren(float convertedX, float convertedY) {
            Rectangle child;
            /* ask the children by inverse ordering.
             * The last child will be drawed lastest
             * therefore it will be top than other children.
             * so we have to ask the last one first.
             */
            for (int i = mChildren.size() - 1; i >= 0; i--) {
                child = mChildren.get(i);
                int id  = child.pointerAt(convertedX, convertedY);
                if (id != -1) {
                    return id;
                }
            }

            return -1;
    }

    protected void updateChildrenCoverage() {
        Rectangle child;
        for (int i = mChildren.size() - 1; i >= 0; i--) {
            child = mChildren.get(i);
            child.getTranslatedCoverage(mTmpRect);
            mCoverage.union(mTmpRect);
        }
    }

    protected boolean measureChildren(float ratioX, float ratioY) {

        Rectangle child;
        boolean updated = false;
        for (int i = 0; i < mChildren.size(); i++) {
            child = mChildren.get(i);
            boolean childUpdated = child.measure(ratioX, ratioY);
            updated = (updated || childUpdated);
        }
        return updated;
    }

    public void addChild(Rectangle child) {
        this.addChild(-1, child);
    }

    /**
     * Add a GLObject as a child to specified position
     *
     * @param location The specified position
     * @param obj The child
     */
    public void addChild(int location, Rectangle child) {
        synchronized(mChildrenLock) {
            int position = location;

            if (position < 0 || position > mChildren.size()) {
                position = mChildren.size(); // add to tail
            }

            child.setParent(this);
            mChildren.add(position, child);
        }
    }

    public Rectangle removeChild(Rectangle child) {
        int index = mChildren.indexOf(child);
        return removeChild(index);
    }

    public Rectangle removeChild(int index) {
        Rectangle child;
        synchronized(mChildrenLock) {
            if (index < 0 || index >= mChildren.size()) {
                return null;
            }
            child = mChildren.remove(index);
        }

        return child;
    }

    public int getChildrenCount() {
        return mChildren.size();
    }

}

