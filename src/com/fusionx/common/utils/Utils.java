/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.common.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.fusionx.common.Constants;
import com.fusionx.common.PreferenceKeys;
import com.fusionx.irc.constants.EventBundleKeys;
import com.fusionx.irc.enums.UserLevel;
import com.fusionx.lightirc.R;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import lombok.NonNull;

/**
 * Full of static utility methods
 *
 * @author Lalit Maganti
 */
public class Utils {
    private static Typeface robotoTypeface = null;

    public static int getThemeInt(final Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final int theme = Integer.parseInt(preferences.getString(PreferenceKeys.Theme, "1"));
        return theme != 0 ? R.style.Light : R.style.Dark;
    }

    public static int getThemedTextColor(final Context context) {
        return isThemeLight(context) ? context.getResources().getColor(android.R.color.black) :
                context.getResources().getColor(android.R.color.white);
    }

    public static boolean isThemeLight(final Context context) {
        return getThemeInt(context) == R.style.Light;
    }

    public static boolean isMotdAllowed(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getBoolean(PreferenceKeys.Motd, true);
    }

    public static boolean isMessagesFromChannelShown(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return !preferences.getBoolean(PreferenceKeys.HideMessages, false);
    }

    public static Typeface getRobotoLightTypeface(final Context context) {
        if (robotoTypeface == null) {
            robotoTypeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }
        return robotoTypeface;
    }

    public static void setTypeface(final Context context, final TextView textView) {
        final Typeface font = getRobotoLightTypeface(context);
        textView.setTypeface(font);
    }

    public static String getPartReason(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString(PreferenceKeys.PartReason, "");
    }

    /**
     * Split the line received from the server into it's components
     *
     * @param rawLine        the line received from the server
     * @param careAboutColon - whether a colon means the rest of the line should be added in one go
     * @return the parsed list
     */
    public static ArrayList<String> splitRawLine(final String rawLine,
                                                 final boolean careAboutColon) {
        if (rawLine != null) {
            final ArrayList<String> list = new ArrayList<>();
            String buffer = "";
            final String colonLessLine = rawLine.charAt(0) == ':' ? rawLine.substring(1) : rawLine;

            for (int i = 0; i < colonLessLine.length(); i++) {
                final char c = colonLessLine.charAt(i);
                if (c == ' ') {
                    list.add(buffer);
                    buffer = "";
                } else if (c == ':' && StringUtils.isEmpty(buffer) && careAboutColon) {
                    // A colon can occur in an IPv6 address so that is why the buffer check
                    // is necessary - the final colon can only occur with an empty buffer
                    // Add all the stuff after the last colon as a single item
                    // Essentially the first colon that occurs when the buffer is empty
                    list.add(colonLessLine.substring(i + 1));
                    break;
                } else {
                    buffer += c;
                }
            }

            if (!StringUtils.isEmpty(buffer)) {
                list.add(buffer);
            }
            return list;
        } else {
            return null;
        }
    }

    public static String convertArrayListToString(final ArrayList<String> list) {
        final StringBuilder builder = new StringBuilder();
        for (final String item : list) {
            builder.append(item).append(" ");
        }
        return builder.toString().trim();
    }

    public static void removeFirstElementFromList(final List<String> list, final int noOfTimes) {
        for (int i = 1; i <= noOfTimes; i++) {
            list.remove(0);
        }
    }

    public static String getNickFromRaw(final String rawSource) {
        String nick;
        if (rawSource.contains("!") && rawSource.contains("@")) {
            final int indexOfExclamation = rawSource.indexOf('!');
            nick = StringUtils.left(rawSource, indexOfExclamation);
        } else {
            nick = rawSource;
        }
        return nick;
    }

    public static int generateRandomColor(final int colorOffset) {
        final Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        red = (red + colorOffset) / 2;
        green = (green + colorOffset) / 2;
        blue = (blue + colorOffset) / 2;

        return Color.rgb(red, green, blue);
    }

    public static int getUserColorOffset(final Context context) {
        return isThemeLight(context) ? 0 : 255;
    }

    public static boolean isChannel(String rawName) {
        return Constants.channelPrefixes.contains(rawName.charAt(0));
    }

    public static String getQuitReason(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getString(PreferenceKeys.QuitReason, "");
    }

    public static int getNumberOfReconnectEvents(final Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return preferences.getInt(PreferenceKeys.ReconnectTries, 3);
    }

    public static Set<String> getIgnoreList(final Context context, final String fileName) {
        final SharedPreferences preferences = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        return getStringSet(preferences, PreferenceKeys.IgnoreList, new HashSet<String>());
    }

    public static boolean areNicksEqual(final String firstNick, final String secondNick) {
        return firstNick.equals(secondNick) || (firstNick.equalsIgnoreCase(secondNick) &&
                (firstNick.equalsIgnoreCase("nickserv") || firstNick.equalsIgnoreCase
                        ("chanserv")));
    }

    public static String getAppVersion(final Context context) {
        try {
            final PackageManager manager = context.getPackageManager();
            final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isUserOwnerOrVoice(final UserLevel level) {
        return level.equals(UserLevel.OP) || level.equals(UserLevel.VOICE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void putStringSet(SharedPreferences preferences, final String key,
                                    final Set<String> set) {
        final SharedPreferences.Editor editor = preferences.edit();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet(key, set);
        } else {
            // removes old occurrences of key
            for (String k : preferences.getAll().keySet()) {
                if (k.startsWith(key)) {
                    editor.remove(k);
                }
            }

            int i = 0;
            for (String value : set) {
                editor.putString(key + i++, value);
            }
        }
        editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(final SharedPreferences pref, final String key,
                                           final Set<String> defaultValue) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            return pref.getStringSet(key, defaultValue);
        } else {
            final Set<String> set = new HashSet<>();

            int i = 0;

            Set<String> keySet = pref.getAll().keySet();
            while (keySet.contains(key + i)) {
                set.add(pref.getString(key + i, ""));
                i++;
            }

            if (set.isEmpty()) {
                return defaultValue;
            } else {
                return set;
            }
        }
    }

    /**
     * Static utility methods only - can't instantiate this class
     */
    private Utils() {

    }
}