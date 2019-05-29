package com.bugsnag.android.mazerunner.scenarios

import android.content.Context
import android.os.Looper
import android.os.Handler
import com.bugsnag.android.Configuration

/**
 * Stops the app from responding for a time period
 */
internal class AppNotRespondingScenario(config: Configuration,
                                        context: Context) : Scenario(config, context) {
    init {
        config.setAutoCaptureSessions(false)
        config.detectAnrs = true
    }

    override fun run() {
        super.run()
        val main = Handler(Looper.getMainLooper())
        main.postDelayed({
            Thread.sleep(60000) // FOREVER (test suite doesn't actually wait this long)
        }, 1) // A moment of delay so there is something to 'tap' onscreen
    }
}
