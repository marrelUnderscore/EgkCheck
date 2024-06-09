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

package dev.marrel.egkcheck.view;

import dev.marrel.egkcheck.cardapi.EgkAPI;
import dev.marrel.egkcheck.cardapi.EgkException;
import dev.marrel.egkcheck.cardapi.SmartcardPresenceDetector;
import dev.marrel.egkcheck.cardapi.SmartcardPresenceListener;
import dev.marrel.egkcheck.model.Patient;
import dev.marrel.egkcheck.utils.LocaleManager;

import javax.imageio.ImageIO;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class EgkCheckWindow extends JFrame implements SmartcardPresenceListener {

    public static final boolean DEBUG = false;

    private final JPanel mainPanel;
    private final JToolBar statusBar;
    private final JLabel labelImageLogo;

    private final JLabel labelCardGeneration, labelUpdateFlag, labelLastUpdate, labelPinState, labelXMLValid, labelPatientNr, labelName, labelGender, labelDateofBirth, labelAddress, labelCity;

    private final JButton buttonInfoUpdateState, buttonInfoPinState, buttonInfoXMLValid;
    private final SmartcardPresenceDetector cardPresenceDetector;
    private final CardTerminal terminal;
    private EgkAPI currentEgkAPI;

    int currentRow = 0;

    public EgkCheckWindow(CardTerminal terminal) {
        this.terminal = terminal;

        mainPanel = new JPanel();
        GridBagLayout mainLayout = new GridBagLayout();
        mainPanel.setLayout(mainLayout);

        setTitle(LocaleManager.getString("title.main") + " V" + EgkCheckWindow.class.getPackage().getImplementationVersion());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.setBorder(new LineBorder(mainPanel.getBackground(), 10));

        statusBar = new JToolBar();
        statusBar.setBorderPainted(true);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusBarLabel = new JLabel(LocaleManager.getString("label.terminal") + ":" + terminal.getName());
        statusBar.add(statusBarLabel);
        add(statusBar, BorderLayout.SOUTH);

        Image logo;
        try {
            logo = ImageIO.read(Objects.requireNonNull(getClass().getResource("/smartcard.png")));
            setIconImage(logo);
            Image schaledLogo = logo.getScaledInstance(51,36, Image.SCALE_SMOOTH);
            labelImageLogo = new JLabel(new ImageIcon(schaledLogo));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        buttonInfoUpdateState = new JButton("ⓘ");
        buttonInfoUpdateState.addActionListener(e -> JOptionPane.showMessageDialog(this,
                LocaleManager.getString("info.updateflag"),
                LocaleManager.getString("title.explain") + ": " + LocaleManager.getString("title.explain.updateflag"),
                JOptionPane.INFORMATION_MESSAGE));
        buttonInfoPinState = new JButton("ⓘ");
        buttonInfoPinState.addActionListener(e -> JOptionPane.showMessageDialog(this,
                LocaleManager.getString("info.pinlocked"),
                LocaleManager.getString("title.explain") + ": " + LocaleManager.getString("title.explain.pinlocked"),
                JOptionPane.INFORMATION_MESSAGE));
        buttonInfoXMLValid = new JButton("ⓘ");
        buttonInfoXMLValid.addActionListener(e -> JOptionPane.showMessageDialog(this,
                LocaleManager.getString("info.xmlerror"),
                LocaleManager.getString("title.explain") + ": " + LocaleManager.getString("title.explain.xmlerror"),
                JOptionPane.INFORMATION_MESSAGE));



        JLabel labelKartenGenerationDesc = new JLabel(LocaleManager.getString("label.cardgeneration") + ": ");
        labelKartenGenerationDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelCardGeneration = new JLabel("INIT");
        addGridRow(labelKartenGenerationDesc, labelCardGeneration, labelImageLogo);

        JLabel labelUpdateStatusDesc = new JLabel(LocaleManager.getString("label.updatestate") + ": ");
        labelUpdateStatusDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelUpdateFlag = new JLabel("INIT");
        addGridRow(labelUpdateStatusDesc, labelUpdateFlag, buttonInfoUpdateState);

        JLabel labelLastUpdateDesc = new JLabel(LocaleManager.getString("label.lastupdate") + ": ");
        labelLastUpdateDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelLastUpdate = new JLabel("INIT");
        addGridRow(labelLastUpdateDesc, labelLastUpdate);

        JLabel labelPinStatusDesc = new JLabel(LocaleManager.getString("label.pinstate") + ": ");
        labelPinStatusDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelPinState = new JLabel("INIT");
        addGridRow(labelPinStatusDesc, labelPinState, buttonInfoPinState);

        JLabel labelXMLValidDesc = new JLabel(LocaleManager.getString("label.xmlcheck") + ": ");
        labelXMLValidDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelXMLValid = new JLabel("[Noch nicht implementiert]");
        addGridRow(labelXMLValidDesc, labelXMLValid, buttonInfoXMLValid);

        JLabel labelVernrDesc = new JLabel(LocaleManager.getString("label.policyholderid") + ": ");
        labelVernrDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelPatientNr = new JLabel("INIT");
        addGridRow(labelVernrDesc, labelPatientNr);

        JLabel labelNameDesc = new JLabel(LocaleManager.getString("label.name") + ": ");
        labelNameDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelName = new JLabel("INIT");
        addGridRow(labelNameDesc, labelName);

        JLabel labelGenderDesc = new JLabel(LocaleManager.getString("label.gender") + ": ");
        labelGenderDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelGender = new JLabel("INIT");
        addGridRow(labelGenderDesc, labelGender);

        JLabel labelDateofBirthDesc = new JLabel(LocaleManager.getString("label.dateofbirth") + ": ");
        labelDateofBirthDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelDateofBirth = new JLabel("INIT");
        addGridRow(labelDateofBirthDesc, labelDateofBirth);

        JLabel labelAdresseDesc = new JLabel(LocaleManager.getString("label.address") + ": ");
        labelAdresseDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelAddress = new JLabel("INIT");
        addGridRow(labelAdresseDesc, labelAddress);

        JLabel labelOrtDesc = new JLabel(LocaleManager.getString("label.place") + ": ");
        labelOrtDesc.setHorizontalAlignment(SwingConstants.RIGHT);
        labelCity = new JLabel("INIT");
        addGridRow(labelOrtDesc, labelCity);

        cardPresenceDetector = new SmartcardPresenceDetector(terminal);
        cardPresenceDetector.start();
        cardPresenceDetector.subscribe(this);

        clear();
    }

    @Override
    public void cardPresenceChanged() {
        if(cardPresenceDetector.getCardPresence()) {
            try {
                //Connection procedure
                labelCardGeneration.setText(LocaleManager.getString("state.readingcard"));
                currentEgkAPI = EgkAPI.connectToTerminal(terminal);

                //Card generation
                labelCardGeneration.setText(currentEgkAPI.getCardVersion().name);

                //Update flag
                labelUpdateFlag.setText(LocaleManager.getString("state.checking"));
                if (currentEgkAPI.isUpdateUnfinisched() || DEBUG) {
                    labelUpdateFlag.setText(LocaleManager.getString("state.updatestate.failed"));
                    labelUpdateFlag.setForeground(Color.red);
                    labelUpdateFlag.setFont(labelUpdateFlag.getFont().deriveFont(Font.BOLD));
                    buttonInfoUpdateState.setVisible(true);
                } else {
                    labelUpdateFlag.setText(LocaleManager.getString("state.ok"));
                }

                labelLastUpdate.setText(currentEgkAPI.getLastUpdateTimestamp().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(LocaleManager.getLocale())));

                //PIN status
                int remainingPinAttempts = currentEgkAPI.getRemainingPinAttempts();
                if(remainingPinAttempts == -1) {
                    labelPinState.setText(LocaleManager.getString("state.pinstate.notrequired"));
                } else if(remainingPinAttempts == -2) {
                    labelPinState.setText(LocaleManager.getString("state.pinstate.transport"));
                } else if(remainingPinAttempts == 0 || DEBUG) {
                    labelPinState.setText(LocaleManager.getString("state.pinstate.locked"));
                    labelPinState.setForeground(Color.red);
                    labelPinState.setFont(labelPinState.getFont().deriveFont(Font.BOLD));
                    buttonInfoPinState.setVisible(true);
                } else {
                    labelPinState.setText(LocaleManager.getString("state.ok") + " (" + remainingPinAttempts + " " + LocaleManager.getString("state.pinstate.remaining") + ")");
                }

                //XML Check
                labelXMLValid.setText(LocaleManager.getString("state.checking"));
                boolean xmlValidPD = currentEgkAPI.checkXmlSchemaValid(EgkAPI.XMLFILE.PD);
                boolean xmlValidVD = currentEgkAPI.checkXmlSchemaValid(EgkAPI.XMLFILE.VD);
                boolean xmlValidGVD = currentEgkAPI.checkXmlSchemaValid(EgkAPI.XMLFILE.GVD);
                if((xmlValidPD && xmlValidVD && xmlValidGVD) && !DEBUG) {
                    labelXMLValid.setText(LocaleManager.getString("state.ok"));

                } else {
                    String str = null;
                    if(!xmlValidPD) str = "PD";
                    if(!xmlValidVD) str = (str==null ? "VD" : " & VD");
                    if(!xmlValidGVD) str = (str==null ? "GVD" : " & GVD");
                    if(DEBUG) str = (str==null ? "DEBUG" : " & DEBUG");

                    labelXMLValid.setText(str + " " + LocaleManager.getString("state.invalid"));
                    labelXMLValid.setForeground(Color.red);
                    labelXMLValid.setFont(labelXMLValid.getFont().deriveFont(Font.BOLD));
                    buttonInfoXMLValid.setVisible(true);
                }

                if(xmlValidPD) {
                    //Read Patient Data
                    labelPatientNr.setText(LocaleManager.getString("state.readingdata"));
                    Patient patient = currentEgkAPI.getPatient();
                    labelPatientNr.setText(patient.getPolicyholderId());
                    labelName.setText(patient.getFirstname() + " " + patient.getLastname());
                    labelGender.setText(patient.getGender());
                    labelDateofBirth.setText(patient.getDateOfBirth().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(LocaleManager.getLocale())));
                    labelAddress.setText(patient.getStreet() + " " + patient.getStreetNumber());
                    labelCity.setText(patient.getPostcode() + " " + patient.getCity());
                }


                //currentEgkAPI.getXMLFile(EgkAPI.XMLFILE.VD);
                //currentEgkAPI.getXMLFile(EgkAPI.XMLFILE.GVD);

                //Close Connection
                currentEgkAPI.close();
            } catch (CardException e) {
                if(e.getCause() != null && e.getCause().getMessage().contains("SCARD_W_UNRESPONSIVE_CARD")) {
                    JOptionPane.showMessageDialog(null, LocaleManager.getString("error.cardconnectfailed"), LocaleManager.getString("title.error"), JOptionPane.ERROR_MESSAGE);
                    clear();
                } else if(e.getCause() != null && (e.getCause().getMessage().contains("SCARD_W_REMOVED_CARD") || e.getCause().getMessage().contains("SCARD_E_NO_SERVICE") || e.getCause().getMessage().contains("SCARD_E_READER_UNAVAILABLE"))) {
                    JOptionPane.showMessageDialog(null, LocaleManager.getString("error.carddisconnect"), LocaleManager.getString("title.error"), JOptionPane.ERROR_MESSAGE);
                    clear();
                } else {
                    String errorMessage = e.getMessage();
                    if(e.getCause() != null) {
                        errorMessage += "\n" + e.getCause().getMessage();
                    }
                    JOptionPane.showMessageDialog(null, LocaleManager.getString("error.cardcritical") + "\n" + errorMessage, LocaleManager.getString("title.error"), JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            } catch (EgkException e) {
                if(e.getErrorNum() == 0x6283) {
                    labelUpdateFlag.setText(LocaleManager.getString("state.updatestate.cardlocked"));
                    labelUpdateFlag.setFont(labelPatientNr.getFont().deriveFont(Font.BOLD));
                    labelUpdateFlag.setForeground(Color.red);
                } else if (e.getErrorNum() == 0x6982 && labelUpdateFlag.getText().equals(LocaleManager.getString("state.checking"))) {
                    labelUpdateFlag.setText(LocaleManager.getString("state.updatestate.notreadable"));
                    labelUpdateFlag.setFont(labelPatientNr.getFont().deriveFont(Font.BOLD));
                    labelUpdateFlag.setForeground(Color.red);
                    JOptionPane.showMessageDialog(null, LocaleManager.getString("error.noreadmaybenfc"), LocaleManager.getString("title.error"), JOptionPane.WARNING_MESSAGE);

                } else {
                    JOptionPane.showMessageDialog(null,  LocaleManager.getString("error.general") + ":\n" + e.getErrorString(), LocaleManager.getString("title.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            clear();
        }
    }

    public void clear() {
        labelCardGeneration.setText(LocaleManager.getString("state.insertcard"));
        labelName.setText("---");
        labelPinState.setText("---");
        labelPinState.setFont(labelPinState.getFont().deriveFont(Font.PLAIN));
        labelPinState.setForeground(Color.black);
        labelPatientNr.setText("---");
        labelPatientNr.setFont(labelPatientNr.getFont().deriveFont(Font.PLAIN));
        labelPatientNr.setForeground(Color.black);
        labelUpdateFlag.setText("---");
        labelUpdateFlag.setFont(labelUpdateFlag.getFont().deriveFont(Font.PLAIN));
        labelUpdateFlag.setForeground(Color.black);
        labelLastUpdate.setText("---");
        labelXMLValid.setText("---");
        labelXMLValid.setFont(labelUpdateFlag.getFont().deriveFont(Font.PLAIN));
        labelXMLValid.setForeground(Color.black);
        labelGender.setText("---");
        labelDateofBirth.setText("---");
        labelAddress.setText("---");
        labelCity.setText("---");


        buttonInfoUpdateState.setVisible(false);
        buttonInfoPinState.setVisible(false);
        buttonInfoXMLValid.setVisible(false);
    }

    private void addGridRow(Component labelDesc, Component labelValue) {
        addGridRow(labelDesc, labelValue, null);
    }
    private void addGridRow(Component labelDesc, Component labelValue, Component buttonInfo) {
        GridBagConstraints c;

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = currentRow;
        c.insets = new Insets(5,5,5,5);
        c.weightx = 0.0;
        c.weighty = 1;
        mainPanel.add(labelDesc, c);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = (buttonInfo == null ? 2 : 1);
        c.gridx = 2;
        c.gridy = currentRow;
        c.insets = new Insets(5,5,5,5);
        c.weightx = (buttonInfo == null ? 1 : 0.9);
        c.weighty = 1;
        mainPanel.add(labelValue, c);

        if(buttonInfo != null) {
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.EAST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 3;
            c.gridy = currentRow;
            if(buttonInfo instanceof JButton) c.insets = new Insets(2,5,2,5);
            //c.weightx = 0.1;
            c.weighty = 1;
            mainPanel.add(buttonInfo, c);
        }
        currentRow++;
    }
}
