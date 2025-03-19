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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class XMLTools {
    private XMLTools() {
    }

    @SuppressWarnings("unused")
    public static ArrayList<Node> getDirectChildren(Node n) {
        ArrayList<Node> nodes = new ArrayList<>();

        Node child = n.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                nodes.add(child);
            }
            child = child.getNextSibling();
        }

        return nodes;
    }

    public static Node getFirstElementByTagName(Element n, String tagName) {
        NodeList nl = n.getElementsByTagName(tagName);
        if(nl.getLength() > 0) {
            return nl.item(0);
        } else {
            return null;
        }
    }


    public static Element parseStringXML(String data) {
        InputStream is = new ByteArrayInputStream(data.getBytes(Charset.forName("ISO-8859-15")));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        //Build Document
        Document document;
        try {
            document = builder.parse(is);
        } catch (SAXException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Normalize the XML Structure; important !!
        document.getDocumentElement().normalize();

        //Here comes the root node
        return document.getDocumentElement();
    }

    public static boolean validateAgainstXSD(String xml, InputStream xsdFile) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(xsdFile);
        Schema schema;
        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException e) {
            System.out.println("Schema file invalid!");
            throw new RuntimeException(e);
        }
        Validator validator = schema.newValidator();
        try {
            validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()))));
            return true;
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}