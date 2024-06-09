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

@SuppressWarnings("unused")
public class HexTools {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Converts a byte array to a hexdump string
     * @param bytes Byte array to dump
     * @return Byte Dump as a String
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        StringBuilder s = new StringBuilder();
        for(int i=0; i<hexChars.length; i+=2) {
            char[] c = new char[] {hexChars[i], hexChars[i+1]};
            s.append(new String(c));
            s.append(" ");
        }
        return s.toString();
    }
}
