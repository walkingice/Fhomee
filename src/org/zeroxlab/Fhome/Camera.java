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

public class Camera {
    public float LEFT   = ViewManager.PROJ_LEFT;
    public float RIGHT  = ViewManager.PROJ_RIGHT;
    public float TOP    = ViewManager.PROJ_TOP;
    public float BOTTOM = ViewManager.PROJ_BOTTOM;
    public float NEAR   = ViewManager.PROJ_NEAR;
    public float FAR    = ViewManager.PROJ_FAR;

    public static boolean sRefresh = true;

    protected RectF mNearViewport;
    protected LinkedList<Layer> mLayers;

    protected Object mChildrenLock;

    public Camera() {
        mChildrenLock = new Object();
        mNearViewport = new RectF();
        mLayers = new LinkedList<Layer>();
        setNearViewport();
    }

    public void setFrustum(float l, float r, float t, float b, float n, float f) {
        LEFT = l;
        RIGHT = r;
        TOP = t;
        BOTTOM = b;
        NEAR = n;
        FAR = f;
        setNearViewport();
    }

    public void onDraw(GL10 gl) {
        Layer layer;

        if (sRefresh) {
            for (int i = mLayers.size() - 1; i >= 0; i--) {
                layer = mLayers.get(i);
                layer.checkViewport();
            }

            sRefresh = false;
        }

	gl.glMatrixMode(gl.GL_MODELVIEW);

	gl.glLoadIdentity();

	/* the coordinate of OpenGL is different from normal computer system
	 * We may rotate the coordinate so we don't have to worry about that.
	 */
	gl.glRotatef(180f, 1f, 0f, 0f); // now the +x is heading for right
					//         +y is heading for bottom

        for (int i = mLayers.size() - 1; i >= 0; i--) {
            layer = mLayers.get(i);
            layer.onDraw(gl);
        }
    }

    public void addLayer(Layer layer) {
        addLayer(-1, layer);
    }

    public void addLayer(int location, Layer layer) {
        /*The first layer(position = 0) will be drawn in the last*/
	synchronized(mChildrenLock) {
	    int position = location;

	    if (position < 0 || position > mLayers.size()) {
		position = mLayers.size(); // add to tail
	    }

	    mLayers.add(position, layer);
	}
    }

    public Layer removeChild(Layer layer) {

	int index = mLayers.indexOf(layer);
	return removeChild(index);
    }

    public Layer removeChild(int index) {
	Layer layer;
	synchronized(mChildrenLock) {
	    if (index < 0 || index >= mLayers.size()) {
		return null;
	    }

	    layer = mLayers.remove(index);
	}
	return layer;
    }

    public RectF getNearViewport() {
        return mNearViewport;
    }

    public void updateLayerViewport() {
        Layer layer;
        for (int i = 0; i < mLayers.size(); i++) {
            layer = mLayers.get(i);
            layer.setViewport(mNearViewport);
        }
    }

    private void setNearViewport() {
        mNearViewport.set(LEFT, TOP, RIGHT, BOTTOM);
    }
}

