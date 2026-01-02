package rahul.lohra.streamvideosdkexample

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.getstream.video.android.compose.ui.ComposeStreamCallActivity
import io.getstream.video.android.compose.ui.StreamCallActivityComposeDelegate
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.MemberState
import io.getstream.video.android.core.call.state.CallAction
import io.getstream.video.android.ui.common.StreamActivityUiDelegate
import io.getstream.video.android.ui.common.StreamCallActivity


class MyCallActivity() : ComposeStreamCallActivity() {

    val TAG = "MyCallActivity"

    override val uiDelegate: StreamActivityUiDelegate<StreamCallActivity>
        get() = object : StreamCallActivityComposeDelegate(){
            @Composable
            override fun StreamCallActivity.IncomingCallContent(
                modifier: Modifier,
                call: Call,
                isVideoType: Boolean,
                isShowingHeader: Boolean,
                headerContent: @Composable (ColumnScope.() -> Unit)?,
                detailsContent: @Composable (ColumnScope.(List<MemberState>, Dp) -> Unit)?,
                controlsContent: @Composable (BoxScope.() -> Unit)?,
                onBackPressed: () -> Unit,
                onCallAction: (CallAction) -> Unit
            ) {
                io.getstream.video.android.compose.ui.components.call.ringing.incomingcall.IncomingCallContent(
                    call = call,
                    isVideoType = isVideoType,
                    modifier = modifier,
                    isShowingHeader = isShowingHeader,
                    headerContent = {
                        Text("This is from Custom UI", color = Color.Cyan)
                    },
                    detailsContent = detailsContent,
                    controlsContent = controlsContent,
                    onBackPressed = onBackPressed,
                    onCallAction = onCallAction,
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
    }
}