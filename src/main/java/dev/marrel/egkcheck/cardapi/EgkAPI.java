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


import dev.marrel.egkcheck.model.DataArea;
import dev.marrel.egkcheck.model.Patient;
import dev.marrel.egkcheck.utils.Decompressor;
import dev.marrel.egkcheck.utils.XMLTools;
import org.w3c.dom.Element;

import javax.smartcardio.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

public class EgkAPI {

    public enum CARDVERSION {
        G1("G1"),
        G1plus("G1 plus"),
        G2("G2"),
        G2_1("G2.1"),
        OTHER("Unknown");

        public final String name;
        CARDVERSION(String name) {
            this.name = name;
        }
    }

    public enum XMLFILE {
        PD, VD, GVD //GVD will be Pin protected in the future, but currently isn't
    }

    private final CardChannel channel;

    private EgkAPI(CardChannel channel) {
        this.channel = channel;
    }

    /**
     * Build an EgkAPI object to communicate with an EGK
     * @param terminal The terminal with the desired card
     * @return an EgkAPI Object that allows communication with the card, null if no card is present
     * @throws CardException General Smartcard failure
     */
    public static EgkAPI connectToTerminal(CardTerminal terminal) throws CardException {
        if (terminal.isCardPresent()) {
            // Connect to the card using T=1 protocol
            Card card = terminal.connect("T=1");

            // Get the basic channel
            CardChannel channel = card.getBasicChannel();
            return new EgkAPI(channel);
        } else {
            return null;
        }
    }

    //-------------------------------------------------------

