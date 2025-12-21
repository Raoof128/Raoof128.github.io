package com.qrshield.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.focusable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern

@Composable
fun TrustCentreScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.trustCentre()
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F8FA))
                .gridPattern(spacing = 40.dp, lineColor = Color(0xFFE1E4E8), lineWidth = 1.dp)
        ) {
            TrustCentreSidebar(onNavigate = { viewModel.currentScreen = it })
            TrustCentreContent(
                viewModel = viewModel,
                onNavigate = { viewModel.currentScreen = it }
            )
        }
    }
}

@Composable
private fun TrustCentreSidebar(onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .width(288.dp)
            .fillMaxHeight()
            .background(Color.White)
            .border(1.dp, Color(0xFFD0D7DE))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFD0D7DE))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource("assets/stitch/logo-shield.png"),
                        contentDescription = "Shield Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column {
                    Text("QR-SHIELD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24292F))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2EA043))
                        )
                        Text("v2.4.0 Offline", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF57606A))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SidebarLink(label = "Dashboard", icon = "dashboard", onClick = { onNavigate(AppScreen.Dashboard) })
                SidebarLink(label = "Scan History", icon = "history", onClick = { onNavigate(AppScreen.ScanHistory) })
                SidebarLink(label = "Trust Centre", icon = "verified_user", isActive = true, onClick = { })
                SidebarLink(label = "Threat Intel", icon = "public", onClick = { onNavigate(AppScreen.ResultDangerous) })
                SidebarLink(label = "Settings", icon = "settings", onClick = { onNavigate(AppScreen.TrustCentreAlt) })
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFD0D7DE))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF135BEC), Color(0xFF9333EA)))),
                contentAlignment = Alignment.Center
            ) {
                Text("", fontSize = 12.sp)
            }
            Column {
                Text("System Admin", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                Text("Licence: Pro", fontSize = 12.sp, color = Color(0xFF57606A))
            }
        }
    }
}

@Composable
private fun SidebarLink(label: String, icon: String, isActive: Boolean = false, onClick: () -> Unit) {
    val bg = if (isActive) Color(0xFF135BEC).copy(alpha = 0.05f) else Color.Transparent
    val border = if (isActive) Color(0xFF135BEC).copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isActive) Color(0xFF135BEC) else Color(0xFF57606A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialSymbol(name = icon, size = 20.sp, color = textColor)
        Text(label, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal, color = textColor)
    }
}

