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

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

public class Invoker {

    Intent mIntent;
    final String TAG = "Invoker";

    Invoker(String packageName, String className) {
        ComponentName component = new ComponentName(packageName, className);
        mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mIntent.setComponent(component);
    }

    Invoker(Intent intent) {
        mIntent = intent;
    }

    public boolean invoke() {
        if (mIntent != null) {
            MainActivity.getActivity().startActivity(mIntent);
        } else {
            Log.i(TAG, "Invoke nothing");
            return false;
        }

        return true;
    }
}

