package util

import app.morphe.patcher.Patcher
import app.morphe.patcher.PatcherConfig
import app.morphe.patcher.dex.SdkDexVerifier
import app.morphe.patcher.dex.DexVerifier
import app.morphe.patcher.dex.DexVerificationException
import java.util.zip.ZipFile
import app.morphe.patcher.patch.loadPatchesFromJar
import kotlinx.coroutines.runBlocking
import java.io.File

/** Applies the released bundle to a clean APK and compiles all resulting DEX files. */
fun main(args: Array<String>) = runBlocking {
    require(args.size == 2) { "Usage: validateMonet <bundle.mpp> <original.apk>" }
    val patches = loadPatchesFromJar(setOf(File(args[0]))).filter { it.name == "Monet accent theme" }.toSet()
    check(patches.size == 1)
    val sdkVerifier = SdkDexVerifier(File(requireNotNull(System.getenv("ANDROID_HOME"))), "36.0.0")
    val baseline = File("build/monet-original-dex").apply { mkdirs() }
    ZipFile(args[1]).use { zip ->
        zip.entries().asSequence().filter { it.name.matches(Regex("classes[0-9]*\\.dex")) }.forEach { entry ->
            zip.getInputStream(entry).use { input -> baseline.resolve(entry.name).outputStream().use { input.copyTo(it) } }
        }
    }
    fun hierarchyIssues(directory: File): String? = try {
        sdkVerifier.verifyDexDirectory(directory)
        null
    } catch (e: DexVerificationException) { e.message }
    val originalIssues = hierarchyIssues(baseline)
    val verifier = object : DexVerifier by sdkVerifier {
        override fun verifyDexDirectory(dexDir: File) {
            check(hierarchyIssues(dexDir) == originalIssues) { "Patched APK introduces class hierarchy issues" }
            println("DEX verification passed; hierarchy issues match the original APK baseline")
        }
    }
    Patcher(PatcherConfig(File(args[1]), temporaryFilesPath = File("build/monet-validation"),
        verifier = verifier)).use { patcher ->
        patcher += patches
        patcher().collect { result ->
            result.exception?.let { throw it }
            println("Applied ${result.patch.name}")
        }
        val result = patcher.get()
        println("Compiled patched DEX: $result")
    }
}
