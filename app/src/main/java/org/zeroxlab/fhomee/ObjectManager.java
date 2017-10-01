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

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/* ObjectManager is a singleton instance */
public class ObjectManager {

    final String TAG = "ObjectManager";

    private static ObjectManager manager = new ObjectManager();

    // Key: Id, Value: GLObject
    private HashMap mObjectMap;

    private int mCounter = 0;

    private ObjectManager() {
        mObjectMap = new HashMap();
    }

    synchronized static public ObjectManager getInstance() {
        if (manager == null) {
            manager = new ObjectManager();
        }

        return manager;
    }

    public GLObject getGLObjectById(int id) {
        Integer target = new Integer(id);
        GLObject obj = (GLObject) mObjectMap.get(target);

        if (obj == null) {
            Log.i(TAG, "cannot find out GLObject which id = " + id);
        }

        return obj;
    }

    public Integer getIdByGLObject(GLObject obj) {
        Integer id = null;
        Set keys = mObjectMap.keySet();
        Iterator<Integer> iterator = keys.iterator();
        while (id == null && iterator.hasNext()) {
            Integer pointer = iterator.next();
            if (obj.equals(mObjectMap.get(pointer))) {
                id = pointer;
            }
        }

        if (id == null) {
            Log.i(TAG, "Cannot find out the id of GLObject");
        }

        return id;
    }

    public synchronized int register(GLObject obj) {

        Integer id = null;

        if (mObjectMap.containsValue(obj)) {
            id = getIdByGLObject(obj);
            Log.i(TAG, "GLObject already registered, ID = " + id.intValue());
        } else {
            id = fetchUsableId();
            mObjectMap.put(id, obj);
        }

        return id.intValue();
    }

    /* unregister the GLObject from manager
     * NOTICE: Dont forget to call this method
     * otherwise GC never recycle the memory.
     */
    public void unregister(GLObject obj) {
        Integer id = new Integer(obj.getId());
        mObjectMap.remove(id);
    }

    private Integer fetchUsableId() {
        Integer key;
        do {
            increaseCounter();
            key = new Integer(mCounter);
        } while (mObjectMap.containsKey(key));

        return key;
    }

    private void increaseCounter() {
        if (mCounter == Integer.MAX_VALUE) {
            mCounter = 0;
        } else {
            mCounter++;
        }
    }
}

