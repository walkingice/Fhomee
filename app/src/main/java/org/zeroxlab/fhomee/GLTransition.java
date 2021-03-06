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

import android.graphics.Bitmap;

import org.zeroxlab.fhomee.TextureManager.TextureObj;

public class GLTransition {

    final static String TAG = "GLTransition";
    final static long DEFAULT_TIME = 500; // 500 ms

    protected static ResourcesManager ResourcesMgr = ResourcesManager.getInstance();
    protected static TextureManager TextureMgr = TextureManager.getInstance();

    public static long Now = 0;

    protected long mStart = 0;
    protected long mTotal = 0;

    protected GLObject mObject;

    protected String[] mName;
    protected long[] mTime;
    protected TextureObj[] mTextures;

    GLTransition(String[] name, long[] time) {
        int length = name.length;
        mName = new String[length];
        mTime = new long[length];
        mTextures = new TextureObj[length];

        for (int i = 0; i < length; i++) {
            mName[i] = name[i];
            mTime[i] = DEFAULT_TIME;
        }

        int time_length = time.length;
        if (time_length > mTime.length) {
            time_length = mTime.length;
        }

        mTotal = 0;
        for (int i = 0; i < time_length; i++) {
            mTime[i] = time[i];
            mTotal += mTime[i];
        }
    }

    public static void setNow(long now) {
        Now = now;
    }

    public long getLife() {
        return mTotal;
    }

    public void setStart(long start) {
        mStart = start;
    }

    public void generateTextures() {
        Bitmap bitmap;
        for (int i = 0; i < mName.length; i++) {
            bitmap = ResourcesMgr.getBitmapByName(mName[i]);
            mTextures[i] = TextureMgr.getTextureObj(bitmap, mName[i]);
        }
    }

    /**
     * Get a texture id which should being used now.
     * GLTransition holds a series of Texture id.
     * This method return one of them by elapsed time.
     *
     * @return the corresponding texture id
     */
    public TextureObj getNowTextureObj() {
        if (mStart + mTotal < Now) {
            mStart = Now;
        }
        long elapse = Now - mStart;
        int tail = mTime.length;
        for (int i = 0; i < tail; i++) {
            elapse = elapse - mTime[i];
            if (elapse <= 0) {
                //  we are at mTime[i] < Now < mTime[i+1]
                return mTextures[i];
            }
        }

	/* If exceed, return last one */
        return mTextures[tail];
    }
}

