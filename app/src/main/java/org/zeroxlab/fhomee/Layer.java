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

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

public class Layer {

    public final static String TAG = "Layer";
    public static float sProjNearWidth = -1;
    public static float sProjNearHeight = -1;
    public static float sProjNear = -1;
    public static float sProjLeft = -1;
    public static float sProjBottom = -1;

    /* ratio of ScreenSize:Near
     * they will be used in measuring.
     * Horizontal Ratio = Project width / Screen Width
     */
    public static float sRatioH = 1;
    public static float sRatioV = 1;

    private float mDepth = 0;
    private float mZn = 0;
    private float mLayerLeft = 0;
    private float mLayerTop = 0;
    private PointF mPoint;
    private LinkedList<GLObject> mChildren;
    private LinkedList<Touchable> mTouchableItems;

    Layer(float depth) {
        if (sProjNearWidth == -1
                || sProjNearHeight == -1
                || sProjNear == -1
                || sProjLeft == -1
                || sProjBottom == -1) {
            Log.e(TAG, "please initialize Projector related parameters");
        }

        mDepth = depth;
        mZn = mDepth / sProjNear;
        /* We rotate the coordinate to make Z-axis upside down
           (0, 0) is at Left-Top */
        mLayerLeft = mZn * sProjLeft;
        mLayerTop = mZn * sProjBottom;
        mPoint = new PointF();
        mChildren = new LinkedList<GLObject>();
        mTouchableItems = new LinkedList<Touchable>();
    }

    public void measure() {
        GLObject obj;
        for (int i = 0; i < mChildren.size(); i++) {
            obj = mChildren.get(i);
            obj.measure(mZn * sRatioH, mZn * sRatioV);
        }
    }

    public void addChild(GLObject obj, boolean isTouchable) {
        mChildren.add(obj);
        if (isTouchable) {
            Touchable t = (Touchable) obj;
            mTouchableItems.add(t);
        }
    }

    public void onDraw(GL10 gl) {
        for (int i = 0; i < mChildren.size(); i++) {
            gl.glPushMatrix();
            gl.glTranslatef(mLayerLeft, mLayerTop, 0f);
            gl.glTranslatef(0f, 0f, mDepth);
            mChildren.get(i).draw(gl);
            gl.glPopMatrix();
        }
    }

    /* if the specificTarget is at this Layer,
     * return the id of the object which contains the point
     */
    public int getIdContains(PointF nearPoint, GLObject specificTarget) {
        pointNearToLayer(mZn, nearPoint, mPoint);
        if (mChildren.contains(specificTarget)) {
            return specificTarget.pointerAt(mPoint.x, mPoint.y);
        }
        return -1;
    }

    public int getIdContains(PointF nearPoint) {
        for (int i = 0; i < mChildren.size(); i++) {
            GLObject obj = mChildren.get(i);
            int id = getIdContains(nearPoint, obj);
            if (id != -1) {
                return id;
            }
        }

        return -1;
    }

    public boolean onPressEvent(PointF nearPoint, MotionEvent event, Touchable specificTarget) {
        if (mTouchableItems.contains(specificTarget)) {
            pointNearToLayer(mZn, nearPoint, mPoint);
            return specificTarget.onPressEvent(mPoint, event);
        }

        return false;
    }

    public boolean onReleaseEvent(PointF nearPoint, MotionEvent event, Touchable specificTarget) {
        if (mTouchableItems.contains(specificTarget)) {
            pointNearToLayer(mZn, nearPoint, mPoint);
            return specificTarget.onReleaseEvent(mPoint, event);
        }

        return false;
    }

    public boolean onDragEvent(PointF nearPoint, MotionEvent event, Touchable specificTarget) {
        if (mTouchableItems.contains(specificTarget)) {
            pointNearToLayer(mZn, nearPoint, mPoint);
            return specificTarget.onDragEvent(mPoint, event);
        }

        return false;
    }

    public boolean onLongPressEvent(PointF nearPoint, MotionEvent event, Touchable specificTarget) {
        if (mTouchableItems.contains(specificTarget)) {
            pointNearToLayer(mZn, nearPoint, mPoint);
            return specificTarget.onLongPressEvent(mPoint, event);
        }

        return false;
    }


    public boolean onPressEvent(PointF nearPoint, MotionEvent event) {
        for (int i = 0; i < mTouchableItems.size(); i++) {
            Touchable touchable = mTouchableItems.get(i);
            if (onPressEvent(nearPoint, event, touchable)) {
                return true;
            }
        }
        return false;
    }

    public boolean onReleaseEvent(PointF nearPoint, MotionEvent event) {
        for (int i = 0; i < mTouchableItems.size(); i++) {
            Touchable touchable = mTouchableItems.get(i);
            if (onReleaseEvent(nearPoint, event, touchable)) {
                return true;
            }
        }
        return false;
    }

    public boolean onDragEvent(PointF nearPoint, MotionEvent event) {
        for (int i = 0; i < mTouchableItems.size(); i++) {
            Touchable touchable = mTouchableItems.get(i);
            if (onDragEvent(nearPoint, event, touchable)) {
                return true;
            }
        }
        return false;
    }

    public boolean onLongPress(PointF nearPoint, MotionEvent event) {
        for (int i = 0; i < mTouchableItems.size(); i++) {
            Touchable touchable = mTouchableItems.get(i);
            if (onLongPressEvent(nearPoint, event, touchable)) {
                return true;
            }
        }
        return false;
    }

    public static void pointLayerToNear(float zN, PointF layerPoint, PointF nearPoint) {
        nearPoint.x = layerPoint.x / zN;
        nearPoint.y = layerPoint.y / zN;
    }

    public static void pointNearToLayer(float zN, PointF nearPoint, PointF layerPoint) {
        layerPoint.x = zN * nearPoint.x;
        layerPoint.y = zN * nearPoint.y;
    }
}

