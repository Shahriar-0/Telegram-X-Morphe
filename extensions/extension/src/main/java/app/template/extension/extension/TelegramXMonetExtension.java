package app.template.extension.extension;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import android.app.Activity;
import android.view.WindowInsetsController;

/** Runtime bridge for the verified, obfuscated Telegram X 1817 theme API. */
@SuppressLint("ResourceType") // IDs belong to the target APK, not the extension module.
@SuppressWarnings({"unused", "unchecked"})
public final class TelegramXMonetExtension {
    private static final int MONET_ID = -2147483000;
    private static final String TAG = "MorpheMonet";
    private static SharedPreferences preferences;
    private static ClassLoader loader;
    private static Context application;
    private static WeakReference<Activity> activity = new WeakReference<>(null);
    private static boolean enabled;
    private static final Map<Integer, Binding> roles = new HashMap<>();
    private static final int PRIMARY = 0, CONTAINER = 1, ON_PRIMARY = 2, PAGE = 3,
        SURFACE = 4, TEXT = 5, SECONDARY = 6, OUTLINE = 7, ON_CONTAINER = 8;
    private static final String[] FAMILIES = {"accent1", "accent1", "accent1", "neutral1",
        "neutral1", "neutral1", "neutral2", "neutral2", "accent1"};
    private static final int[] LIGHT = {600, 100, 0, 50, 0, 900, 700, 200, 900};
    private static final int[] DARK = {200, 800, 900, 950, 900, 100, 300, 700, 100};
    private static final int[] palette = new int[9];
    private static final boolean[] available = new boolean[9];
    private static boolean paletteValid;
    private static boolean paletteDark;
    private static boolean loggedPalette;
    private static Method managerInstance, currentTheme;
    private static Object activeTheme;
    private static int activeThemeId;
    private static int monetMarkerColor;

    private static final class Binding {
        final int role;
        final boolean alpha;
        Binding(int role, boolean alpha) { this.role = role; this.alpha = alpha; }
    }

    private static void bind(Map<String, Integer> colors, int role, boolean alpha, String... names) {
        for (String name : names) {
            Integer id = colors.get(name);
            if (id != null) roles.put(id, new Binding(role, alpha));
            else Log.w(TAG, "Unknown theme role: " + name);
        }
    }

    public static synchronized void initialize(Context context) {
        if (context instanceof Activity) activity = new WeakReference<>((Activity) context);
        if (preferences != null || Build.VERSION.SDK_INT < 31) return;
        Context app = context.getApplicationContext();
        application = app;
        loader = app.getClassLoader();
        preferences = app.getSharedPreferences("morphe_monet", Context.MODE_PRIVATE);
        enabled = preferences.getBoolean("enabled", false);
        try {
            Map<String, Integer> colors = (Map<String, Integer>) type("o09").getMethod("a").invoke(null);
            monetMarkerColor = colors.get("controlActive");
            managerInstance = type("se6").getMethod("j");
            currentTheme = type("se6").getMethod("d", boolean.class, boolean.class);
            bind(colors, PRIMARY, false, "controlActive", "iconActive", "progress", "chatSendButton",
                "textLink", "textNeutral", "textSearchQueryHighlight", "sliderActive", "checkActive",
                "togglerActive", "togglerPositive", "circleButtonRegular", "chatListAction", "badge",
                "inlineText", "inlineOutline", "inlineIcon", "bubbleIn_progress", "bubbleOut_progress",
                "bubbleIn_textLink", "bubbleOut_textLink", "bubbleOut_inlineText", "bubbleOut_inlineOutline",
                "bubbleOut_inlineIcon", "iv_textLink", "iv_textMarkedLink", "headerTabActive",
                "headerTabActiveText", "inputActive", "playerButtonActive", "unreadText", "profileSectionActive",
                "profileSectionActiveContent", "messageAuthor", "bubbleOut_messageAuthor");
            bind(colors, CONTAINER, false, "togglerActiveBackground", "togglerPositiveBackground",
                "bubbleOut_background", "chatKeyboardButton", "unread");
            bind(colors, CONTAINER, true, "textSelectionHighlight", "textLinkPressHighlight", "messageSelection",
                "bubble_messageSelection", "bubble_messageSelectionNoWallpaper", "bubbleIn_textLinkPressHighlight",
                "bubbleOut_textLinkPressHighlight", "iv_textLinkPressHighlight", "fillingActive", "bubbleIn_fillingActive",
                "bubbleOut_fillingActive");
            bind(colors, ON_PRIMARY, false, "controlContent", "checkContent", "circleButtonRegularIcon",
                "badgeText", "togglerPositiveContent", "inlineContentActive");
            bind(colors, PAGE, false, "background", "chatBackground", "statusBar", "statusBarLegacy");
            bind(colors, SURFACE, false, "filling", "fillingPressed", "headerBackground", "headerLightBackground",
                "headerPickerBackground", "bubbleIn_background", "chatKeyboard", "overlayFilling", "drawer",
                "playerCoverPlaceholder", "iv_preBlockBackground", "iv_chatLinkBackground", "iv_textReferenceBackground");
            bind(colors, SURFACE, true, "bubble_chatBackground", "previewBackground");
            bind(colors, TEXT, false, "text", "headerText", "headerIcon", "headerButton", "headerLightText",
                "headerLightIcon", "headerPickerText", "drawerText", "icon", "iv_icon", "playerButton",
                "bubbleIn_text", "iv_text", "iv_header", "iv_pageTitle", "ivHeader", "iv_blockQuote",
                "iv_pullQuote", "iv_textReference", "fillingActiveContent", "bubbleIn_fillingActiveContent");
            bind(colors, TEXT, true, "headerTabInactiveText", "statusBarContent", "statusBarLegacyContent");
            bind(colors, ON_CONTAINER, false, "bubbleOut_text", "bubbleOut_fillingActiveContent");
            bind(colors, SECONDARY, false, "textLight", "textPlaceholder", "background_text", "background_textLight",
                "background_icon", "iconLight", "controlInactive", "chatListIcon", "chatListMute", "inputInactive",
                "bubbleIn_time", "bubbleOut_time", "iv_caption", "iv_pageAuthor", "iv_pageFooter", "iv_pageSubtitle");
            bind(colors, OUTLINE, false, "separator", "chatSeparator", "bubbleIn_separator", "bubbleOut_separator",
                "bubbleIn_outline", "bubbleOut_outline", "iv_separator", "sliderInactive", "togglerInactive",
                "togglerInactiveBackground");
            bind(colors, OUTLINE, true, "bubble_chatSeparator", "shareSeparator");
            IntentFilter changes = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
            changes.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            app.registerReceiver(new BroadcastReceiver() {
                @Override public void onReceive(Context c, Intent i) { refresh(); }
            }, changes);
        } catch (ReflectiveOperationException e) { Log.e(TAG, "Cannot bind theme colors", e); }
    }

