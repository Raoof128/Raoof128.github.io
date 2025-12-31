package com.raouf.mehrguard.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raouf.mehrguard.desktop.i18n.AppLanguage
import com.raouf.mehrguard.desktop.i18n.DesktopStrings
import com.raouf.mehrguard.desktop.theme.LocalStitchTokens

/**
 * Edit Profile Dialog (parity with Web app shared-ui.js showEditProfileModal)
 * 
 * Allows users to edit their profile information including:
 * - Name
 * - Email
 * - Role
 * - Initials (auto-generated from name if blank)
 */
@Composable
fun EditProfileDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    currentName: String,
    currentEmail: String,
    currentRole: String,
    currentInitials: String,
    onSave: (name: String, email: String, role: String, initials: String?) -> Unit,
    language: AppLanguage
) {
    if (!isVisible) return
    
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    
    var name by remember(currentName) { mutableStateOf(currentName) }
    var email by remember(currentEmail) { mutableStateOf(currentEmail) }
    var role by remember(currentRole) { mutableStateOf(currentRole) }
    var initials by remember(currentInitials) { mutableStateOf(currentInitials) }
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Auto-generate initials when name changes
    val autoInitials = remember(name) {
        generateInitialsFromName(name)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(400.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Preview avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (initials.isBlank()) autoInitials else initials,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Column {
                            Text(
                                text = t("Edit Profile"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMain
                            )
                            Text(
                                text = t("Update your information"),
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                    
                    IconButton(onClick = onDismiss) {
                        MaterialSymbol(name = "close", size = 20.sp, color = colors.textMuted)
                    }
                }
                
                HorizontalDivider(color = colors.border)
                
                // Form Fields
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Name field
                    ProfileTextField(
                        label = t("Name"),
                        value = name,
                        onValueChange = { name = it },
                        placeholder = t("Enter your name"),
                        focusRequester = focusRequester,
                        language = language
                    )
                    
                    // Email field
                    ProfileTextField(
                        label = t("Email"),
                        value = email,
                        onValueChange = { email = it },
                        placeholder = t("Enter your email"),
                        keyboardType = KeyboardType.Email,
                        language = language
                    )
                    
                    // Role field
                    ProfileTextField(
                        label = t("Role"),
                        value = role,
                        onValueChange = { role = it },
                        placeholder = t("e.g. Security Analyst"),
                        language = language
                    )
                    
                    // Initials field (optional - auto-generates if blank)
                    ProfileTextField(
                        label = t("Initials (optional)"),
                        value = initials,
                        onValueChange = { 
                            // Limit to 2 characters
                            if (it.length <= 2) initials = it.uppercase() 
                        },
                        placeholder = autoInitials,
                        hint = t("Leave blank to auto-generate"),
                        language = language
                    )
                }
                
                HorizontalDivider(color = colors.border)
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.handCursor()
                    ) {
                        Text(
                            text = t("Cancel"),
                            color = colors.textMuted
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = {
                            onSave(
                                name,
                                email,
                                role,
                                initials.ifBlank { null }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.handCursor()
                    ) {
                        MaterialSymbol(name = "check", size = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("Save Changes"),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    hint: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    focusRequester: FocusRequester? = null,
    language: AppLanguage = AppLanguage.English
) {
    val colors = LocalStitchTokens.current.colors
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSub
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = colors.textMuted
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                cursorColor = colors.primary,
                focusedTextColor = colors.textMain,
                unfocusedTextColor = colors.textMain
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (focusRequester != null) 
                        Modifier.focusRequester(focusRequester) 
                    else 
                        Modifier
                )
        )
        
        if (hint != null) {
            Text(
                text = hint,
                fontSize = 10.sp,
                color = colors.textMuted
            )
        }
    }
}

/**
 * Generates initials from a name string
 */
private fun generateInitialsFromName(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
        parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.isNotEmpty() -> parts[0].take(1).uppercase() + "U"
        else -> "QU"
    }
}
