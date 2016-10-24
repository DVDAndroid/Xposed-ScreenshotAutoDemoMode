/*
 * Copyright 2016 dvdandroid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dvd.android.xposed.screenshotautodemomode

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class XposedMod : IXposedHookLoadPackage {

    val PHONE_WINDOW_MANAGER_CLASS = "com.android.server.policy.PhoneWindowManager"
    val DEMO_MODE_ACTION = "com.android.systemui.demo"

    val hook = object : XC_MethodHook() {

        @Throws(Throwable::class)
        override fun beforeHookedMethod(param: MethodHookParam?) {
            super.beforeHookedMethod(param)

            // get context
            val context = XposedHelpers.getObjectField(param!!.thisObject, "mContext") as Context?

            // set wifi level to max and visible
            var i: Intent = Intent(DEMO_MODE_ACTION)
            i.putExtra("command", "network")
            i.putExtra("wifi", "show")
            i.putExtra("level", "4")

            context!!.sendBroadcast(i)

            // set mobile level to max and visible
            i = Intent(DEMO_MODE_ACTION)
            i.putExtra("command", "network")
            i.putExtra("mobile", "show")
            i.putExtra("level", "4")

            context.sendBroadcast(i)

            // set battery level to 100 and not plugged
            i = Intent(DEMO_MODE_ACTION)
            i.putExtra("command", "battery")
            i.putExtra("level", "100")
            i.putExtra("plugged", "false")

            context.sendBroadcast(i)

            // hide all notifications
            i = Intent(DEMO_MODE_ACTION)
            i.putExtra("command", "notifications")
            i.putExtra("visible", "false")

            context.sendBroadcast(i)

            // set clock
            i = Intent(DEMO_MODE_ACTION)
            i.putExtra("command", "clock")
            i.putExtra("hhmm", getAndroidVersion())

            context.sendBroadcast(i)

            // wait at least for 200ms to avoid mod Screenshot delay remover mod
            SystemClock.sleep(200)
        }

        @Throws(Throwable::class)
        override fun afterHookedMethod(param: MethodHookParam?) {
            super.afterHookedMethod(param)

            val context = XposedHelpers.getObjectField(param!!.thisObject, "mContext") as Context?

            // wait 1 sec before exiting from demo mode
            Handler().postDelayed({ context!!.sendBroadcast(Intent(DEMO_MODE_ACTION).putExtra("command", "exit")) }, 1000)
        }
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam!!.packageName != "android")
            return

        if (Build.VERSION.SDK_INT >= 24) {
            XposedHelpers.findAndHookMethod(PHONE_WINDOW_MANAGER_CLASS, lpparam.classLoader, "takeScreenshot", "int", hook)
        } else {
            XposedHelpers.findAndHookMethod(PHONE_WINDOW_MANAGER_CLASS, lpparam.classLoader, "takeScreenshot", hook)
        }
    }

    fun getAndroidVersion(): String {
        val version = Build.VERSION.RELEASE
        val digits = version.split(".")

        var result = ""

        if (digits[0].length == 1) result += "0"

        result += digits[0]

        if (digits.size > 2) {
            result += digits[1] + digits[2]
        } else {
            result += digits[1] + "0"
        }

        return result
    }

}