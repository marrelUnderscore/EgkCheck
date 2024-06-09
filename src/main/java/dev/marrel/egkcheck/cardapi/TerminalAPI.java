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

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.util.List;

public class TerminalAPI {

    /**
     * Looks for any connected card terminal and returns the first one found (prefers Cherry, because I often have multiple others connected :) )
     * @return The CardTerminal that has been found
     * @throws CardException General Smartcard failure
     */
    public static CardTerminal getFirstTerminal() throws CardException {
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();

        if (terminals.isEmpty()) {
            return null;
        } else if (terminals.size() == 1) {
            return terminals.get(0);
        } else {
            for(CardTerminal terminal : terminals) {
                if(terminal.getName().contains("Cherry")) return terminal;
            }
            return terminals.get(0);
        }
    }
}
