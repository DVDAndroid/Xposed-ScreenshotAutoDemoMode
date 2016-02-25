package com.dvd.android.xposed.screenshotautodemomode

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.SystemClock
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class XposedMod : IXposedHookLoadPackage {

    val PHONE_WINDOW_MANAGER_CLASS = "com.android.server.policy.PhoneWindowManager"
    val DEMO_MODE_ACTION = "com.android.systemui.demo"

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (!lpparam!!.packageName.equals("android"))
            return

        XposedHelpers.findAndHookMethod(PHONE_WINDOW_MANAGER_CLASS, lpparam.classLoader, "takeScreenshot", object : XC_MethodHook() {

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

                // set clock at 06:00 (marshmallow default)
                i = Intent(DEMO_MODE_ACTION)
                i.putExtra("command", "clock")
                i.putExtra("hhmm", "0600")

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
        })
    }

}