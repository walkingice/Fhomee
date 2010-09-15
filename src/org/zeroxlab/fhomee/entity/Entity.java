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
 * Entity is a basic data structure represent a entity
 * without position, size
 */
public class Entity {

    public final String TAG = "Entity";

    protected int mID = -1;

    public Entity() {
        mID = EntityManager.getInstance().register(this);
    }

    /* Before you drop this Entity, please call this method
     * for reducing your memory usage.
     */
    public void clear() {
        EntityManager.getInstance().unregister(this);
    }

    public int getId() {
        return mID;
    }
}