@Composable
private fun TrustCentreContent(viewModel: AppViewModel, onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Trust Centre & Privacy Controls", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24292F))
            Text("Manage offline heuristics, data retention policies, and domain allowlists. All changes apply immediately.", fontSize = 16.sp, color = Color(0xFF57606A), modifier = Modifier.widthIn(max = 640.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFF2EA043).copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                MaterialSymbol(
                    name = "shield_lock",
                    size = 180.sp,
                    color = Color(0xFF2EA043).copy(alpha = 0.1f),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                            Text("AIR-GAPPED STATUS: ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2EA043), letterSpacing = 1.2.sp)
                        }
                        Text("Strict Offline Guarantee", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24292F))
                        Text(
                            "QR-SHIELD operates entirely on your local hardware. No image data, scanned URLs, or telemetry are sent to the cloud for analysis.",
                            fontSize = 16.sp,
                            color = Color(0xFF57606A),
                            modifier = Modifier.widthIn(max = 520.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFD0D7DE)),
                        modifier = Modifier
                            .clickable { onNavigate(AppScreen.ReportsExport) }
                            .focusable()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialSymbol(name = "description", size = 16.sp, color = Color(0xFF24292F))
                            Text("View Audit Log", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD0D7DE))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialSymbol(name = "tune", size = 20.sp, color = Color(0xFF135BEC))
                            Text("Heuristic Sensitivity", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF135BEC).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFF135BEC).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("MODE: BALANCED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF135BEC))
                        }
                    }
                    Box(modifier = Modifier.height(48.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFE5E7EB))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF93C5FD), Color(0xFF135BEC))))
                        )
                        Column(
                            modifier = Modifier
                                .offset(x = 0.dp, y = 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Text("Low", fontSize = 12.sp, color = Color(0xFF57606A))
                        }
                        Column(
                            modifier = Modifier
                                .offset(x = 240.dp, y = (-4).dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF135BEC))
                                    .border(4.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                            }
                            Text("Balanced", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24292F))
                        }
                        Column(
                            modifier = Modifier
                                .offset(x = 480.dp, y = 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Text("Paranoia", fontSize = 12.sp, color = Color(0xFF57606A))
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD0D7DE))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Verdict Logic Explanation", fontSize = 13.sp, color = Color(0xFF57606A), fontWeight = FontWeight.Medium)
                    Text(
                        "Balanced Mode uses standard heuristic signatures. It flags known malicious patterns but allows common URL shorteners unless they redirect to suspicious TLDs.",
                        fontSize = 14.sp,
                        color = Color(0xFF24292F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Engine v4.1.2 • Sig DB: 2023-10-27", fontSize = 11.sp, color = Color(0xFF57606A))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ToggleCard(
                title = "Strict Offline Mode",
                subtitle = "Force disable all network adapters for app",
                enabled = viewModel.trustCentreToggles.strictOffline,
                activeColor = Color(0xFF2EA043),
                modifier = Modifier.weight(1f)
            )
            ToggleCard(
                title = "Anonymous Telemetry",
                subtitle = "Share threat signatures to improve DB",
                enabled = viewModel.trustCentreToggles.anonymousTelemetry,
                activeColor = Color(0xFF135BEC),
                modifier = Modifier.weight(1f)
            )
            ToggleCard(
                title = "Auto-copy Safe Links",
                subtitle = "Copy to clipboard if Verdict is SAFE",
                enabled = viewModel.trustCentreToggles.autoCopySafe,
                activeColor = Color(0xFF135BEC),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
            AllowListCard(modifier = Modifier.weight(1f))
            BlockListCard(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ToggleCard(title: String, subtitle: String, enabled: Boolean, activeColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD0D7DE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF57606A))
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (enabled) activeColor else Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(if (enabled) Alignment.CenterEnd else Alignment.CenterStart)
                        .offset(x = if (enabled) (-4).dp else 4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun AllowListCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD0D7DE))
    ) {
        Column(modifier = Modifier.height(400.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "check_circle", size = 20.sp, color = Color(0xFF2EA043))
                    Text("Domain Allowlist", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(name = "add", size = 20.sp, color = Color(0xFF57606A))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AllowItem("assets/stitch/favicon-google.png", "*.google.com")
                AllowItem("assets/stitch/favicon-company.png", "company-portal.net")
                AllowItem("assets/stitch/favicon-slack.png", "*.slack.com")
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "search", size = 16.sp, color = Color(0xFF57606A))
                    Text("Search domains...", fontSize = 13.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun AllowItem(imagePath: String, domain: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFD0D7DE))
            ) {
                Image(
                    painter = painterResource(imagePath),
                    contentDescription = domain,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
        }
        MaterialSymbol(name = "delete", size = 18.sp, color = Color(0xFFCF222E))
    }
}

@Composable
private fun BlockListCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD0D7DE))
    ) {
        Column(modifier = Modifier.height(400.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "block", size = 20.sp, color = Color(0xFFCF222E))
                    Text("Custom Blocklist", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(name = "add", size = 20.sp, color = Color(0xFF57606A))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BlockItem("bit.ly/*", badge = "WILDCARD")
                BlockItem("suspicious-domain.xyz")
                BlockItem("free-crypto-giveaway.net")
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "search", size = 16.sp, color = Color(0xFF57606A))
                    Text("Search rules...", fontSize = 13.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun BlockItem(domain: String, badge: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD29922).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFD29922).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD29922))
                }
            }
        }
        MaterialSymbol(name = "delete", size = 18.sp, color = Color(0xFFCF222E))
    }
}
