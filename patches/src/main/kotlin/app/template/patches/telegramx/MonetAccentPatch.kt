package app.template.patches.telegramx

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val MONET = "Lapp/template/extension/extension/TelegramXMonetExtension;"
private fun target(owner: String, method: String, args: List<String>, result: String) =
    Fingerprint(definingClass = owner, name = method, parameters = args, returnType = result)

@Suppress("unused")
val monetAccentPatch = bytecodePatch(
    name = "Monet accent theme",
    description = "Adds Android’s wallpaper-based Monet palette to Telegram X surfaces, text, and controls.",
    default = true
) {
    compatibleWith(COMPATIBILITY_TELEGRAM_X)
    extendWith("extensions/extension.mpe")
    execute {
        target("Lorg/thunderdog/challegram/MainActivity;", "onCreate", listOf("Landroid/os/Bundle;"), "V")
            .method.addInstructions(0, "invoke-static/range {p0 .. p0}, $MONET->initialize(Landroid/content/Context;)V")
        target("Lu85;", "Cb", listOf("Ljava/util/ArrayList;", "Ljava/util/ArrayList;", "Z"), "V")
            .method.addInstructions(0, "invoke-static {p0, p2}, $MONET->addTheme(Ljava/lang/Object;Ljava/util/ArrayList;)V")
        val row = target("Lu85;", "Jb", listOf("Lfe6;"), "Lu13;").method
        row.implementation!!.instructions.withIndex().filter { it.value.opcode == Opcode.RETURN_OBJECT }
            .reversed().forEach { (index, instruction) ->
                val register = (instruction as OneRegisterInstruction).registerA
                row.addInstructions(index, "invoke-static {v$register}, $MONET->updateRow(Ljava/lang/Object;)V")
            }
        val click = target("Lu85;", "onClick", listOf("Landroid/view/View;"), "V").method
        click.addInstructions(0, """
            move-object/from16 v0, p0
            move-object/from16 v1, p1
            invoke-static {v0, v1}, $MONET->onClick(Ljava/lang/Object;Landroid/view/View;)Z
            move-result v0
            if-eqz v0, :monet_native_click
            return-void
            :monet_native_click
            nop
        """.trimIndent())
        val longClick = target("Lu85;", "onLongClick", listOf("Landroid/view/View;"), "Z").method
        longClick.addInstructions(0, """
            invoke-static {p1}, $MONET->isMonetView(Landroid/view/View;)Z
            move-result v0
            if-eqz v0, :monet_native_long_click
            return v0
            :monet_native_long_click
            nop
        """.trimIndent())
        target("Lse6;", "o", listOf("Lxn0;", "Z"), "V").method.addInstructions(
            0, "invoke-static {}, $MONET->invalidatePalette()V"
        )
        val changed = target("Lu85;", "d2", listOf("Lee6;", "Lee6;"), "V").method
        // Capture the controller before native code reuses its parameter register.
        changed.addInstructions(0, "invoke-static {p0}, $MONET->scheduleSelection(Ljava/lang/Object;)V")
        // R8 inlines some global lookups. Route all original interface lookups through
        // an active-theme-aware bridge; leave extension code out to prevent recursion.
        val affected = mutableListOf<String>()
        classDefForEach { cls ->
            if (!cls.type.startsWith("Lapp/template/extension/")) {
                val containsLookup = cls.methods.any { method ->
                    method.implementation?.instructions?.any { instruction ->
                        val ref = (instruction as? ReferenceInstruction)?.reference as? MethodReference
                        ref?.definingClass == "Lee6;" && ref.name in setOf("c", "d") &&
                            ref.parameterTypes == listOf("I") && ref.returnType in setOf("F", "I")
                    } == true
                }
                if (containsLookup) affected.add(cls.type)
            }
        }
        var rewritten = 0
        for (owner in affected) {
            for (method in mutableClassDefBy(owner).methods) {
                val instructions = method.implementation?.instructions ?: continue
                for ((index, instruction) in instructions.withIndex().toList()) {
                    val ref = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: continue
                    if (ref.definingClass != "Lee6;" || ref.parameterTypes != listOf("I")) continue
                    val bridge = when {
                        ref.name == "d" && ref.returnType == "I" -> "delegateColor(Ljava/lang/Object;I)I"
                        ref.name == "c" && ref.returnType == "F" -> "delegateProperty(Ljava/lang/Object;I)F"
                        else -> continue
                    }
                    val call = when (instruction.opcode) {
                        Opcode.INVOKE_INTERFACE -> {
                            val registers = instruction as FiveRegisterInstruction
                            "invoke-static {v${registers.registerC}, v${registers.registerD}}, $MONET->$bridge"
                        }
                        Opcode.INVOKE_INTERFACE_RANGE -> {
                            val registers = instruction as RegisterRangeInstruction
                            "invoke-static/range {v${registers.startRegister} .. v${registers.startRegister + 1}}, $MONET->$bridge"
                        }
                        else -> continue
                    }
                    method.replaceInstruction(index, call)
                    rewritten++
                }
            }
        }
        check(rewritten > 0) { "No Telegram X theme lookups found" }
        println("Monet: routed $rewritten theme lookups across ${affected.size} classes")
        for ((stub, native, result) in listOf(
            Triple("nativeColor", "d(I)I", "I"), Triple("nativeProperty", "c(I)F", "F")
        )) {
            target(MONET, stub, listOf("Ljava/lang/Object;", "I"), result).method.addInstructions(0, """
                check-cast p0, Lee6;
                invoke-interface {p0, p1}, Lee6;->$native
                move-result p1
                return p1
            """.trimIndent())
        }
        for ((stub, native, result) in listOf(
            Triple("nativeId", "getId()I", "I"), Triple("nativeDark", "b()Z", "Z")
        )) {
            target(MONET, stub, listOf("Ljava/lang/Object;"), result).method.addInstructions(0, """
                check-cast p0, Lee6;
                invoke-interface {p0}, Lee6;->$native
                move-result p0
                return p0
            """.trimIndent())
        }
    }
}
