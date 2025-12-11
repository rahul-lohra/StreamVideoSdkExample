package rahul.lohra.streamvideosdkexample

import android.app.Application
import android.util.Base64
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.getstream.log.taggedLogger
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Date

class App: Application() {

    override fun onCreate() {
        super.onCreate()
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

    fun createStreamToken(userId: String, secret: String): String {
        require(userId.isNotBlank()) { "User ID cannot be empty" }

        val algorithm = Algorithm.HMAC256(secret)

        return JWT.create()
            .withClaim("user_id", userId) // REQUIRED by Stream
            .withIssuedAt(Date(System.currentTimeMillis()))
            .sign(algorithm)
    }
}