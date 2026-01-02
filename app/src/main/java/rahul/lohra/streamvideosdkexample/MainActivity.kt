package rahul.lohra.streamvideosdkexample

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.log.Priority
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.logging.HttpLoggingLevel
import io.getstream.video.android.core.logging.LoggingLevel
import io.getstream.video.android.core.notifications.NotificationConfig
import io.getstream.video.android.core.notifications.NotificationHandler
import io.getstream.video.android.core.notifications.internal.service.CallServiceConfig
import io.getstream.video.android.core.notifications.internal.service.CallServiceConfigRegistry
import io.getstream.video.android.model.StreamCallId
import io.getstream.video.android.model.User
import io.getstream.video.android.model.UserType
import io.getstream.video.android.ui.common.StreamCallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rahul.lohra.streamvideosdkexample.ui.theme.StreamVideoSdkExampleTheme
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamVideoSdkExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    RootUi(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                    )
                }
            }
        }
    }
}

val appUsers = arrayListOf<String>("tony", "steve", "peter", "sam")

@Composable
fun LoginScreen(onClick: (String) -> Unit) {
    var fcmToken by remember { mutableStateOf("EMPTY_TOKEN") }
    LaunchedEffect(Unit) {
        fcmToken = FirebaseTokenManager.awaitFCMToken() ?: "EMPTY_TOKEN"
        Log.d("Noob", "fcm token: $fcmToken")
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(14.dp), contentAlignment = Alignment.Center
    ) {
        Column {

            Text("Fcm Token: $fcmToken", fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Text("Select the user to login", fontSize = 26.sp)
            Spacer(Modifier.height(48.dp))
//            HorizontalDivider()
            appUsers.forEach {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            onClick(it)
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(it.capitalize(Locale.US), fontSize = 26.sp)
                }
//                HorizontalDivider()
            }
        }
    }
}

@Composable
fun RootUi(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen { username ->
                navController.navigate("user/$username")
            }
        }
        composable("user/{username}") { backStackEntry ->
            // Extract the username from the route
            val username = backStackEntry.arguments?.getString("username") ?: "Unknown User"
            // A simple screen to show the selected user
            UserScreen(username, {
                navController.navigate("livestream_host")
            }, {
                navController.navigate("livestream_guest")
            })
        }
        composable("livestream_host") {
            VideoTheme {
                LiveHost(navController, "demo")
            }
        }
        composable("livestream_guest") {
            VideoTheme {
                LiveGuest(navController, "demo")
            }
        }
    }
}

@Composable
fun UserScreen(username: String, onLiveClick:()->Unit, onGuestClick:()->Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        initSdk(context, username)
    }

    var fcmToken by remember { mutableStateOf("EMPTY_TOKEN") }
    LaunchedEffect(Unit) {
        fcmToken = FirebaseTokenManager.awaitFCMToken() ?: "EMPTY_TOKEN"
        Log.d("Noob", "fcm token: $fcmToken")
    }

    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        var selectedUsers by remember { mutableStateOf(setOf<String>()) }

        Column {
            val streamVideoState = StreamVideo.instanceState.collectAsStateWithLifecycle()
            val loggedInUserName = streamVideoState.value?.user?.name
            Text("Welcome, $loggedInUserName!", fontSize = 24.sp)

            Spacer(Modifier.height(16.dp))
            Text("Select user to call", fontSize = 26.sp)
            Spacer(Modifier.height(48.dp))
//            HorizontalDivider()
            appUsers.filter { it != username }.forEach { user ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            val newSelection = selectedUsers.toMutableSet()
                            if (user in selectedUsers) {
                                newSelection.remove(user)
                            } else {
                                newSelection.add(user)
                            }
                            selectedUsers = newSelection
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            user.capitalize(),
                            modifier = Modifier.align(Alignment.CenterVertically),
                            fontSize = 26.sp
                        )
                        Checkbox(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            checked = user in selectedUsers,
                            onCheckedChange = { isChecked ->
                                // 3. Update the state when the checkbox itself is clicked
                                val newSelection = selectedUsers.toMutableSet()
                                if (isChecked) {
                                    newSelection.add(user)
                                } else {
                                    newSelection.remove(user)
                                }
                                selectedUsers = newSelection
                            })
                    }

                }
//                HorizontalDivider()
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val context = LocalContext.current
                Button({
                    performCall(selectedUsers.toList(), false, context)
                }) {
                    Text("Audio Call")
                }
                Spacer(Modifier.width(16.dp))
                Button({
                    performCall(selectedUsers.toList(), true, context)
                }) {
                    Text("Video Call")
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val context = LocalContext.current
                Button({
                    onLiveClick()
                }) {
                    Text("Host LiveStream")
                }
                Spacer(Modifier.width(16.dp))
                Button({
                    onGuestClick()
                }) {
                    Text("Guest Livestream")
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val context = LocalContext.current
                Button({
                    performLogout()
                }) {
                    Text("Log out")
                }
                Spacer(Modifier.width(16.dp))
                Button({
//                    performLivestreamCall(false, context)
                }) {
//                    Text("Guest Livestream")
                }
            }
        }
    }
}

private fun initSdk(context: Context, username: String) {
    StreamVideo.removeClient()
    val apiKey = "9k9p69vpa78m"
    val user = User(
        id = username,
        name = username.capitalize(),
        type = UserType.Authenticated,
    )
    val callServiceRegistry = CallServiceConfigRegistry()
    callServiceRegistry.register("audio_room", CallServiceConfig(true))
    callServiceRegistry.register("default", CallServiceConfig(true))

    StreamVideoBuilder(
        context = context,
        apiKey = apiKey,
        user = user,
        callServiceConfigRegistry = callServiceRegistry,
        loggingLevel = LoggingLevel(Priority.VERBOSE, HttpLoggingLevel.BODY),
        token = TokenUtils.createStreamToken(
            username,
            "5apyvgdp7pnzes23e4kzkngkybfx9ajqtae2sqjqfp3xrhudrfvst4tznbspwzaa"
        ),
        notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(
                FirebasePushDeviceGenerator(providerName = "firebase", context = context)
            )
        )
    )
        .build()
}

fun performCall(users: List<String>, isVideo: Boolean, context: Context) {
    StreamVideo.instanceOrNull()?.let { streamVideo ->
        val callType = if (isVideo) "default" else "audio_room"
        val callId = UUID.randomUUID().toString()
        val call = streamVideo.call(callType, callId)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            call.create(users, ring = true, video = isVideo)
                .onSuccess {
                    val streamCallId = StreamCallId(callType, callId)
                    val intent = StreamCallActivity.callIntent(
                        context,
                        action = NotificationHandler.ACTION_OUTGOING_CALL,
                        cid = streamCallId,
                        members = users,
                        clazz = MyCallActivity::class.java
                    )
                    context.startActivity(intent)
                }
        }
    }
}

fun performLivestreamCall(isHost: Boolean, context: Context) {
    StreamVideo.instanceOrNull()?.let { streamVideo ->
        val callType = "livestream"
        val callId = UUID.randomUUID().toString()
        val call = streamVideo.call(callType, callId)
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
        }
    }
}

fun performLogout() {
    StreamVideo.instanceOrNull()?.let { streamVideo ->
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            streamVideo.logOut()
            streamVideo.cleanup()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserScreenDemo() {
    StreamVideoSdkExampleTheme {
        UserScreen("Tony", {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StreamVideoSdkExampleTheme {
        RootUi(navController = rememberNavController())
    }
}