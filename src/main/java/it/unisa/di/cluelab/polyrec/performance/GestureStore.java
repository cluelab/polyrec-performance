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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * @author Vittorio
 *
 */
public class GestureStore {
    public static final int TYPE_TRAINING = 0;
    public static final int TYPE_TEST = 1;
    public static final int TYPE_ANY = 2;

    private HashMap<TemplateKey, ArrayList<Gesture>> store;
    private ArrayList<String> templateNames;
    private ArrayList<Integer> subjects;
    private ArrayList<String> speeds;

    private HashMap<String, ArrayList<Gesture>> selected;
    private HashMap<String, ArrayList<Gesture>> trainingSample;
    private HashMap<String, ArrayList<Gesture>> testSample;

    private HashMap<Gesture, Integer> gestureTypes;

    public GestureStore() {
        store = new HashMap<TemplateKey, ArrayList<Gesture>>();
        gestureTypes = new HashMap<Gesture, Integer>();
        templateNames = new ArrayList<String>();
        subjects = new ArrayList<Integer>();
        speeds = new ArrayList<String>();
    }

    public void addAtIndex(ArrayList<Gesture> list, int index, Gesture element) {
        if (index < list.size()) {
            list.set(index, element);
        } else {
            final int insertNulls = index - list.size();
            for (int i = 0; i < insertNulls; i++) {
                list.add(null);
            }
            list.add(element);
        }

    }

    public void addGesture(Gesture gesture, int gestureType) {
        final TemplateKey tKey = new TemplateKey(gesture.getInfo().getSubject(), gesture.getInfo().getSpeed(),
                gesture.getInfo().getName());
        ArrayList<Gesture> gestures = null;
        if (!store.containsKey(tKey)) {
            gestures = new ArrayList<Gesture>();
            store.put(tKey, gestures);
        } else {
            gestures = store.get(tKey);
        }
        // System.out.println("trying to add "+gesture.getName()+" in pos "+(gesture.getNumber()-1));
        addAtIndex(gestures, gesture.getInfo().getNumber() - 1, gesture);
        if (!templateNames.contains(gesture.getInfo().getName())) {
            templateNames.add(gesture.getInfo().getName());
        }
        if (!subjects.contains(gesture.getInfo().getSubject())) {
            subjects.add(gesture.getInfo().getSubject());
        }
        if (!speeds.contains(gesture.getInfo().getSpeed())) {
            speeds.add(gesture.getInfo().getSpeed());
        }

        gestureTypes.put(gesture, gestureType);
    }

    // selects templates by subject and speed
    public void selectBySpeed(String speed) {
        selected = new HashMap<String, ArrayList<Gesture>>();

        final Iterator<String> iter = templateNames.iterator();
        // for each class
        while (iter.hasNext()) {
            final String name = iter.next();
            // for each subject
            for (int subject : subjects) {
                final TemplateKey tKey = new TemplateKey(subject, speed, name);
                final ArrayList<Gesture> templates = store.get(tKey);
                selected.put(name, templates);
            }
        }
    }

    // selects templates by subject and speed
    public void selectBySubjectAndSpeed(int subject, String speed) {
        selected = new HashMap<String, ArrayList<Gesture>>();

        final Iterator<String> iter = templateNames.iterator();
        // for each class
        while (iter.hasNext()) {
            final String name = iter.next();
            final TemplateKey tKey = new TemplateKey(subject, speed, name);
            final ArrayList<Gesture> templates = store.get(tKey);
            selected.put(name, templates);
        }
    }

    // divides in training sets and test sets
    public void randomize(int numTrainingSamples) {
        trainingSample = new HashMap<String, ArrayList<Gesture>>();
        testSample = new HashMap<String, ArrayList<Gesture>>();

        // randomize in each class
        for (String classe : templateNames) {
            final ArrayList<Gesture> gesturesOfAClass = selected.get(classe);
            Collections.shuffle(gesturesOfAClass);

            final ArrayList<Gesture> trainingClass = new ArrayList<Gesture>();
            final ArrayList<Gesture> testClass = new ArrayList<Gesture>();
            for (Gesture g : gesturesOfAClass) {

                final int type = gestureTypes.get(g);
                // System.out.println(g.getName()+" "+g.getNumber()+" "+type);

                if (type == TYPE_TRAINING && trainingClass.size() < numTrainingSamples) {
                    trainingClass.add(g);
                } else if (type == TYPE_TEST) {
                    testClass.add(g);
                } else if (type == TYPE_ANY && trainingClass.size() < numTrainingSamples) {
                    trainingClass.add(g);
                } else if (type != TYPE_TRAINING) {
                    testClass.add(g);
                }
            }
            trainingSample.put(classe, trainingClass);
            testSample.put(classe, testClass);
            // System.out.println(classe+" "+trainingClass.size());
        }
    }

    public ArrayList<Gesture> getRandomTrainingSamples(String classe) {
        return trainingSample.get(classe);
    }

    public Gesture getRandomTestSample(String classe) {
        final ArrayList<Gesture> testClass = testSample.get(classe);
        final int index = new Random().nextInt(testClass.size());
        return testClass.get(index);
    }

    public HashMap<String, ArrayList<Gesture>> getAllGestures(int subject, String speed, int gestureType) {
        final HashMap<String, ArrayList<Gesture>> gestures = new HashMap<String, ArrayList<Gesture>>();

        final Iterator<String> iter = templateNames.iterator();
        while (iter.hasNext()) {
            final String name = iter.next();
            final TemplateKey tKey = new TemplateKey(subject, speed, name);
            final ArrayList<Gesture> templates = store.get(tKey);

            // filter gestures based on types
            final ArrayList<Gesture> typedGestures = new ArrayList<Gesture>();
            for (Gesture g : templates) {
                if (gestureTypes.get(g) == TYPE_ANY || gestureTypes.get(g) == gestureType) {
                    typedGestures.add(g);
                }
            }
            gestures.put(name, typedGestures);
        }
        return gestures;
    }

    public HashMap<TemplateKey, ArrayList<Gesture>> getStore() {
        return store;
    }

    public ArrayList<String> getTemplateNames() {
        return templateNames;
    }

    public ArrayList<Integer> getSubjects() {
        return subjects;
    }

    public ArrayList<String> getSpeeds() {
        return speeds;
    }

}
