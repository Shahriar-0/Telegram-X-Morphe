# Telegram X 1817 Monet hooks

These mappings were traced from all three DEX files of the official universal
0.29.1.1817 APK and compared with the matching upstream source tag. They are
version-specific; do not reuse them for another release without checking it.

| Source role | APK descriptor |
| --- | --- |
| ThemeManager singleton | `Lse6;->j()Lse6;` |
| Current theme | `Lse6;->d(ZZ)Lee6;` |
| Theme color notification | `Lse6;->o(Lxn0;Z)V` |
| Theme delegate / dark flag | `Lee6;` / `b()Z` |
| Theme color lookup | `Ln09;->j(I)I`, `k(II)I`, `l(I)I` |
| Color name to ID map | `Lo09;->a()Ljava/util/Map;` |
| Themes controller | `Lu85;` |
| Theme group builder | `Lu85;->Cb(Ljava/util/ArrayList;Ljava/util/ArrayList;Z)V` |
| Theme row builder | `Lu85;->Jb(Lfe6;)Lu13;` |
| Builtin theme list | `Lu85;->V1:Ljava/util/ArrayList;` |
| Settings adapter / items | `Lu85;->P1:Lpf0;` / `Lq45;->Z:Ljava/util/ArrayList;` |
| Adapter notifyDataSetChanged | `Landroidx/recyclerview/widget/f;->l()V` |
| Theme info constructor | `Lfe6;-><init>(Ljava/lang/String;Ljava/lang/String;III)V` |
| Theme info ID | `Lfe6;->a:I` |
| ListItem ID / theme ID / selected flag | `Lu13;->b:I` / `s:I` / `f(Z)V` |
| Theme row view ID | `0x7f0903bf` |

Monet uses a synthetic negative theme ID only in the theme picker. It never
writes that ID to Telegram X's own theme storage or asks its theme loader to
load it. Selecting the row saves an independent preference; native theme
selection clears it. The underlying native theme remains available for dark
mode, theme properties, and fallback colors.

Original `Lee6.d(I)I` color and `Lee6.c(I)F` property interface calls are routed
through active-theme-aware bridges, including calls R8 inlined outside `Theme`.
Native bridge bodies are injected after the sweep, so they invoke the original
interface directly and cannot recurse through themselves. The object and ID
registers are forwarded unchanged; the original move-result instruction stays
in place. Inactive theme IDs use native colors and properties.

Resources come from the application context, so Android's selected dynamic
palette overlays (including SPRITZ) apply. Quick Settings on the test phone
resolves `system_accent1_200` to `#C4C8BA`.

| Role | Light | Dark |
| --- | --- | --- |
| Accent | accent1_600 | accent1_200 |
| Accent container | accent1_100 | accent1_800 |
| Accent content | accent1_0 | accent1_900 |
| Page | neutral1_50 | neutral1_950 |
| Surface | neutral1_0 | neutral1_900 |
| Text/icons | neutral1_900 | neutral1_100 |
| Secondary text/icons | neutral2_700 | neutral2_300 |
| Outline | neutral2_200 | neutral2_700 |
| Container content | accent1_900 | accent1_100 |

Bindings are explicit name-to-role assignments. Solid surfaces are opaque;
overlays, ripples, and selection highlights preserve their native alpha.
Android lacks a neutral1_950 resource; the dark page shade is interpolated in
linear light between the selected neutral1_900 and neutral1_1000 colors. Other
missing system resources fall back individually to the original theme color.
Wallpaper images, warning/error/success colors, verification, avatar identities,
and media overlays are excluded. Native theme swatches remain native; only the
synthetic Monet row uses the dynamic control accent as its radio color.

Property 18 (`lightStatusBar`) follows the mapped surface for the active theme.
The Activity's status/navigation bar icon appearance is also refreshed on
selection and configuration changes, since a color-only notification does not
run Telegram X's full theme-change window callback.
