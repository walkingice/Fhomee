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


package org.zeroxlab.fhomee.time;

import org.zeroxlab.fhomee.entity.GLObject;

import android.util.Log;

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

public class GLAnimation extends Timer{

    final static String TAG="GLAnimation";

    protected GLObject mObject;

    GLAnimation(long howLong) {
        this(howLong, DEFAULT_UPDATE);
    }

    GLAnimation(long howLong,long update) {
        super(howLong, update);
    }

    public void bindGLObject(GLObject object) {
        mObject = object;
    }

    public void unbindGLObject() {
        mObject = null;
    }

    /* This method was called iff this Animation complete but not be interrupted.*/
    public void complete() {
        super.complete();

        /* If this animation is interrupted, mObject becomes null */
        if (mObject != null) {
            mObject.clearAnimation();
        }
        unbindGLObject();
    }

    public boolean applyAnimation(GL10 gl) {
        // do nothing yet, subclass overwrite me
        /* FIXME: if a subclass want to operate mObject at applyAnimation
           it might cause a null pointer exception because while another thread
           set a new animation to GLObject, it call unbindGLObject
           then clear mObject
           */
        boolean glObjectDrawItself = true;
        return glObjectDrawItself;
    }
}

