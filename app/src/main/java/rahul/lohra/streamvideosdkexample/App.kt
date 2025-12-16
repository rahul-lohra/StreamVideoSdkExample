package rahul.lohra.streamvideosdkexample

import android.app.Application
import android.os.Build
import android.util.Base64
import android.util.Log
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import io.getstream.log.taggedLogger
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

class App: Application() {

    val TAG = "App"
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "FCM token: $token"
            Log.d(TAG, msg)
        })
    }

    private fun initSdk(){
        val apiKey = "9k9p69vpa78m"
        val user = User(id = "rahul", name = "Rahul", )
        StreamVideoBuilder(
            context = this,
            apiKey = apiKey,
            user = user,
            token = TokenUtils.devToken(user.id),)
            .build()
    }
}

object TokenUtils {

    val logger by taggedLogger("Video:TokenUtils")

    fun getUserId(token: String): String = try {
        JSONObject(
            token
                .takeIf { it.contains(".") }
                ?.split(".")
                ?.getOrNull(1)
                ?.let {
                    String(
                        Base64.decode(
                            it.toByteArray(StandardCharsets.UTF_8),
                            Base64.NO_WRAP,
                        ),
                    )
                }
                ?: "",
        ).optString("user_id")
    } catch (e: JSONException) {
        logger.e(e) { "Unable to obtain userId from JWT Token Payload" }
        ""
    } catch (e: IllegalArgumentException) {
        logger.e(e) { "Unable to obtain userId from JWT Token Payload" }
        ""
    }

    /**
     * Generate a developer token that can be used to connect users while the app is using a development environment.
     *
     * @param userId the desired id of the user to be connected.
     */
    fun devToken(userId: String): String {
        require(userId.isNotEmpty()) { "User id must not be empty" }
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" //  {"alg": "HS256", "typ": "JWT"}
        val devSignature = "devtoken"
        val payload: String =
            Base64.encodeToString(
                "{\"user_id\":\"$userId\"}".toByteArray(StandardCharsets.UTF_8),
                Base64.NO_WRAP,
            )
        return "$header.$payload.$devSignature"
    }

    fun createJwtToken(
        username: String,
        secret: String,
        expiryMillis: Long = 3_600_000 // default 1 hour
    ): String {
        require(username.isNotBlank()) { "Username cannot be empty" }

        val now = System.currentTimeMillis()
        val algorithm = Algorithm.HMAC256(secret)

        return JWT.create()
            .withSubject(username)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + expiryMillis))
            .sign(algorithm)
    }

    fun createStreamToken1(userId: String, secret: String): String {
        require(userId.isNotBlank()) { "User ID cannot be empty" }

        val algorithm = Algorithm.HMAC256(secret)
        val issuedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Date.from(Instant.now())
        }else {
            Date(System.currentTimeMillis())
        }
        Log.d("Noob", "issuedDate: $issuedDate")
        return JWT.create()
            .withClaim("user_id", userId) // REQUIRED by Stream
            .withIssuedAt(issuedDate)
            .sign(algorithm)
    }

    fun createStreamToken(userId: String, secret: String): String {
        require(userId.isNotBlank()) { "User ID cannot be empty" }

        val algorithm = Algorithm.HMAC256(secret)

        // Subtract 10 seconds to account for clock skew
        val issuedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Date.from(Instant.now().minusSeconds(10))
        } else {
            Date(System.currentTimeMillis() - 10000) // 10 seconds in milliseconds
        }

        Log.d("Noob", "issuedDate: $issuedDate")

        return JWT.create()
            .withClaim("user_id", userId)
            .withIssuedAt(issuedDate)
            .sign(algorithm)
    }
}