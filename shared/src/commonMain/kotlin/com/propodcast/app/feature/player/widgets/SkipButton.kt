package com.propodcast.app.feature.player.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.ic_forward
import com.propodcast.app.resources.ic_replay
import com.propodcast.app.resources.player_seek_back
import com.propodcast.app.resources.player_seek_forward
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Circular arrow with the number of seconds inside, like "↺15" / "30↻". */
@Composable
internal fun SkipButton(
    seconds: Int,
    forward: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier.size(56.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(if (forward) Res.drawable.ic_forward else Res.drawable.ic_replay),
                contentDescription = stringResource(
                    if (forward) Res.string.player_seek_forward else Res.string.player_seek_back,
                    seconds,
                ),
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = seconds.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                // The circle's centre sits slightly below the icon's centre.
                modifier = Modifier
                    .offset(y = 2.dp)
                    .clearAndSetSemantics {},
            )
        }
    }
}
