/*
 *     EGKCheck - Demo Program to read and test EGKs
 *     Copyright (C) 2024  Marcel K
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.marrel.egkcheck.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleManager {

    protected static ResourceBundle rb;

    private static final String[] validLocales = new String[]{"en-GB", "de-DE", "en-US"};

    public static void switchLocale(Locale locale) {
        if(Arrays.asList(validLocales).contains(locale.toLanguageTag())) {
            rb = ResourceBundle.getBundle("lang/lang", locale);
        } else {
            rb = ResourceBundle.getBundle("lang/lang", Locale.US);
        }
    }

    public static String getString(String key) {
        return rb.getString(key);
    }

    public static Locale getLocale() {
        return rb.getLocale();
    }
}
