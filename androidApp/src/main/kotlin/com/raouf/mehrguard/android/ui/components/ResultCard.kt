/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raouf.mehrguard.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raouf.mehrguard.android.R
import com.raouf.mehrguard.android.ui.theme.*
import com.raouf.mehrguard.model.Verdict

/**
 * Result Card displaying scan analysis results.
 *
 * Features:
 * - Animated score display
 * - Verdict badge with color coding
 * - Risk factors list
 * - Copy URL functionality
 * - Full accessibility support
 */
@Composable
fun ResultCard(
    url: String,
    score: Int,
    verdict: Verdict,
    flags: List<String>,
    onDismiss: () -> Unit,
    onScanAnother: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    val (color, emoji) = when (verdict) {
        Verdict.SAFE -> VerdictSafe to "✅"
        Verdict.SUSPICIOUS -> VerdictWarning to "⚠️"
        Verdict.MALICIOUS -> VerdictDanger to "🚨"
        Verdict.UNKNOWN -> VerdictUnknown to "❓"
    }

    // Animate score counting up
    var animatedScore by remember { mutableStateOf(0) }
    LaunchedEffect(score) {
        animate(
            initialValue = 0f,
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedScore = value.toInt()
        }
    }

    val resultDesc = stringResource(R.string.cd_scan_result_full, verdict.name, score, url, flags.size)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .semantics {
                contentDescription = resultDesc
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated emoji with pulse effect
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emoji_scale"
        )

        Text(
            text = emoji,
            fontSize = (64 * scale).sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Score
        val scoreScale by animateFloatAsState(
            targetValue = if (animatedScore == score) 1.2f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "score_pop"
        )

        val riskScoreDesc = stringResource(R.string.cd_risk_score, score)
        Text(
            text = "$animatedScore",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier
                .scale(scoreScale)
                .semantics {
                contentDescription = riskScoreDesc
            }
        )

        Text(
            text = stringResource(R.string.risk_score),
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Verdict Badge
        Surface(
            color = color.copy(alpha = 0.2f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.5f)) // Added glowing border
        ) {
            Text(
                text = verdict.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // URL Card
        val scannedUrlDesc = stringResource(R.string.cd_scanned_url, url)
        val copyUrlDesc = stringResource(R.string.cd_copy_url)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = scannedUrlDesc
                },
            colors = CardDefaults.cardColors(containerColor = BackgroundCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.scanned_url),
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(url))
                        },
                        modifier = Modifier.semantics {
                            contentDescription = copyUrlDesc
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.copy),
                            fontSize = 12.sp,
                            color = BrandPrimary
                        )
                    }
                }

                Text(
                    text = url,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    maxLines = 4
                )
            }
        }

        // Risk Factors
        if (flags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "${flags.size} risk factors detected"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ ${stringResource(R.string.risk_factors)} (${flags.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    flags.take(5).forEach { flag ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "• ",
                                color = color,
                                fontSize = 12.sp
                            )
                            Text(
                                text = flag,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (flags.size > 5) {
                        Text(
                            text = "+${flags.size - 5} more",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Safety recommendation
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (verdict) {
                    Verdict.SAFE -> VerdictSafe.copy(alpha = 0.1f)
                    Verdict.SUSPICIOUS -> VerdictWarning.copy(alpha = 0.1f)
                    Verdict.MALICIOUS -> VerdictDanger.copy(alpha = 0.1f)
                    Verdict.UNKNOWN -> BackgroundCard
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = when (verdict) {
                    Verdict.SAFE -> stringResource(R.string.recommendation_safe)
                    Verdict.SUSPICIOUS -> stringResource(R.string.recommendation_suspicious)
                    Verdict.MALICIOUS -> stringResource(R.string.recommendation_malicious)
                    Verdict.UNKNOWN -> stringResource(R.string.recommendation_unknown)
                },
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        val dismissDesc = stringResource(R.string.cd_dismiss_result)
        val scanAnotherDesc = stringResource(R.string.cd_scan_another)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics {
                        contentDescription = dismissDesc
                    },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text(stringResource(R.string.done))
            }

            Button(
                onClick = onScanAnother,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics {
                        contentDescription = scanAnotherDesc
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary
                )
            ) {
                Text(stringResource(R.string.scan_another))
            }
        }
    }
}