    private static Class<?> type(String name) throws ClassNotFoundException {
        return Class.forName(name, true, loader);
    }
    private static Object field(Object object, String name) throws ReflectiveOperationException {
        for (Class<?> c = object.getClass(); c != null; c = c.getSuperclass()) {
            try { Field f = c.getDeclaredField(name); f.setAccessible(true); return f.get(object); }
            catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }
    private static int id(Object item) throws ReflectiveOperationException {
        return (Integer) field(item, "s");
    }
    public static void addTheme(Object controller, ArrayList<Object> themes) {
        if (preferences == null) return;
        try {
            if (themes != field(controller, "V1")) return;
            for (Object theme : themes) if ((Integer) field(theme, "a") == MONET_ID) return;
            Object theme = type("fe6").getConstructor(String.class, String.class, int.class, int.class, int.class)
                .newInstance("Monet", null, MONET_ID, 1, 0);
            themes.add(theme);
        } catch (ReflectiveOperationException e) { Log.e(TAG, "Cannot add Monet theme", e); }
    }
    public static void updateRow(Object item) {
        if (preferences == null) return;
        try {
            if (id(item) == MONET_ID) item.getClass().getField("w").setInt(item, monetMarkerColor);
            if (id(item) == MONET_ID || enabled)
                item.getClass().getMethod("f", boolean.class).invoke(item, enabled && id(item) == MONET_ID);
        } catch (ReflectiveOperationException e) { Log.e(TAG, "Cannot update theme row", e); }
    }
    public static boolean isMonetView(View view) {
        if (view.getId() != 0x7f0903bf || preferences == null) return false;
        try { return id(view.getTag()) == MONET_ID; }
        catch (ReflectiveOperationException | NullPointerException e) { return false; }
    }
    public static boolean onClick(Object controller, View view) {
        if (preferences == null || view.getId() != 0x7f0903bf) return false;
        try {
            boolean monet = id(view.getTag()) == MONET_ID;
            enabled = monet;
            preferences.edit().putBoolean("enabled", monet).apply();
            refresh();
            Object adapter = field(controller, "P1");
            for (Object item : (ArrayList<?>) field(adapter, "Z")) {
                if ((Integer) field(item, "b") == 0x7f0903bf) {
                    int themeId = id(item);
                    boolean selected = monet ? themeId == MONET_ID : themeId == id(view.getTag());
                    item.getClass().getMethod("f", boolean.class).invoke(item, selected);
                }
            }
            adapter.getClass().getMethod("l").invoke(adapter);
            return monet;
        } catch (ReflectiveOperationException e) { Log.e(TAG, "Cannot select Monet theme", e); return false; }
    }
    public static void invalidatePalette() { paletteValid = false; }
    public static void scheduleSelection(Object controller) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            updateSelection(controller);
            updateSystemBars();
        });
    }
    public static void updateSelection(Object controller) {
        if (!enabled) return;
        try {
            Object adapter = field(controller, "P1");
            if (adapter == null) return;
            for (Object item : (ArrayList<?>) field(adapter, "Z"))
                if ((Integer) field(item, "b") == 0x7f0903bf) updateRow(item);
            adapter.getClass().getMethod("l").invoke(adapter);
        } catch (ReflectiveOperationException e) { Log.e(TAG, "Cannot refresh selection", e); }
    }
    private static void updateSystemBars() {
        if (Build.VERSION.SDK_INT < 31) return;
        Activity current = activity.get();
        if (current == null || current.isFinishing() || !loadPalette()) return;
        WindowInsetsController bars = current.getWindow().getInsetsController();
        if (bars == null) return;
        int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
        boolean dark = enabled ? paletteDark : nativeDark(activeTheme);
        boolean lightStatus = enabled ? !dark : nativeProperty(activeTheme, 18) == 1f;
        int appearance = (lightStatus ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0)
            | (!dark ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0);
        bars.setSystemBarsAppearance(appearance, mask);
    }
    private static void refresh() {
        invalidatePalette();
        try {
            Object manager = type("se6").getMethod("j").invoke(null);
            type("se6").getMethod("o", type("xn0"), boolean.class).invoke(manager, null, false);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(TelegramXMonetExtension::updateSystemBars);
        } catch (ReflectiveOperationException e) { Log.e(TAG, "Cannot refresh theme", e); }
    }
    private static boolean loadPalette() {
        if (paletteValid) return true;
        try {
            Object manager = managerInstance.invoke(null);
            activeTheme = currentTheme.invoke(manager, true, false);
            Object targetTheme = currentTheme.invoke(manager, false, false);
            activeThemeId = nativeId(targetTheme);
            boolean dark = nativeDark(targetTheme);
            paletteDark = dark;
            Resources resources = application.getResources();
            for (int role = 0; role < palette.length; role++) {
                int tone = (dark ? DARK : LIGHT)[role];
                int resource = resources.getIdentifier("system_" + FAMILIES[role] + "_" + tone, "color", "android");
                available[role] = resource != 0;
                if (resource != 0) {
                    try { palette[role] = resources.getColor(resource, application.getTheme()) & 0x00ffffff; }
                    catch (Resources.NotFoundException e) { available[role] = false; }
                } else if (tone == 950 && FAMILIES[role].equals("neutral1")) {
                    // Android exposes 900 and 1000, but not 950. Interpolate in
                    // linear light to retain the current palette's neutral hue.
                    int lower = resources.getIdentifier("system_neutral1_900", "color", "android");
                    int upper = resources.getIdentifier("system_neutral1_1000", "color", "android");
                    if (lower != 0 && upper != 0) {
                        try {
                            int a = resources.getColor(lower, application.getTheme());
                            int b = resources.getColor(upper, application.getTheme());
                            palette[role] = mixNeutral(a, b);
                            available[role] = true;
                        } catch (Resources.NotFoundException ignored) { }
                    }
                }
            }
            paletteValid = true;
            if (!loggedPalette && available[PRIMARY]) {
                Log.i(TAG, String.format("App Monet full palette v2: dark=%s primary=#%08X page=#%08X surface=#%08X",
                    dark, 0xff000000 | palette[PRIMARY], 0xff000000 | palette[PAGE], 0xff000000 | palette[SURFACE]));
                loggedPalette = true;
            }
            return true;
        } catch (ReflectiveOperationException e) {
            Log.e(TAG, "Cannot resolve current Monet palette", e);
            return false;
        }
    }

    private static int mixNeutral(int a, int b) {
        int color = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            double x = ((a >> shift) & 255) / 255.0;
            double y = ((b >> shift) & 255) / 255.0;
            x = x <= 0.04045 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4);
            y = y <= 0.04045 ? y / 12.92 : Math.pow((y + 0.055) / 1.055, 2.4);
            double linear = (x + y) / 2.0;
            double srgb = linear <= 0.0031308 ? linear * 12.92 : 1.055 * Math.pow(linear, 1.0 / 2.4) - 0.055;
            color |= Math.min(255, Math.max(0, (int) Math.round(srgb * 255))) << shift;
        }
        return color;
    }

    // Replaced with typed native invokes by the patch after caller rewriting.
    public static int nativeColor(Object theme, int id) { return id; }
    public static float nativeProperty(Object theme, int id) { return 0f; }
    public static int nativeId(Object theme) { return 0; }
    public static boolean nativeDark(Object theme) { return false; }

    private static boolean isActive(Object theme) {
        return theme == activeTheme || nativeId(theme) == activeThemeId;
    }
    public static int delegateColor(Object theme, int id) {
        int original = nativeColor(theme, id);
        if (!enabled || Build.VERSION.SDK_INT < 31 || application == null) return original;
        Binding binding = roles.get(id);
        if (binding == null || !loadPalette() || !isActive(theme) || !available[binding.role]) return original;
        return (binding.alpha ? original & 0xff000000 : 0xff000000) | palette[binding.role];
    }
    public static float delegateProperty(Object theme, int id) {
        float original = nativeProperty(theme, id);
        // Property 18 controls dark status-bar icons, which must follow the new surface.
        if (!enabled || id != 18 || application == null || !loadPalette() || !isActive(theme)) return original;
        return paletteDark ? 0f : 1f;
    }
}
