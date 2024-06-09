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

package dev.marrel.egkcheck;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dev.marrel.egkcheck.cardapi.TerminalAPI;
import dev.marrel.egkcheck.utils.LocaleManager;
import dev.marrel.egkcheck.view.EgkCheckWindow;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.swing.*;
import java.util.Locale;

public class main {
    @SuppressWarnings("MethodNameSameAsClassName")
    public static void main(String[] args) {
        FlatLaf.registerCustomDefaultsSource("theme");
        FlatLightLaf.setup();

        LocaleManager.switchLocale(Locale.getDefault());

        CardTerminal terminal;
        try {
            terminal = TerminalAPI.getFirstTerminal();
        } catch (CardException e) {
            terminal = null;
        }
        if(terminal == null) {
            JOptionPane.showMessageDialog(null, LocaleManager.getString("error.noreader"), LocaleManager.getString("title.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        EgkCheckWindow cardWindow = new EgkCheckWindow(terminal);
        cardWindow.setVisible(true);
    }
}
