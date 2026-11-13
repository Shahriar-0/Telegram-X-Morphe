# 👋🧩 Morphe Patches template

Template repository for Morphe Patches.

## ❓ About

Patches for apps I like.

<!-- TODO: Update this about section with a brief introduction/summary about this repo and what it offers. -->

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=Shahriar-0/Telegram-X-Morphe

## Monet theme

Select **Monet accent theme** when patching Telegram X `0.29.1.1817-universal`.
On Android 12 or later, open **Settings → Themes → Color Theme → Monet**.
The full theme follows Android’s selected wallpaper palette, including accents,
page and toolbar surfaces, text, and chat bubbles. Telegram X’s day/night setting
chooses the light or dark palette. Choosing another color theme turns Monet off.
Chat wallpaper and semantic warning, success, verification, and identity colors remain native.

The patch is selected by default in Morphe. Monet becomes active only after you
choose it in Telegram X. Android versions before 12 retain the stock theme list.

To check the bytecode hooks against an original APK:

```sh
ANDROID_HOME="$HOME/Android/Sdk" ./gradlew :patches:validateMonet -PmonetApk=/absolute/path/to/Telegram-X.apk
```

This check applies the bundle and compiles/verifies its DEX files. Verify the
appearance, selection persistence, and wallpaper updates on a phone as well.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/bhashanaliyanage/Telegram-X-Morphe/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;2 patches total
<details open>
<summary>📦 Telegram X&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 0.29.1.1817-universal |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Monet accent theme](#monet-accent-theme) | Adds Android’s wallpaper-based Monet palette to Telegram X surfaces, text, and controls. |  |
| [Remove Telegram X sponsored messages](#remove-telegram-x-sponsored-messages) | Blocks Telegram X sponsored-message requests. |  |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

UserXYZ Patches are licensed under the [GNU General Public License v3.0](LICENSE)
