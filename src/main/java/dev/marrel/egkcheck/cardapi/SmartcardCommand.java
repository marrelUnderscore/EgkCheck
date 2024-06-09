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

import javax.smartcardio.CommandAPDU;

@SuppressWarnings("unused")
public class SmartcardCommand {

    // [S5 reader.py] and [S4 T15]
    public enum SHORT_FILE_ID {
        EF_ATR((byte) 0x1D),        //in MF
        EF_GDO((byte) 0x02),        //in MF
        EF_VERSION((byte) 0x10),    //in MF
        EF_VERSION2((byte) 0x11),   //in MF
        EF_STATUSVD((byte) 0x0C),   //in HCA
        EF_PD((byte) 0x01),         //in HCA
        EF_VD((byte) 0x02);         //in HCA

        public final byte sfid;

        SHORT_FILE_ID(byte sfid){
            this.sfid = sfid;
        }
    }

    /**
     * Switch to the master file container (use as entrypoint when selecting a container)
     * [S5 reader.py], [S2 C5.4.1] and [S2 T14]
     */
    public static final CommandAPDU SELECT_MF = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xA4,
            (byte) 0x04,
            (byte) 0x0C,
            (byte) 0x07,
            (byte) 0xD2,
            (byte) 0x76,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x44,
            (byte) 0x80,
            (byte) 0x00
    });

    /**
     * Switch to the HealthCardApplication container (master file container has to be selected before doing this)
     * [S5 reader.py], [S2 C5.4.1] and [S2 T14]
     */
    public static final CommandAPDU SELECT_HCA = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xA4,
            (byte) 0x04,
            (byte) 0x0C,
            (byte) 0x06,
            (byte) 0xD2,
            (byte) 0x76,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x02
    });

    /*
     * The following are basically just default read commands to select the correct file (offset=0, length=0-> Wildcard, read as many as possible, max 256)
     * Subsequent reads without a file identifier will be made from the same file.
     * [S2 T20] and [S5 reader.py]
     */

    public static final CommandAPDU EF_ATR = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB0, //READ BINARY instruction
            (byte) (Byte.MIN_VALUE | SHORT_FILE_ID.EF_ATR.sfid), //Short file id of file
            (byte) 0x00, //Offset
            (byte) 0x00 //We don't care about the length
    });


    public static final CommandAPDU EF_GDO = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB0, //READ BINARY instruction
            (byte) (Byte.MIN_VALUE | SHORT_FILE_ID.EF_GDO.sfid), //Short file id of file
            (byte) 0x00, //Offset
            (byte) 0x00 //We don't care about the length
    });

    public static final CommandAPDU SELECT_FILE_PD = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB0, //READ BINARY instruction
            (byte) (Byte.MIN_VALUE | SHORT_FILE_ID.EF_PD.sfid), //Short file id of file
            (byte) 0x00, //Offset
            (byte) 0x02 //First two byte are the size of the data -> Here we only read the size
    });

    public static final CommandAPDU SELECT_FILE_Version2 = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB0, //READ BINARY instruction
            (byte) (Byte.MIN_VALUE | SHORT_FILE_ID.EF_VERSION2.sfid), //Short file id of file
            (byte) 0x00, //Offset
            (byte) 0x02 //First two byte are the size of the data -> Here we only read the size
    });

    public static final CommandAPDU SELECT_FILE_StatusVD = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB0, //READ BINARY instruction
            (byte) (Byte.MIN_VALUE | SHORT_FILE_ID.EF_STATUSVD.sfid), //Short file id of file
            (byte) 0x00, //Offset
            (byte) 0x01 //First byte contains the update state flag, which is what we want in this case
    });

    public static final CommandAPDU SELECT_FILE_VD = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB0, //READ BINARY instruction
            (byte) (Byte.MIN_VALUE | SHORT_FILE_ID.EF_VD.sfid), //Short file id of file
            (byte) 0x00, //Offset
            (byte) 0x08 //These bytes are structured like this: 2B offset VD, 2B Length VD, 2B offset GVD, 2B Length GVD - as a result we read the first 8 Bytes in order to get any offset/length info we need
    });


    /*
     * Read a certain record from the Card
     * [S2 T24] and [S5 reader.py]
     */

    public static final CommandAPDU EF_VERSION_1 = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB2, //READ RECORD instruction
            (byte) 0x01, // Record number 1
            (byte) (SHORT_FILE_ID.EF_VERSION.sfid*8 | 0x04), //From file VERSION (see sources for more ifo)
            (byte) 0x00 //Wildcard -> We don't care how many bytes come back
    });

    public static final CommandAPDU EF_VERSION_2 = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB2, //READ RECORD instruction
            (byte) 0x02, // Record number 2
            (byte) (SHORT_FILE_ID.EF_VERSION.sfid*8 | 0x04), //From file VERSION (see sources for more ifo)
            (byte) 0x00 //Wildcard -> We don't care how many bytes come back
    });

    public static final CommandAPDU EF_VERSION_3 = new CommandAPDU(new byte[] {
            (byte) 0x00,
            (byte) 0xB2, //READ RECORD instruction
            (byte) 0x03, // Record number 3
            (byte) (SHORT_FILE_ID.EF_VERSION.sfid*8 | 0x04), //From file VERSION (see sources for more ifo)
            (byte) 0x00 //Wildcard -> We don't care how many bytes come back
    });

    /*
     * Genertal CT commands, see [S2 T12] and [S5 reader.py]
     */

    public static final CommandAPDU RESET_CARD_TERMINAL = new CommandAPDU(new byte[] {
            (byte) 0x20,
            (byte) 0x11,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    });

    public static final CommandAPDU GET_CARD = new CommandAPDU(new byte[] {
            (byte) 0x20,
            (byte) 0x12,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x05
    });

    public static final CommandAPDU EJECT_CARD = new CommandAPDU(new byte[] {
            (byte) 0x20,
            (byte) 0x15,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x01
    });

    // Reads the status of Pin #1 [S3 C15.6.4]
    public static final CommandAPDU GET_PIN_STATUS = new CommandAPDU(new byte[] {
            (byte) 0x80,
            (byte) 0x20,
            (byte) 0x00,
            (byte) 0x01
    });


    /**
     * Generate a read command for the given values from the currently selected file
     * [S2 T19] and [S5 reader.py]
     * @param startPosition The byte offset to start reading from (only 2 bytes are used, value from 0 to 65535)
     * @param length The amount of bytes to read starting with startPosition (only 1 byte is used, value from 0 to 255)
     * @return A CommandAPDU, that contains the desired read command
     */
    public static CommandAPDU createReadCommand(int startPosition, int length) {
        //Split startPosition into two bytes
        byte bpos0 = (byte) (startPosition >> 8 & 0xFF);
        byte bpos1 = (byte) (startPosition & 0xFF);

        return new CommandAPDU(new byte[] {
                (byte) 0x00,
                (byte) 0xB0,
                bpos0,
                bpos1,
                (byte) (length & 0xFF)
        });
    }

}
