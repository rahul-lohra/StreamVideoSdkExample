package rahul.lohra.streamvideosdkexample

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.getstream.video.android.compose.ui.components.call.CallAppBar
import io.getstream.video.android.compose.ui.components.livestream.LivestreamPlayer
import io.getstream.video.android.core.ParticipantState
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.notifications.internal.service.DefaultCallConfigurations
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val TAG =  "LiveGuest"

@Composable
fun LiveGuest(
    navController: NavController,
    callId: String,
){
    StreamVideo.instanceOrNull()?.let { client ->
        // Step 1 - Update call settings via callConfigRegistry
        client.state.callConfigRegistry.register(
            DefaultCallConfigurations.getLivestreamGuestCallServiceConfig(),
        )

        // Step 2 - join a call, which type is `default` and id is `123`.
        val call = remember(callId) { client.call("livestream", callId) }

        suspend fun performJoin() {
            call.join()
        }

        LaunchedEffect(call) {
            call.microphone.setEnabled(false, fromUser = true)
            call.camera.setEnabled(false, fromUser = true)
            performJoin()
        }

        LaunchedEffect(call) {
            call.state.participants.collect { participants->
                Log.d(TAG, "participant size: ${participants.size}")

                participants.forEach {
                    val participantName = it.name.value
                    val logText = "name: $participantName, video enabled: ${it.videoEnabled.value}, video:${it.video.value},"
                    Log.d(TAG, "participant : $logText")
                }
            }
        }

        LaunchedEffect(call) {
            val item = call.state.participants.flatMapLatest { participants: List<ParticipantState> ->
                // For each participant, create a small Flow that watches videoEnabled.
                val participantVideoFlows = participants.map { participant ->
                    participant.videoEnabled.map { enabled -> participant to enabled }
                }
                // Combine these Flows: whenever a participant’s videoEnabled changes,
                // we re-calculate which participants have video.
                combine(participantVideoFlows) { participantEnabledPairs ->
                    participantEnabledPairs
                        .filter { (_, isEnabled) -> isEnabled }
                        .map { (participant, _) -> participant }
                }
            }.flatMapLatest { participantWithVideo ->
                participantWithVideo.firstOrNull()?.video ?: flow { emit(null) }
            }

            item.collect {
                Log.d(TAG, "video enabled: $it")
            }
        }

        val coroutineScope = rememberCoroutineScope()

        val onRetryJoin: () -> Unit = {
            coroutineScope.launch {
                performJoin()
            }
        }

        Box {
            LivestreamPlayer(
                call = call,
                onRetryJoin = onRetryJoin,
            )
            CallAppBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(end = 16.dp, top = 16.dp),
                call = call,
                centerContent = { },
                onCallAction = {
                    call.leave()
                    navController.popBackStack()
                },
            )
        }
    }
}