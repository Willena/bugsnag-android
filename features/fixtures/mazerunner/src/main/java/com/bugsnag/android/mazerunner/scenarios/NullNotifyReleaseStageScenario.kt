package com.bugsnag.android.mazerunner.scenarios

import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration

/**
 * Attempts to send a handled exception to Bugsnag, when the notifyReleaseStages is null.
 */
internal class NullNotifyReleaseStageScenario(config: Configuration,
                                              context: Context) : Scenario(config, context) {
    init {
        config.setAutoCaptureSessions(false)
    }

    override fun run() {
        super.run()
        Bugsnag.setReleaseStage("prod")
//        Bugsnag.setNotifyReleaseStages()
        Bugsnag.notify(generateException())
    }

}
