/*
PolyRec Project
Copyright (c) 2015-2017, Vittorio Fuccella - CLUE Lab - http://cluelab.di.unisa.it
All rights reserved. Includes a reference implementation of the following:

* Vittorio Fuccella, Gennaro Costagliola. "Unistroke Gesture Recognition
  Through Polyline Approximation and Alignment". In Proceedings of the 33rd
  annual ACM conference on Human factors in computing systems (CHI '15).
  April 18-23, 2015, Seoul, Republic of Korea.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the PolyRec Project nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package it.unisa.di.cluelab.polyrec.performance;

import it.unisa.di.cluelab.polyrec.Gesture;
import it.unisa.di.cluelab.polyrec.GestureInfo;
import it.unisa.di.cluelab.polyrec.TPoint;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Vittorio
 *
 */
public class FileImporter {
    private File fXmlFile;
    private int subject;
    private String speed;
    private String name;
    private int number;

    public FileImporter(File fXmlFile) {
        this.fXmlFile = fXmlFile;
    }

    public Gesture importInfo(boolean fullName) {
        final Gesture tstroke = new Gesture();

        try {

            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(fXmlFile);

            final Element root = doc.getDocumentElement();
            final String wholeName = root.getAttribute("Name");
            if (fullName) {
                name = wholeName.substring(0, wholeName.length());
            } else {
                name = wholeName.replaceAll("[0-9]*$", "");
            }

            subject = Integer.parseInt(root.getAttribute("Subject"));
            final String wholeSpeed = root.getAttribute("Speed");
            // if (wholeSpeed.equals("fast"))
            // speed = 1;
            // else if (wholeSpeed.equals("medium"))
            // speed = 2;
            // else if (wholeSpeed.equals("slow"))
            // speed = 3;
            // else
            // speed = Integer.parseInt(wholeSpeed);
            speed = wholeSpeed;
            number = Integer.parseInt(root.getAttribute("Number"));
            final GestureInfo info = new GestureInfo(subject, speed, name, number);
            tstroke.setInfo(info);
            // tstroke.setNumPts(Integer.parseInt(root.getAttribute("NumPts")));
            // tstroke.setMillseconds(Integer.parseInt(root.getAttribute("Millseconds")));

            final NodeList nList = doc.getElementsByTagName("Point");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                final Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    final Element elPoint = (Element) nNode;
                    final double x = Double.parseDouble(elPoint.getAttribute("X"));
                    final double y = Double.parseDouble(elPoint.getAttribute("Y"));
                    final long t = Long.parseLong(elPoint.getAttribute("T"));
                    final TPoint point = new TPoint(x, y, t);
                    tstroke.addPoint(point);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return tstroke;
    }

    public int getSubject() {
        return subject;
    }

    public String getSpeed() {
        return speed;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

}
