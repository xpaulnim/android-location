package sample.services

import android.app.IntentService
import android.content.Intent
import android.util.Log

class CollectLocationService : IntentService("LocationCollectionService") {
    companion object {
        val TAG: String = CollectLocationService::class.java.simpleName
    }

    override fun onHandleIntent(intent: Intent?) {
        try {
            for (i in 1 .. 5) {
                Log.i(TAG, "Doing work...")
                Thread.sleep(5000)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}