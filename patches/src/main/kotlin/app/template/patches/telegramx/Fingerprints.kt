package app.template.patches.telegramx

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

object RequestSponsoredMessagesFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(
            smali = "Lorg/drinkless/tdlib/TdApi\$GetChatSponsoredMessages;-><init>(J)V"
        )
    )
)