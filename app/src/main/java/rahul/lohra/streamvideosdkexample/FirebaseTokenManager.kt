// In a new file, e.g., FirebaseTokenManager.kt
package rahul.lohra.streamvideosdkexample

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

object FirebaseTokenManager {

    private const val TAG = "FirebaseTokenManager"

    /**
     * Asynchronously retrieves the current FCM registration token.
     * This suspend function will wait until the token is available.
     *
     * @return The FCM token string, or null if retrieval fails.
     */
    suspend fun awaitFCMToken(): String? {
        return try {
            // This is an asynchronous call that suspends the coroutine
            // until the Task<String> is complete.
            val token = Firebase.messaging.token.await()
            Log.d(TAG, "FCM token successfully retrieved: $token")
            token
        } catch (e: Exception) {
            // Handle potential exceptions, including network issues or if Play Services are unavailable.
            // A CancellationException should be re-thrown.
            if (e is CancellationException) throw e
            Log.e(TAG, "Fetching FCM registration token failed", e)
            null
        }
    }
}
