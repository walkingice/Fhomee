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

import org.zeroxlab.fhomee.time.Timer;
import org.zeroxlab.fhomee.time.GLTransition;

import android.util.Log;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;

import java.lang.Thread;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.microedition.khronos.opengles.GL10;


/* Timeline is a singleton instance. It maintain OpenGL redraw rate */
public class Timeline implements IFhomee {

    public  static int ENDLESS = -1;
    private int DEFAULT_UPDATE = 5000; // 5 secs

    private static Timeline mTimeline = new Timeline();
    private static int sleepingTimer = 15;

    private long mLastUpdate;
    private long mUpdate = DEFAULT_UPDATE;

    private GLSurfaceView mSurface;
    private RedrawThread  mThread;

    private Object mLocker;
    private LinkedList<Timer> mTimers;
    private LinkedList<Timer> mUpdateTime;

    private boolean processing = false;

    private Timeline() {
    }

    synchronized static public Timeline getInstance() {
        if (mTimeline == null) {
            mTimeline = new Timeline();
        }

        return mTimeline;
    }

    public void addTimer(Timer timer) {
        timer.setStart(SystemClock.uptimeMillis());
        synchronized(mLocker) {
            mUpdateTime.add(timer);
            int position = linearSearchByEndTime(timer.getEndTime());
            mTimers.add(position, timer);
        }

        updateFrequency();
    }

    public void monitor(GLSurfaceView surface) {
        mSurface = surface;
        mLastUpdate = SystemClock.uptimeMillis();
        mThread = new RedrawThread();
        mThread.start();

        mLocker = new Object();
        mTimers = new LinkedList<Timer>();
        mUpdateTime = new LinkedList<Timer>();
        //UpdateComparator update = new UpdateComparator();
        //mUpdateTime = new TreeSet<GLTimer>(update);
    }

    /* Find out the position for new Timer by EndTime*/
    private int linearSearchByEndTime(long endTime) {
        long end;
        int counter = 0;
        for (counter = mTimers.size() - 1; counter >= 0;counter--) {
            end = mTimers.get(counter).getEndTime();
            if (end > endTime) {
                return counter+1;
            }
        }
        return 0;
    }

    private boolean clearExpiredTimer(long now) {
        if (mTimers.isEmpty()) {
            return false;
        }
        boolean redraw = false;
        long minimal = DEFAULT_UPDATE;

        boolean keepWalking = true;
        while (keepWalking) {
            Timer timer = mTimers.getLast();
            if (timer.isFinish(now)) {
                timer.complete();
                redraw = true;

                synchronized(mLocker) {
                    mTimers.remove(timer);
                    mUpdateTime.remove(timer);
                }

                if(mTimers.isEmpty()) {
                    keepWalking = false;
                }

                updateFrequency();
            } else {
                keepWalking = false;
            }
        }

        return redraw;
    }

    private void updateFrequency() {
        mUpdate = minimalFrequency();
        return;
    }

    private long minimalFrequency() {
        long minimal = DEFAULT_UPDATE;
        synchronized(mLocker) {
            for (int i = 0; i < mUpdateTime.size(); i++) {
                Timer timer = mUpdateTime.get(i);
                long update = timer.getUpdateTime();

                if(update < minimal) {
                    minimal = update;
                }
            }
        }
        return minimal;
    }

    private void processRedraw() {
        /* Making sure the redraw thread will not keep asking redraw*/
        if (processing) {
            return;
        }
        processing = true;

        boolean haveToUpdate = false;
        long now = SystemClock.uptimeMillis();
        long past = now - mLastUpdate;
        Timer.setNow(now);
        GLTransition.setNow(now);
        haveToUpdate = clearExpiredTimer(now);
        if (mTimers.isEmpty()) {
            //No Timer, do nothing
        } else {
            // FIXME: the time may be reset. maybe use Math.abs?
            if (mUpdate < past) {
                haveToUpdate = true;
            }
        }

        if (haveToUpdate) {
            for (int i = 0; i < mTimers.size(); i++) {
                mTimers.get(i).update();
            }
            mSurface.requestRender();
            mLastUpdate = now;
        }

        processing = false;
    }

    private class RedrawThread extends Thread {
        private boolean keepRunning = true;
        RedrawThread() {
        }

        public void end() {
            keepRunning = false;
        }

        public void run() {
            while (keepRunning) {
                try {
                    sleep(sleepingTimer);
                    processRedraw();
                } catch (InterruptedException exception) {
                    Log.i(TAG,"ooops, RedrawThread was interrupted!");
                    exception.printStackTrace();
                }
            }

            // supposed you will never be here
            Log.i(TAG," thread stopped. Anything go wrong?");
        }
    }

    //    private class TimerComparator implements Comparator<GLTimer> {
    //    private final int GREATER = 1;
    //    private final int EQUAL   = 0;
    //    private final int LESS    = -1;
    //
    //    public int compare(GLTimer ani1, GLTimer ani2) {
    //        long end1 = ani1.getEndTime();
    //        long end2 = ani2.getEndTime();
    //        if(ani1.equals(ani2)) {
    //                return EQUAL;
    //        }
    //        if(end1 >= end2) {
    //                return GREATER;
    //        } else if (end1 < end2) {
    //                return LESS;
    //        } else {
    //                return EQUAL;
    //        }
    //    }
    //
    //    public boolean equals(Object obj) {
    //        // FIXME: why do I need this?
    //        return false;
    //    }
    //    }
    //
    // Maybe we need it in the future
    //
    //    private class UpdateComparator implements Comparator<GLTimer> {
    //
    //    /* do not return 0, we may add two similar Timer into TreeSet.
    //       You cannot add Timer into TreeSet if compare() return 0 */
    //    public int compare(GLTimer ani1, GLTimer ani2) {
    //        long update1 = ani1.getUpdateTime();
    //        long update2 = ani2.getUpdateTime();
    //        if (ani1.mId == ani2.mId) {
    //                return 0; // equal
    //        }
    //        if(update1 >= update2) {
    //                return 1;
    //        } else {
    //                return -1;
    //        }
    //    }
    //
    //    public boolean equals(Object obj) {
    //        // FIXME: why do I need this?
    //        return false;
    //    }
    //    }
}

