package com.mercan.app.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercan.app.BuildConfig
import com.mercan.app.R
import com.mercan.app.domain.Settings
import com.mercan.app.domain.ThemeMode
import com.mercan.app.platform.AppLanguage
import com.mercan.app.platform.WidgetPin
import com.mercan.app.ui.components.Segmented
import com.mercan.app.ui.components.SeyirIcon
import com.mercan.app.ui.components.SeyirScreen
import com.mercan.app.ui.components.rememberFormats
import java.time.LocalTime

private const val SourceUrl = "https://sosyaltesisler.gop.edu.tr/yemekhane_menu.aspx"

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings = state ?: return
    SeyirScreen(stringResource(R.string.tab_settings)) {
        ReminderCard(settings, viewModel::setReminderOn, viewModel::setReminderMinute)
        WidgetCard()
        Card {
            Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleSmall)
            Segmented(
                options = ThemeMode.entries,
                selected = settings.theme,
                label = { theme ->
                    stringResource(
                        when (theme) {
                            ThemeMode.SYSTEM -> R.string.theme_system
                            ThemeMode.LIGHT -> R.string.theme_light
                            ThemeMode.DARK -> R.string.theme_dark
                        },
                    )
                },
                onSelect = viewModel::setTheme,
            )
        }
        Card {
            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleSmall)
            Segmented(
                options = AppLanguage.entries,
                selected = AppLanguage.current(),
                label = { language ->
                    stringResource(
                        when (language) {
                            AppLanguage.SYSTEM -> R.string.language_system
                            AppLanguage.ENGLISH -> R.string.language_en
                            AppLanguage.TURKISH -> R.string.language_tr
                        },
                    )
                },
                onSelect = AppLanguage::apply,
            )
        }
        AboutCard()
        Text(
            text = stringResource(R.string.disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) = RowCard {
    Column(Modifier.padding(16.dp), content = content)
}

/** Boşluğu satırlarına bırakan kart: basma efekti satırı kenardan kenara kaplar. */
@Composable
private fun RowCard(content: @Composable ColumnScope.() -> Unit) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer),
    content = content,
)

@Composable
private fun ReminderCard(settings: Settings, onToggle: (Boolean) -> Unit, onMinute: (Int) -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var allowed by remember { mutableStateOf(true) }
    var picking by rememberSaveable { mutableStateOf(false) }
    val check = { allowed = NotificationManagerCompat.from(context).areNotificationsEnabled() }
    LifecycleResumeEffect(context) {
        check()
        onPauseOrDispose {}
    }
    val permission = rememberLauncherForActivityResult(RequestPermission()) { check() }
    val time = LocalTime.ofSecondOfDay(settings.reminderMinute * 60L)

    RowCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = settings.reminderOn, role = Role.Switch) { on ->
                    onToggle(on)
                    if (on && !allowed && Build.VERSION.SDK_INT >= 33) {
                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_reminder), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(R.string.settings_reminder_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = settings.reminderOn,
                onCheckedChange = null,
                thumbContent = {},
                colors = SwitchDefaults.colors(
                    checkedTrackColor = scheme.primary,
                    checkedThumbColor = scheme.onPrimary,
                    uncheckedTrackColor = scheme.outlineVariant,
                    uncheckedThumbColor = scheme.surface,
                    uncheckedBorderColor = scheme.outlineVariant,
                ),
            )
        }
        if (settings.reminderOn) {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = scheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = stringResource(R.string.settings_reminder_time),
                        role = Role.Button,
                    ) { picking = true }
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rememberFormats().time(time),
                    style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                )
                SeyirIcon(R.drawable.ic_seyir_next, tint = scheme.onSurfaceVariant)
            }
            if (!allowed) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.notifications_blocked),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { context.openNotificationSettings() }) {
                        Text(stringResource(R.string.action_open_settings))
                    }
                }
            }
        }
    }
    if (picking) {
        TimeDialog(
            initial = time,
            onSave = {
                onMinute(it)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

/** Ana ekran uygulaması içeriden eklemeyi desteklemiyorsa görünmez. */
@Composable
private fun WidgetCard() {
    val context = LocalContext.current
    if (!remember(context) { WidgetPin.isSupported(context) }) return
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    RowCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = stringResource(R.string.widget_add), role = Role.Button) {
                    WidgetPin.request(context)
                }
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.settings_widget), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(R.string.widget_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted,
                )
            }
            SeyirIcon(R.drawable.ic_seyir_next, tint = muted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(initial: LocalTime, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    val picker = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reminder_time), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { TimePicker(picker) }
        },
        confirmButton = {
            TextButton(onClick = { onSave(picker.hour * 60 + picker.minute) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun AboutCard() {
    val links = LocalUriHandler.current
    RowCard {
        AboutRow(
            label = stringResource(R.string.settings_source),
            value = stringResource(R.string.source_host),
            modifier = Modifier.clickable(role = Role.Button) {
                // Tarayıcısı olmayan telefonda sessizce geç.
                runCatching { links.openUri(SourceUrl) }
            },
        )
        HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
        AboutRow(stringResource(R.string.settings_version), BuildConfig.VERSION_NAME)
    }
}

@Composable
private fun AboutRow(label: String, value: String, modifier: Modifier = Modifier) = FlowRow(
    modifier = modifier
        .fillMaxWidth()
        .semantics(mergeDescendants = true) {}
        .heightIn(min = 56.dp)
        .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalArrangement = Arrangement.Center,
    itemVerticalAlignment = Alignment.CenterVertically,
) {
    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 12.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= 26) {
        Intent(ACTION_APP_NOTIFICATION_SETTINGS).putExtra(EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    }
    startActivity(intent)
}
