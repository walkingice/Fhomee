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


package org.zeroxlab.Fhome;

import android.util.Log;
import android.view.MotionEvent;
import javax.microedition.khronos.opengles.GL10;

interface Touchable {
    public boolean onPressEvent(float nowX, float nowY, MotionEvent event);
    public boolean onReleaseEvent(float nowX, float nowY, MotionEvent event);
    public boolean onDragEvent(float nowX, float nowY, MotionEvent event);
}
