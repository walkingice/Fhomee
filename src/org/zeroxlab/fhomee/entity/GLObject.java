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


package org.zeroxlab.fhomee.entity;

import org.zeroxlab.fhomee.time.GLAnimation;
import org.zeroxlab.fhomee.*;

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

/**
 * GLObject represent any Object on the screen
 * it encapsulants the information including position, size...etc
 */
public class GLObject extends RectangleGroup {

    final String TAG = "GLObject";
    protected static ResourcesManager ResourcesMgr = ResourcesManager.getInstance();
    protected static TextureManager   TextureMgr   = TextureManager.getInstance();

    protected ClickListener mListener;
    protected GLView mGLView;

    protected boolean mHasChildren = false;
    protected GLObject mParent = null;

    protected GLAnimation mAnimation;
    protected Object mAnimationLock;

    public GLObject(float width, float height) {
        this(0f, 0f, width, height);
    }

    public GLObject(float x, float y, float width, float height) {
        super(x, y, width, height);

        mAnimationLock = new Object();
        setXY(x, y);
    }


    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (mGLView != null) {
            mGLView.setSize(mRect);
        }

        updateCoverage();
    }

    /* Before you drop this GLObject, please call this method
     * for reducing your memory usage.
     */
    public void clear() {
        super.clear();
        setTexture(null);
        destroyGLView();
    }

    public void setAnimation(GLAnimation animation) {
        if (mAnimation != null) {
            clearAnimation();
        }
        animation.bindGLObject(this);
        mAnimation = animation;
    }

    public void clearAnimation() {
        synchronized (mAnimationLock) {
            if (mAnimation != null) {
                mAnimation.unbindGLObject();
            }
            mAnimation = null;
        }
    }

    /**
     * Set texture to this GLObject.
     * The GLObject will draw itself with this texture.
     * @param name The name of the Drawable of the texture
     */
    public void setTextureByName(String name) {
        TextureObj texture;
        Bitmap bitmap;
        bitmap  = ResourcesMgr.getBitmapByName(name);
        texture = TextureMgr.getTextureObj(bitmap, name);
        bitmap.recycle();
        this.setTexture(texture);
    }

    public void setTexture(TextureObj obj) {
        if (mGLView == null) {
            createGLView();
        }

        TextureObj old = mGLView.getTexture();
        mGLView.setTexture(obj);

        if (obj != null) {
            obj.increaseBinding();
        }
        if (old != null) {
            old.decreaseBinding();
        }
    }

    public TextureObj getTexture() {
        if (mGLView != null) {
            return mGLView.getTexture();
        }

        return null;
    }

    protected void createGLView() {
        if (mGLView == null) {
            mGLView = new GLView();
            mGLView.setSize(mRect);
        }

        mVisible = true;
    }

    protected void destroyGLView() {
        if (mGLView != null) {
            mGLView.clear();
            mGLView  = null;
        }

        mVisible = false;
    }

    public GLObject getParent() {
        return mParent;
    }

    protected boolean applyAnimation(GL10 gl) {
        boolean drawGLView = true;
        synchronized (mAnimationLock) {
            if (mAnimation != null) {
                drawGLView = mAnimation.applyAnimation(gl);
            }
        }

        return drawGLView;
    }

    protected void drawMyself(GL10 gl) {
        if (mGLView == null) {
            return;
        }

        mGLView.drawGLView(gl);
        /* Animation might change drawing color, reset it. */
        gl.glColor4f(1f, 1f, 1f, 1f);
    }

    public void setListener(ClickListener listener) {
        mListener = listener;
    }

    public void onClick() {
        if (mListener != null) {
            mListener.onClick(this);
        }
    }

    public interface ClickListener {
        public void onClick(GLObject obj);
    }
}

