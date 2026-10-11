package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.presentation.screens.notification.NotificationStateHolder
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme

/**
 * Global persistent TopBar across MainScaffold screens.
 * Both Profile Avatar (Left) and Month Selector (Center) use the unified global
 * [AppDropdownContainer] / [AppDropdownMenu] engine with smooth unclipped RoundRect shadow
 * and instant tap handling.
 */
@Composable
fun GlobalTopBar(
    months: List<String>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasUnreadNotifications: Boolean = NotificationStateHolder.hasUnread,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    var isProfileMenuOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(52.dp)
                .padding(horizontal = 20.dp)
        ) {
            // Left: Profile Avatar with Global Dropdown Menu (AppDropdownContainer)
            AppDropdownContainer(
                expanded = isProfileMenuOpen,
                onExpandedChange = { newExpanded ->
                    isProfileMenuOpen = newExpanded
                    if (newExpanded) onProfileClick()
                },
                dropdownName = "ProfileDropdown",
                menuAlignment = Alignment.TopStart,
                menuWidth = 248.dp,
                popupOffsetY = 44.dp,
                modifier = Modifier.align(Alignment.CenterStart),
                trigger = { onToggle ->
                    Surface(
                        onClick = onToggle,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.cd_profile),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            ) { closeDropdown ->
                // Profile Header Item inside Global Dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "BB",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Biu Biu",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = stringResource(R.string.badge_pro),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "biubiu@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    thickness = 0.8.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )

                AppDropdownActionItem(
                    label = stringResource(R.string.settings_title),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        closeDropdown()
                        onSettingsClick()
                    }
                )

                AppDropdownActionItem(
                    label = stringResource(R.string.menu_help_support),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        closeDropdown()
                    }
                )
            }

            // Center: Month Selector Dropdown Pill (Matching Aktivitas pill style)
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                AppDropdownMenu(
                    items = months,
                    selectedItem = selectedMonth,
                    onItemSelected = onMonthSelected,
                    dropdownName = "HomeMonthDropdown",
                    triggerShape = CircleShape,
                    triggerHeight = 38.dp,
                    triggerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    triggerBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    menuAlignment = Alignment.TopCenter,
                    menuWidth = 196.dp
                )
            }

            // Right: Notification Bell Button (Matching Aktivitas pill style)
            Surface(
                onClick = onNotificationClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.CenterEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier.size(21.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = stringResource(R.string.cd_notifications),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                        if (hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(8.5.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MoneyTrackerTheme.colors.notificationBadge)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
