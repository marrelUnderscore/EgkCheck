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

import java.util.HashMap;

public class EgkException extends Exception {
    private final int errortype;
    public static final HashMap<Integer, String> ERROR_TYPE;
    static {
        ERROR_TYPE = new HashMap<>();
        ERROR_TYPE.put(0x0000, "UNKNOWN ERROR");
        ERROR_TYPE.put(0x6283, "Card is deactivated!");
        ERROR_TYPE.put(0x6A82, "The requested data could not be found.");
        ERROR_TYPE.put(0x6900, "Access error: Command not allowed.");
        ERROR_TYPE.put(0x6282, "Read error: Reached end of file.");
        ERROR_TYPE.put(0x6281, "Read error: Data is corrupted.");
        ERROR_TYPE.put(0x6986, "Technical error: No current EF."); //TODO WHATS THAT?
        ERROR_TYPE.put(0x6982, "Access error: Security Status not satisfied."); //NFC PIN stuff
        ERROR_TYPE.put(0x6981, "Technical error: Wrong file type.");
        ERROR_TYPE.put(0x6B00, "Technical error: Offset too big.");
    }

    public EgkException(int errortype) {
        this.errortype = errortype;
        System.err.println("EGKException: " + Integer.toHexString(errortype) + " " + getErrorString());
    }

    public String getErrorString() {
        if(ERROR_TYPE.containsKey(errortype)) {
            return ERROR_TYPE.get(errortype);
        } else {
            return ERROR_TYPE.get(0x0000);
        }
    }

    public int getErrorNum() {
        return errortype;
    }
}
