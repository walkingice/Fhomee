/* ------------------------------------------------------------------
 * Copyright (C) 2009 0xlab.org - http://0xlab.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */

package org.zeroxlab.fhomee;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * ResourceManager is a resource center of Launcher.
 * It is a singleton instance. It read Drawable from outer package and provide
 * to component of Launcher.
 */
public class ResourcesManager {

    static final String TAG = "ResourcesManager";
    static final String DEFAULT_THEME = "org.zeroxlab.fhomee"; // hard code to itself
    static final String DEFAULT_VALUE = "Default";
    static final String LAUNCHER_THEME = "LAUNCHER_THEME";
    private static ResourcesManager mResourcesManager = new ResourcesManager();
    private static Context mContext;
    private static Resources mDefaultResources;
    private static Resources mOuterResources;
    private static String mDefaultPackageName = "org.zeroxlab.fhomee";
    private static String mOuterPackageName;

    private ResourcesManager() {
    }

    synchronized static public ResourcesManager getInstance() {
        if (mContext == null) {
            Log.i(TAG, "OOOOps..you should setContext before getInstance");
        }
        if (mResourcesManager == null) {
            mResourcesManager = new ResourcesManager();
        }

        return mResourcesManager;
    }

    public static void setContext(Context context) {
        mContext = context;
        updatePackageName();
        updateResources();
    }

    public boolean isContextAssigned() {
        if (mContext == null) {
            return false;
        }
        return true;
    }

    public Bitmap getBitmapByName(String name) {
        Bitmap bitmap;
        Resources resources;

        // id == 0 is illegal
        int id = getIdByName("drawable", name);
        if (id == 0) {
            id = getDefaultIdByName("drawable", name);
            resources = mDefaultResources;
        } else {
            resources = mOuterResources;
        }

        bitmap = BitmapFactory.decodeResource(resources, id);
        return bitmap;
    }

    public Drawable getDrawableByName(String name) {
        int id = getIdByName("drawable", name);
        Resources resources;
        if (id == 0) {
            id = getDefaultIdByName("drawable", name);
            resources = mDefaultResources;
        } else {
            resources = mOuterResources;
        }

        return resources.getDrawable(id);
    }

    private int getDefaultIdByName(String type, String name) {
        Resources resources = mDefaultResources;
        String packageName = mDefaultPackageName;
        int id = resources.getIdentifier(name, type, packageName);

        return id;
    }

    private int getIdByName(String type, String name) {
        Resources resources = mOuterResources;
        String packageName = mOuterPackageName;
        int id = resources.getIdentifier(name, type, packageName);

        if (id == 0) {
            Log.i(TAG, "BAD:id is 0," + name + " at " + packageName);
        }

        return id;
    }

    private static void updateResources() {
        updatePackageName();
        mDefaultResources = mContext.getResources();

        String packageName = mOuterPackageName;
        if (packageName.equals(DEFAULT_THEME)) {
            mOuterResources = mContext.getResources();
        } else {
        /* If found target package, use the resources of it */
            PackageManager pm = mContext.getPackageManager();
            try {
                mOuterResources = pm.getResourcesForApplication(packageName);
            } catch (PackageManager.NameNotFoundException ex) {
                ex.printStackTrace();
                mOuterResources = mContext.getResources();
            }
        }
    }

    private static void updatePackageName() {
        String preference = getPreference();
        if (preference.equals(DEFAULT_VALUE)) {
            mOuterPackageName = mDefaultPackageName;
        } else {
            mOuterPackageName = preference;
        }
    }

    private static String getPreference() {
        SharedPreferences settings = mContext.getSharedPreferences(LAUNCHER_THEME,
                Context.MODE_PRIVATE);
        String value = settings.getString(LAUNCHER_THEME, DEFAULT_VALUE);
        return value;
    }
}

