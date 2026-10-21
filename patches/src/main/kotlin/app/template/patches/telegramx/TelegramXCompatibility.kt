package app.template.patches.telegramx

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal val COMPATIBILITY_TELEGRAM_X = Compatibility(
    name = "Telegram X",
    packageName = "org.thunderdog.challegram",
    apkFileType = ApkFileType.APK,
    appIconColor = 0x3F73A8,
    targets = listOf(
        AppTarget(version = "0.29.1.1817-universal")
    )
)