package app.template.patches.telegramx

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeSponsoredMessagesPatch = bytecodePatch(
    name = "Remove Telegram X sponsored messages",
    description = "Prevents Telegram X from requesting sponsored messages in chats.",
    default = true
) {
    compatibleWith(COMPATIBILITY_TELEGRAM_X)

    execute {
        // The fingerprint finds the method that creates the TDLib sponsored-message request.
        // Return immediately, before the request can be sent.
        RequestSponsoredMessagesFingerprint.method.addInstruction(0, "return-void")
    }
}