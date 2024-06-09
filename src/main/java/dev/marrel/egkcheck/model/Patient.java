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

package dev.marrel.egkcheck.model;

import dev.marrel.egkcheck.utils.XMLTools;
import org.w3c.dom.Element;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Patient {

    private final String policyholderId, firstname, lastname, gender, city, postcode, street, streetNumber, countryCode;
    private final LocalDate dateOfBirth;

    public Patient(Element xmlRoot) {
        policyholderId = XMLTools.getFirstElementByTagName(xmlRoot, "Versicherten_ID").getTextContent();
        firstname = XMLTools.getFirstElementByTagName(xmlRoot, "Vorname").getTextContent();
        lastname = XMLTools.getFirstElementByTagName(xmlRoot, "Nachname").getTextContent();
        gender = XMLTools.getFirstElementByTagName(xmlRoot, "Geschlecht").getTextContent();
        city = XMLTools.getFirstElementByTagName(xmlRoot, "Ort").getTextContent();
        postcode = XMLTools.getFirstElementByTagName(xmlRoot, "Postleitzahl").getTextContent();
        street = XMLTools.getFirstElementByTagName(xmlRoot, "Strasse").getTextContent();
        streetNumber = XMLTools.getFirstElementByTagName(xmlRoot, "Hausnummer").getTextContent();
        countryCode = XMLTools.getFirstElementByTagName(xmlRoot, "Wohnsitzlaendercode").getTextContent();

        String dobString = XMLTools.getFirstElementByTagName(xmlRoot, "Geburtsdatum").getTextContent();
        dateOfBirth = LocalDate.parse(dobString, DateTimeFormatter.BASIC_ISO_DATE);

    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getCity() {
        return city;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getStreet() {
        return street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPolicyholderId() {
        return policyholderId;
    }

    public String getGender() {
        return gender;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAll() {
        return policyholderId + firstname + lastname + city + postcode + street + streetNumber + dateOfBirth.toString();
    }
}
