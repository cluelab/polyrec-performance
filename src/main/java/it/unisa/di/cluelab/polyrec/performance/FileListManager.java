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

import java.io.File;
import java.util.ArrayList;

/**
 * @author Vittorio
 *
 */
public class FileListManager {
    private File startDir;
    private String ext;
    private ArrayList<File> filelist = new ArrayList<File>();

    public FileListManager(String sDir, String ext) {
        this.startDir = new File(sDir);
        this.ext = ext;
        obtainFileList(startDir);

    }

    public ArrayList<File> getFileList() {
        return filelist;
    }

    public ArrayList<Gesture> importInfo(int index, boolean fullName) {

        final ArrayList<Gesture> gestures = new ArrayList<Gesture>();
        final File file = filelist.get(index);
        final FileImporter fi = new FileImporter(file);
        gestures.add(fi.importInfo(fullName));
        return gestures;
    }

    private void obtainFileList(File dir) {
        final String entDir = "Entering the directory ";
        final File[] allFiles = dir.listFiles();
        if (allFiles != null) {
            System.out.println(entDir + dir.getName());
            System.out.println("got the following files: " + allFiles.length);
            for (int i = 0; i < allFiles.length; i++) {
                if (allFiles[i].getName().endsWith("." + ext)) {
                    filelist.add(allFiles[i]);
                } else if (allFiles[i].isDirectory()) {
                    System.out.println(entDir + allFiles[i]);
                    obtainFileList(allFiles[i]);
                }
            }
        }
    }

}
