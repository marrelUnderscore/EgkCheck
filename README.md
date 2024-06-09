# EGKCheck
EGKCheck is small tool to check whether a german public health insurance card (EGK) is working using an ordinary smartcard reader.
It is also a demo implementation on how to read an EGK using Java.
Many thanks to the creators of https://github.com/Blueshoe/python-healthcard and https://github.com/Kaupisch-IT/eGK-KVK, which were great references for getting the basic implementation right.

## Sources
The code contains references to a couple of references to the technical specification of the EGK. They look like this:
 - [S1 C2.2] -> Source 1, Chapter 2.2
 - [S3 P42]  -> Source 3, Page 42
 - [S2 T13]  -> Source 2, Table 13
 - [S5 ready.py] -> Source 5, File reader.py

They reference the following documents (not all of them are referenced in the code):
 - [S1] https://gemspec.gematik.de/downloads/gemSpec/gemSpec_eGK_ObjSys_G2_1/gemSpec_eGK_ObjSys_G2_1_V4.7.1.pdf
 - [S2] https://www.dkgev.de/fileadmin/default/Mediapool/2_Themen/2.1_Digitalisierung_Daten/2.1.5._Telematik-Infrastruktur/2.1.5.2_Einfuehrung_und_Betrieb/2013_11_06_364_eGK_Veroeffentlichung_Dokuaenderungen.pdf
 - [S3] https://fachportal.gematik.de/fileadmin/user_upload/fachportal/files/Spezifikationen/Basis-Rollout/Elektronische_Gesundheitskarte/gematik_eGK_Spezifikation_Teil1_V2_2_0.pdf
 - [S4] https://www.dkgev.de/fileadmin/default/Mediapool/2_Themen/2.1_Digitalisierung_Daten/2.1.5._Telematik-Infrastruktur/2.1.5.2_Einfuehrung_und_Betrieb/2013_11_06_364_eGK_Veroeffentlichung_Dokuaenderungen.pdf
 - [S5] https://github.com/Blueshoe/python-healthcard
 - [S6] https://gemspec.gematik.de/downloads/gemSpec/gemSpec_Karten_Fach_TIP_G2_1/gemSpec_Karten_Fach_TIP_G2_1_V3.0.0.pdf

## XSD Schema
The XSD Schema was taken from https://github.com/gematik/api-telematik/tree/OPB5/fa/vsds and licensed under the apache license.