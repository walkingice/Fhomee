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
import android.content.Intent;
import android.appwidget.*;
import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import java.util.ArrayList;

public class Elf extends Pet{

    final String TAG = "Elf";

    final String DEFAULT_TEXTURE = "mario";
    private PetBar mPetBar;

    private static Activity sActivity;

    Elf() {
	super();
	setTextureByName(DEFAULT_TEXTURE);
    }

    public void setPetBar(PetBar bar) {
	mPetBar = bar;
    }

    public static void setActivity(Activity a) {
        sActivity = a;
    }

    @Override
    public void onClick() {
	super.onClick();

        int appWidgetId = Launcher.getWidgetHost().allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        AppWidgetProviderInfo info = new AppWidgetProviderInfo();
        sActivity.startActivityForResult(pickIntent, Launcher.REQUEST_PICK_APPWIDGET);
    }
}

