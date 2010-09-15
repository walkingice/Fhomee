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


package org.zeroxlab.fhomee.time;

import org.zeroxlab.fhomee.entity.Entity;

import android.util.Log;

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

public class Timer {

    final static String TAG="Timer";
    final public static long DEFAULT_UPDATE = 500; // 500ms

    protected static long mNow = 0;

    protected TimerListener mListener;
    protected long mStart  = 0;
    protected long mEnd    = 0;
    protected long mLife   = 0;
    protected long mUpdate = 0;

    public Timer(long howLong) {
        this(howLong, DEFAULT_UPDATE);
    }

    public Timer(long howLong,long update) {
        mLife   = howLong;
        mUpdate = update;
    }

    public static void setNow(long now) {
        mNow = now;
    }

    public void setListener(TimerListener listener) {
        mListener = listener;
    }

    public void setStart(long start) {
        mStart = start;
        mEnd   = mStart + mLife;
    }

    public void setUpdateTime(long update) {
        mUpdate = update;
    }

    public long getUpdateTime() {
        return mUpdate;
    }

    public long getEndTime() {
        return mEnd;
    }

    public boolean isFinish(long now) {
        if (now > mEnd) {
            return true;
        }
        return false;
    }

    public void update() {
        if (mListener != null) {
            mListener.onTimerUpdate();
        }
    }

    /* This method was called iff this Timer complete but not be interrupted.*/
    public void complete() {
        if (mListener != null) {
            mListener.onTimerEnd();
        }
    }

    public interface TimerListener {
        public void onTimerUpdate();
        public void onTimerEnd();
    }
}

