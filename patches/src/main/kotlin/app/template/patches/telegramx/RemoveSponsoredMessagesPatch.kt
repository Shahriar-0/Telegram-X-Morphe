package app.template.patches.telegramx

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

@Suppress("unused")
val removeSponsoredMessagesPatch = bytecodePatch(
    name = "Remove Telegram X sponsored messages",
    description = "Prevents Telegram X from requesting sponsored messages in chats.",
    default = true
) {
    compatibleWith(COMPATIBILITY_TELEGRAM_X)

    execute {
        RequestSponsoredMessagesFingerprint.method.returnEarly()
    }
}