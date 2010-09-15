/*
 * Authored By Julian Chu <walkingice@0xlab.org>
 *
 * Copyright (c) 2009 0xlab.org - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License")entity* you may not use this file except in compliance with the License.
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

import org.zeroxlab.fhomee.entity.Entity;

import android.util.Log;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/* EntityManager is a singleton instance */
public class EntityManager{

    final String TAG = "EntityManager";

    private static EntityManager manager = new EntityManager();

    // Key: Id, Value: Entity
    private HashMap mEntityMap;

    private int mCounter = 0;

    private EntityManager() {
        mEntityMap = new HashMap();
    }

    synchronized static public EntityManager getInstance() {
        if(manager == null) {
            manager = new EntityManager();
        }

        return manager;
    }

    public Entity getEntityById(int id) {
        Integer target = new Integer(id);
        Entity entity = (Entity)mEntityMap.get(target);

        if (entity == null) {
            Log.i(TAG, "cannot find out Entity which id = "+ id);
        }

        return entity;
    }

    public Integer getIdByEntity(Entity entity) {
        Integer id = null;
        Set keys = mEntityMap.keySet();
        Iterator<Integer> iterator = keys.iterator();
        while (id == null && iterator.hasNext()) {
            Integer pointer = iterator.next();
            if (entity.equals(mEntityMap.get(pointer))) {
                id = pointer;
            }
        }

        if (id == null) {
            Log.i(TAG, "Cannot find out the id of Entity");
        }

        return id;
    }

    public synchronized int register(Entity entity) {

        Integer id = null;

        if (mEntityMap.containsValue(entity)) {
            id = getIdByEntity(entity);
            Log.i(TAG, "Entity already registered, ID = " + id.intValue());
        } else {
            id = fetchUsableId();
            mEntityMap.put(id, entity);
        }

        return id.intValue();
    }

    /* unregister the Entity from manager
     * NOTICE: Dont forget to call this method
     * otherwise GC never recycle the memory.
     */
    public void unregister(Entity entity) {
        Integer id = new Integer(entity.getId());
        mEntityMap.remove(id);
    }

    private Integer fetchUsableId() {
        Integer key;
        do {
            increaseCounter();
            key = new Integer(mCounter);
        } while (mEntityMap.containsKey(key));

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