    /**
     * Determine the revision of the card using both ways in which the revision might be stored
     * [S5 reader.py]
     * @return The revision of teh card
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public CARDVERSION getCardVersion() throws EgkException, CardException {
        runCommand(SmartcardCommand.SELECT_MF);
        String ef_version_1 = decodeVersion(SmartcardCommand.EF_VERSION_1);
        String ef_version_2 = decodeVersion(SmartcardCommand.EF_VERSION_2);
        String ef_version_3 = decodeVersion(SmartcardCommand.EF_VERSION_3);

        if (ef_version_1.equals("3.0.0") && ef_version_2.equals("3.0.0") && ef_version_3.equals("3.0.2") ) {
            return CARDVERSION.G1;
        }
        if (ef_version_1.equals("3.0.0") && ef_version_2.equals("3.0.1") && ef_version_3.equals("3.0.3") ) {
            return CARDVERSION.G1plus;
        }
        if (ef_version_1.equals("4.0.0") && ef_version_2.equals("4.0.0") && ef_version_3.equals("4.0.0") ) {
            byte[] version2 = readVersion2();
            //Below 4.3.0 is G2, from 4.3.0 onwards G2.1 according to https://gemspec.gematik.de/docs/gemProdT/gemProdT_MobKT/gemProdT_MobKT_PTV_1.5.0-0_V1.0.0/#4.2
            //Testing suggests this seems to be correct
            if(version2[0] >= 4 && version2[1] >= 3) {
                return CARDVERSION.G2_1;
            } else {
                return CARDVERSION.G2;
            }
        }
        return CARDVERSION.OTHER;
    }

    /**
     * Get the version of the active object system using the newer Version2 file [S6 T4]
     * @return Version bytes of the active object system
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    private byte[] readVersion2() throws EgkException, CardException {
        runCommand(SmartcardCommand.SELECT_MF);
        runCommand(SmartcardCommand.SELECT_FILE_Version2);
        /*
         * Version 2 is structured like this: [S6 T4]
         *  EF     2B         C0       03       02  00  00   C1     03      04  05  02  C2 ...
         *  Tag  TotalLength  Tag   TagLengtgh  x   x   x   Tag   TagLengh  y   y   y   Tag ...
         *  We are interested in y y y -> Bytes 9-11
         */
        byte[] read =  readFile(0, 14); //14 bytes is more than enough for us, no need to read everything
        return Arrays.copyOfRange(read, 9, 12); //Only return bytes 9-11 (second parameter is exclusive)
    }

    private String decodeVersion(CommandAPDU command) throws EgkException, CardException {
        byte[] data = runCommand(command);
        byte[] hba = new byte[12];

        int hba_pntr = 0;
        for(byte b : data) {
            hba[hba_pntr++] = (byte) ((b >> 4) & 0x0F);
            hba[hba_pntr++] = (byte) (b & 0x0F);
        }

        int[] versionNumbers = new int[3];

        versionNumbers[0] = decodeToInt(Arrays.copyOfRange(hba, 0, 3));
        versionNumbers[1] = decodeToInt(Arrays.copyOfRange(hba, 3, 6));
        versionNumbers[2] = decodeToInt(Arrays.copyOfRange(hba, 6, 9));

        return versionNumbers[0] + "." + versionNumbers[1] + "." + versionNumbers[2];
    }

    private int decodeToInt(byte[] bytes) {
        StringBuilder nums = new StringBuilder();
        for(byte b : bytes) {
            b &= 0b00001111;
            if (b >= 10) throw new IllegalStateException("b is " + b);
            nums.append(b);
        }
        return Integer.parseInt(nums.toString());
    }

    //-------------------------------------------------------

    /**
     * Check whether the update flag is set on the card [S4 C4.2.2]
     * @return true if the last update was completed successfully, otherwise false
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public boolean isUpdateUnfinisched() throws EgkException, CardException {
        runCommand(SmartcardCommand.SELECT_MF);
        runCommand(SmartcardCommand.SELECT_HCA);
        runCommand(SmartcardCommand.SELECT_FILE_StatusVD);
        byte[] response = runCommand(SmartcardCommand.createReadCommand(0,1)); // is ASCII
        return !(response[0] == 0x30);
    }


    /**
     * Retrieves the timestamp of the last update from the StatusVD file [S4 C4.2.2]
     * @return A LocalDateTime object with the timestamp
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public LocalDateTime getLastUpdateTimestamp() throws EgkException, CardException {
        runCommand(SmartcardCommand.SELECT_MF);
        runCommand(SmartcardCommand.SELECT_HCA);
        runCommand(SmartcardCommand.SELECT_FILE_StatusVD);
        byte[] response = runCommand(SmartcardCommand.createReadCommand(1,14)); // is ASCII
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyMMddHHmmss");
        return LocalDateTime.parse(new String(response), formatter);
    }

    /**
     * Determine the state of the pin protection (Pin#1) of the card
     * [S3 T106]
     * @return -1 if not required, -2 if Transport, else number of remaining attempts
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public int getRemainingPinAttempts() throws EgkException, CardException {
        ResponseAPDU response = runCommandRaw(SmartcardCommand.GET_PIN_STATUS);
        if(response.getSW() == 0x9000) {
            return -1;
        } else if(response.getSW() >= 0x62c0 && response.getSW() <= 0x62CF) {
            return -2;
        } else if(response.getSW() >= 0x63C0 && response.getSW() <= 0x63CF) {
            return response.getSW() & 0x000F;
        } else {
            throw new EgkException(response.getSW());
        }
    }

    /**
     * Checks whether a xml file is valid against the official XSD
     * @param xmlfile The file id to check
     * @return true, if the file was determined valid, otherwise false
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public boolean checkXmlSchemaValid(XMLFILE xmlfile) throws CardException, EgkException {
        String xml = getXMLFile(xmlfile);
        return XMLTools.validateAgainstXSD(xml, new File(Objects.requireNonNull(getClass().getResource("/Schema_VSD.xsd")).getFile()));
    }

    /**
     * Get a Patient object for the currently inserted EGK
     * @return The PatientData from the EGK stored in a patient object
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public Patient getPatient() throws EgkException, CardException {
        String xml = getXMLFile(XMLFILE.PD);
        Element root = XMLTools.parseStringXML(xml);
        return new Patient(root);
    }

    //-------------------------------------------------------

    /**
     * Determine the start end of the standard XML files stored on an EGK [S4 T7/T8]
     * @param xmlfile The XML file for which the bounds should be determined
     * @return a DataArea object containing start and length of the file
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public DataArea getXMLLength(XMLFILE xmlfile) throws EgkException, CardException {
        runCommand(SmartcardCommand.SELECT_MF);
        runCommand(SmartcardCommand.SELECT_HCA);

        if(xmlfile == XMLFILE.PD) {
            runCommand(SmartcardCommand.SELECT_FILE_PD);
            byte[] response = runCommand(SmartcardCommand.createReadCommand(
                    (byte) 0x00,
                    (byte) 0x02
            ));
            int len = (((int)response[0]) << 8 | response[1]);
            //len -= 2;
            return new DataArea(2,len);
        } else if (xmlfile == XMLFILE.VD) {
            runCommand(SmartcardCommand.SELECT_FILE_VD);
            byte[] response = runCommand(SmartcardCommand.createReadCommand(
                    (byte) 0x00,
                    (byte) 0x08
            ));

            int start = (((int)response[0]) << 8 | response[1]);
            int end = (((int)response[2]) << 8 | response[3]);

            int len = end - (start-1);
            return new DataArea(start,len);
        } else if (xmlfile == XMLFILE.GVD) {
            runCommand(SmartcardCommand.SELECT_FILE_VD);
            byte[] response = runCommand(SmartcardCommand.createReadCommand(
                    (byte) 0x00,
                    (byte) 0x08
            ));

            int start = (((int)response[4]) << 8 | response[5]);
            int end = (((int)response[6]) << 8 | response[7]);

            int len = end - (start-1);
            return new DataArea(start,len);
        } else
            throw new IllegalArgumentException("Unknown XMLFILE-Type");
    }

    /**
     * Decodes the standard XML files stored on an EGK [S4T7/T8]
     * Took a loooot of time to get the gzip algorithm right
     * @param xmlfile The XML file that should be decoded
     * @return The decoded XML file as a string
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    public String getXMLFile(XMLFILE xmlfile) throws EgkException, CardException {
        DataArea dataArea = getXMLLength(xmlfile);
        runCommand(SmartcardCommand.SELECT_MF);
        runCommand(SmartcardCommand.SELECT_HCA);

        if(xmlfile == XMLFILE.PD)
            runCommand(SmartcardCommand.SELECT_FILE_PD);
        else if (xmlfile == XMLFILE.VD || xmlfile == XMLFILE.GVD)
            runCommand(SmartcardCommand.SELECT_FILE_VD);
        else
            throw new IllegalArgumentException("Unknown XMLFILE-Type");


        byte[] readResult = readFile(dataArea.getOffset(), dataArea.getLength());

        byte[] decomp;

        try {
            decomp = Decompressor.decompressByteArrayGZip(readResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        String xml;
        try {
            xml = new String(decomp, "ISO-8859-15");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return xml;
    }

    /**
     * Reads a file from the EGK, while automatically splitting the reads. The correct Container has to be selected first!
     * @param readOffset The offset (in bytes) to start reading from
     * @param length The amount of bytes to read
     * @return the bytes that have been read
     * @throws EgkException EGK-specific failure
     * @throws CardException General Smartcard failure
     */
    private byte[] readFile(int readOffset, int length) throws EgkException, CardException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int max_read = 0xFC; //FIXED VALUE, MAYBE TODO

        while(baos.size() < length) {
            int bytes_not_read = length - baos.size();
            int bytes_to_read = Math.min(bytes_not_read, max_read);
            byte[] readData = runCommand(SmartcardCommand.createReadCommand(readOffset, bytes_to_read));
            readOffset += bytes_to_read;
            try {
                baos.write(readData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return baos.toByteArray();
    }

    //-------------------------------------------------------

    private byte[] runCommand(CommandAPDU command) throws EgkException, CardException {
        ResponseAPDU response = channel.transmit(command);
        if (response.getSW() == 0x9000) {
            return response.getData();
        } else {
            throw new EgkException(response.getSW());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private ResponseAPDU runCommandRaw(CommandAPDU command) throws CardException {
        return channel.transmit(command);
    }

    /**
     * Disconnects from the card
     * @throws CardException General Smartcard failure
     */
    public void close() throws CardException {
        channel.getCard().disconnect(true);
    }

}
