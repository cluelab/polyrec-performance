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
import it.unisa.di.cluelab.polyrec.PolyRecognizerGSS;
import it.unisa.di.cluelab.polyrec.Recognizer;
import it.unisa.di.cluelab.polyrec.Result;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * @author Vittorio
 *
 */
// CHECKSTYLE:OFF
public class Tester {
    // CHECKSTYLE:ON
    private GestureStore store;
    private int totTests;
    private long time;
    private File outfile;
    private boolean rInvariant;

    /**
     * Constructor of the recognizer.
     * 
     * @param file
     * 
     * @param parent
     *            Reference of the processing sketch (this).
     */
    public Tester(boolean method2, String dir, boolean fullName, int trainingN, File file) {

        this.rInvariant = method2;
        outfile = file;
        store = new GestureStore();

        // load gestures from logs
        final FileListManager flm = new FileListManager(dir, "xml");
        final ArrayList<File> files = flm.getFileList();
        System.out.println("Adding " + files.size() + " gestures to store");
        for (int i = 0; i < files.size(); i++) {
            final FileImporter fi = new FileImporter(files.get(i));
            final Gesture gesture = fi.importInfo(fullName);
            int type = GestureStore.TYPE_ANY;
            if (trainingN >= 0) {
                if (gesture.getInfo().getNumber() <= trainingN) {
                    type = GestureStore.TYPE_TRAINING;
                } else {
                    type = GestureStore.TYPE_TEST;
                }
            }
            store.addGesture(gesture, type);

        }
        System.out.println("Gestures added to store");
    }

    // CHECKSTYLE:OFF
    public ConfusionMatrix test(int user, String speed, int numTemplates, int numTests) {
        // CHECKSTYLE:ON
        final ArrayList<String> templateNames = store.getTemplateNames();
        final ConfusionMatrix matrix = new ConfusionMatrix(templateNames);

        final DescriptiveStatistics[] stats = new DescriptiveStatistics[templateNames.size()];
        for (int k = 0; k < templateNames.size(); k++) {
            stats[k] = new DescriptiveStatistics();
        }
        String methodDes = null;

        if (user > 0) {
            store.selectBySubjectAndSpeed(user, speed);
        } else {
            store.selectBySpeed(speed);
        }

        // perform i-th test
        for (int i = 0; i < numTests; i++) {
            final Recognizer recognizer = new PolyRecognizerGSS(rInvariant);
            methodDes = recognizer.getMethod();

            // init templates
            store.randomize(numTemplates);
            for (int j = 0; j < templateNames.size(); j++) {
                final List<Gesture> templates = store.getRandomTrainingSamples(templateNames.get(j));
                recognizer.addTemplates(templateNames.get(j), templates);
            }

            // recognizing random gestures
            // for each class
            for (int k = 0; k < templateNames.size(); k++) {
                final String classe = templateNames.get(k);
                final Gesture randomGesture = store.getRandomTestSample(classe);
                final long start = System.currentTimeMillis();
                final Result rec = recognizer.recognize(randomGesture);
                time += System.currentTimeMillis() - start;
                totTests++;

                if (rec != null) {
                    // System.out.println(templName+"\t"+rec.getName());
                    matrix.increase(randomGesture.getInfo().getName(), rec.getName());
                } else {
                    stats[k].addValue(0);
                    System.out.println(randomGesture.getInfo().getName() + "\tUnrecognized");
                }

                if (rec != null && !randomGesture.getInfo().getName().equals(rec.getName())) {
                    stats[k].addValue(0);
                    System.out.println("ERROR: " + randomGesture.getInfo().getName() + "-"
                            + randomGesture.getInfo().getNumber() + '\t' + rec.getName());
                } else {
                    stats[k].addValue(1);
                }
            }
        }
        for (int k = 0; k < templateNames.size(); k++) {
            final ResultRecord rr = new ResultRecord("" + user, methodDes, speed, numTemplates, templateNames.get(k),
                    stats[k].getMean());
            try {
                FileUtils.writeStringToFile(outfile, rr.toString() + "\r\n", (String) null, true);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return matrix;
    }

    public ConfusionMatrix testAll(int numTests, int numTemplates) {
        final int numUsers = store.getSubjects().size();
        final ArrayList<String> templateNames = store.getTemplateNames();
        final ConfusionMatrix[] matrix = new ConfusionMatrix[numUsers];

        for (int user = 0; user < numUsers; user++) {
            System.out.println("Testing user " + store.getSubjects().get(user));
            matrix[user] = new ConfusionMatrix(templateNames);
            for (String speed : store.getSpeeds()) {
                System.out.println("speed " + speed);
                matrix[user].sum(test(store.getSubjects().get(user), speed, numTemplates, numTests));
            }
        }
        return ConfusionMatrix.average(matrix);
    }

    public ConfusionMatrix testUserIndependent(int numTests, int numTemplates) {
        final ArrayList<String> templateNames = store.getTemplateNames();
        final ConfusionMatrix matrix = new ConfusionMatrix(templateNames);

        for (String speed : store.getSpeeds()) {
            System.out.println("speed: " + speed);
            matrix.sum(test(-1, speed, numTemplates, numTests));
        }

        return matrix;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.print("Missing arguments.\nUsage: datasetDirectory ");
            System.out.println("numTests=10 rotationInvariant=false fullName=false maxTempl=9 trainingN=-1");
            // datasets/xml_logs ($1): fullName=false maxTempl=9 trainingN=-1
            // datasets/ilgdb: fullName=true maxTempl=3 trainingN=3
            // datasets/kb: fullName=true maxTempl=3 trainingN=-1
            return;
        }
        final String dir = args[0];
        final int numTests = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        // true: rotation invariant; false: rotation sensitive
        final boolean method = args.length > 2 ? Boolean.parseBoolean(args[2]) : false;
        final boolean fullName = args.length > 3 ? Boolean.parseBoolean(args[3]) : false;
        final int maxTempl = args.length > 4 ? Integer.parseInt(args[4]) : 9;
        final int trainingN = args.length > 5 ? Integer.parseInt(args[5]) : -1;
        final long start = System.currentTimeMillis();
        final Tester tester = new Tester(method, dir, fullName, trainingN,
                new File(new SimpleDateFormat("'results-'yyyy-MM-dd_hh-mm-ss'.txt'").format(new Date())));
        final StringBuilder stats = new StringBuilder("numTempl\terrorRate\n");
        for (int numTempl = 1; numTempl <= maxTempl; numTempl++) {
            final ConfusionMatrix cm = tester.testAll(numTests, numTempl);
            // ConfusionMatrix cm = tester.testUserIndependent(1000,numTempl);
            ConfusionMatrix.print(cm);
            System.out.println("rate = " + cm.getErrorRate());
            stats.append(numTempl + "\t" + cm.getErrorRate() + "\n");
        }
        System.out.println("exec time: " + (System.currentTimeMillis() - start));
        System.out.println("Tot tests " + tester.totTests);
        System.out.println("Time " + tester.time);
        System.out.println("Avg Time " + ((double) tester.time) / ((double) tester.totTests));
        System.out.print(stats);
    }

}
