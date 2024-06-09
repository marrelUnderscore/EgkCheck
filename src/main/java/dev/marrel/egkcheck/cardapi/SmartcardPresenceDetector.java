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

package dev.marrel.egkcheck.cardapi;

import dev.marrel.egkcheck.utils.LocaleManager;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.swing.*;
import java.util.ArrayList;

public class SmartcardPresenceDetector implements Runnable {
    private Thread t;
    private boolean canceled = false;

    private final CardTerminal terminal;
    private final ArrayList<SmartcardPresenceListener> listeners;
    private boolean cardIsPresent;

    public SmartcardPresenceDetector(CardTerminal terminal) {
        this.terminal = terminal;
        listeners = new ArrayList<>();
    }

    public void subscribe(SmartcardPresenceListener listener) {
        listeners.add(listener);
    }

    public boolean getCardPresence() {
        return cardIsPresent;
    }

    @SuppressWarnings("unused")
    public void cancel() {
        canceled = true;
        t.interrupt();
    }

    private void alertAll() {
        for (SmartcardPresenceListener listener : listeners) {
            listener.cardPresenceChanged();
        }
    }

    @Override
    public void run() {
        while(!canceled) {
            try {
                if(cardIsPresent != terminal.isCardPresent()) {
                    cardIsPresent = terminal.isCardPresent();
                    alertAll();
                }
                //noinspection BusyWait
                Thread.sleep(750);
            } catch (InterruptedException ignored) {

            } catch (CardException e) {
                JOptionPane.showMessageDialog(null, LocaleManager.getString("error.terminaldisconnected"), LocaleManager.getString("title.error"), JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}
