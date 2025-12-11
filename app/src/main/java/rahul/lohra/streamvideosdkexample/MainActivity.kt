package rahul.lohra.streamvideosdkexample

import android.content.Context
import android.os.Bundle
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.getstream.log.Priority
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.logging.HttpLoggingLevel
import io.getstream.video.android.core.logging.LoggingLevel
import io.getstream.video.android.core.notifications.NotificationConfig
import io.getstream.video.android.core.permission.android.StreamPermissionCheck
import io.getstream.video.android.model.User
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
    Box(Modifier.fillMaxSize().padding(14.dp), contentAlignment = Alignment.Center) {
        Column {
            Text("Select the user to login", fontSize = 26.sp)
            Spacer(Modifier.height(48.dp))
            HorizontalDivider()
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
                HorizontalDivider()
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
            UserScreen(username)
        }
    }
}

@Composable
fun UserScreen(username: String) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        initSdk(context, username)
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(14.dp)) {
        var selectedUsers by remember { mutableStateOf(setOf<String>()) }

        Column {
            val streamVideoState = StreamVideo.instanceState.collectAsStateWithLifecycle()
            val loggedInUserName = streamVideoState.value?.user?.name
            Text("Welcome, $loggedInUserName!", fontSize = 24.sp)

            Spacer(Modifier.height(16.dp))
            Text("Select user to call", fontSize = 26.sp)
            Spacer(Modifier.height(48.dp))
            HorizontalDivider()
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
                HorizontalDivider()
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button({
                    performCall(selectedUsers.toList(), false)
                }) {
                    Text("Audio Call")
                }
                Spacer(Modifier.width(16.dp))
                Button({
                    performCall(selectedUsers.toList(), true)
                }) {
                    Text("Video Call")
                }
            }
        }
    }
}

private fun initSdk(context: Context, username: String) {
    StreamVideo.removeClient()
    val apiKey = "9k9p69vpa78m"
    val user = User(id = username, name = username.capitalize())
    StreamVideoBuilder(
        context = context,
        apiKey = apiKey,
        user = user,
        loggingLevel = LoggingLevel(Priority.VERBOSE, HttpLoggingLevel.BODY),
        token = TokenUtils.createStreamToken(username, "5apyvgdp7pnzes23e4kzkngkybfx9ajqtae2sqjqfp3xrhudrfvst4tznbspwzaa"),
        notificationConfig = NotificationConfig()
    )
        .build()
}

fun performCall(users: List<String>, isVideo: Boolean) {
    StreamVideo.instanceOrNull()?.let { streamVideo ->
        val call = streamVideo.call("default", UUID.randomUUID().toString())
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            call.create(users, ring = true, video = isVideo)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserScreenDemo() {
    StreamVideoSdkExampleTheme {
        UserScreen("Tony")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StreamVideoSdkExampleTheme {
        RootUi(navController = rememberNavController())
    }
}