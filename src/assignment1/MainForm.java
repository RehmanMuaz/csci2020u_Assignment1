/**
 * Title: CSCI2020U Assignment 1
 * Date: March 6th, 2021
 * Author: Muaz Rehman 100553376
 */

// Package
package assignment1;

// Imports
import javafx.application.Platform;
import javafx.fxml.FXML;

import javax.swing.*;
import java.io.*;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

import java.text.DecimalFormat;

// Main application
public class MainForm {

    private static final String WORD_REGEX = "^[a-zA-Z']*$";

    @FXML
    public TableView<TestFile> tblData;

    @FXML
    public TableColumn<TestFile, String> tblColFileName;
    @FXML
    public TableColumn<TestFile, String> tblColActualClass;
    @FXML
    public TableColumn<TestFile, Double> tblColSpamProbability;
    @FXML
    public TableColumn<TestFile, String> tblColValidResult;

    @FXML
    public TextField txtAccuracy;
    @FXML
    public TextField txtPrecision;


    // Initializes main form and loads data
    @FXML
    public void initialize() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory == null) {
            JOptionPane.showMessageDialog(null, "Invalid folder", "Error",
                    JOptionPane.ERROR_MESSAGE);
            Platform.exit();
            System.exit(-1);
        }
        String mainDirectory = selectedDirectory.toString();

        File trainPath = new File(mainDirectory + "/train");
        if (!trainPath.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Invalid folder", "Error",
                    JOptionPane.ERROR_MESSAGE);
            Platform.exit();
            System.exit(-1);
        }

        Map<String, Float> wordMap = new TreeMap<>();

        // Read training files and calculate spam probability
        try {
            List<File> trainHamFiles = new ArrayList<>();
            List<File> trainSpamFiles = new ArrayList<>();

            findFiles(trainPath, trainHamFiles, trainSpamFiles);

            Map<String, Integer> wordHamFreq = new TreeMap<>();
            Map<String, Integer> wordSpamFreq = new TreeMap<>();

            countWordFrequency(trainHamFiles, wordHamFreq);
            countWordFrequency(trainSpamFiles, wordSpamFreq);

            List<String> words = mergeMapsByKey(wordHamFreq, wordSpamFreq);
            for (String word : words) {
                float probabilityWordSpam = 0;
                float probabilityWordHam = 0;

                if (wordSpamFreq.containsKey(word)) {
                    probabilityWordSpam = wordSpamFreq.get(word) / (float)trainSpamFiles.size();
                }
                if (wordHamFreq.containsKey(word)) {
                    probabilityWordHam = wordHamFreq.get(word) / (float) trainHamFiles.size();
                }

                wordMap.put(word, probabilityWordSpam / (probabilityWordHam + probabilityWordSpam));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Training data could not be read",
                    "Error", JOptionPane.ERROR_MESSAGE);
            Platform.exit();
            System.exit(-1);
        }

        // Read test files and calculate spam probability
        File testPath = new File(mainDirectory + "/test");
        if (!testPath.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Invalid data folder", "Error",
                    JOptionPane.ERROR_MESSAGE);
            Platform.exit();
            System.exit(-1);
        }

        List<TestFile> results = new ArrayList<>();

        try {
            List<File> hamFiles = new ArrayList<>();
            List<File> spamFiles = new ArrayList<>();

            findFiles(testPath, hamFiles, spamFiles);

            calculateSpamProbability(results, hamFiles, wordMap);
            calculateSpamProbability(results, spamFiles, wordMap);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to read test data",
                    "Error", JOptionPane.ERROR_MESSAGE);
            Platform.exit();
            System.exit(-1);
        }

        // Accuracy and precision
        int numTruePositives = 0;
        int numFalsePositives = 0;
        int numTrueNegatives = 0;
        for (TestFile file : results) {
            if (file.getActualClass().equals("Ham")) {
                if (file.getSpamProbability() < 0.5f) {
                    numTruePositives++;
                } else {
                    numFalsePositives++;
                }
            } else if (file.getSpamProbability() > 0.5f) {
                numTrueNegatives++;
            }
        }

        double accuracy = (numTrueNegatives + numTruePositives) / (double)results.size();
        double precision = numTruePositives / (double)(numTruePositives + numFalsePositives);

        // Format to decimal
        DecimalFormat dec = new DecimalFormat("0.000%");

        txtAccuracy.setText(dec.format(accuracy));
        txtPrecision.setText(dec.format(precision));

        // Show data in TableView
        ObservableList<TestFile> data
                = FXCollections.observableArrayList();
        data.addAll(results);

        tblData.setItems(data);

        tblColFileName.setCellValueFactory(
                new PropertyValueFactory<>("fileName"));
        tblColActualClass.setCellValueFactory(
                new PropertyValueFactory<>("actualClass"));
        tblColSpamProbability.setCellValueFactory(
                new PropertyValueFactory<>("spamProbRounded"));
        tblColValidResult.setCellValueFactory(
                new PropertyValueFactory<>("validResult")
        );

    }

    // Find ham and spam files and arrange in list
    private void findFiles(File root, List<File> ham, List<File> spam) {
        for (File path : root.listFiles()) {
            if (path.isDirectory()) {
                String pathName = path.getPath();
                if (pathName.contains("ham")) {
                    ham.addAll(Arrays.asList(path.listFiles()));
                } else if (pathName.contains("spam")) {
                    spam.addAll(Arrays.asList(path.listFiles()));
                }
            }
        }
    }

    // Counts frequency of word occurance
    private void countWordFrequency(List<File> files, Map<String, Integer> frequencyMap) throws IOException {
        for (File file : files) {
            // Read file word by word
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (word.matches(WORD_REGEX)) {
                    // Increase the frequency of the word whenever we find it in the file
                    if (frequencyMap.containsKey(word)) {
                        frequencyMap.put(word, frequencyMap.get(word) + 1);
                    } else {
                        frequencyMap.put(word, 1);
                    }
                }
            }
            fileReader.close();
        }
    }

    // Calculates spam probability using ham and spam lists
    private void calculateSpamProbability(List<TestFile> result, List<File> files, Map<String, Float> wordMap)
            throws IOException {
        for (File file : files) {
            String path = file.getPath();

            // Determine if the file is ham or spam
            String actualClass = "Ham";
            Boolean spamClass = false;
            if (path.contains("spam")) {
                actualClass = "Spam";
                spamClass = true;
            }

            TestFile testFile = new TestFile(path, 0, actualClass, "false");
            float sum = 0;

            // Read file word by word
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader);
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (word.matches(WORD_REGEX)) {
                    if (wordMap.containsKey(word)) {
                        // Increase the sum depending on how probable the word is to be spam
                        float wordSpamProbability = wordMap.get(word);
                        if (wordSpamProbability > 0.0f && wordSpamProbability < 1.0f) {
                            sum += Math.log(1 - wordSpamProbability)
                                    - Math.log(wordSpamProbability);
                        }
                    }
                }
            }
            fileReader.close();

            // Calculate spam probability for file
            double spamProbability = 1 / (1 + Math.pow(Math.E, sum));
            testFile.setSpamProbability(spamProbability);

            if(spamClass == true && spamProbability < 0.5)
            {
                testFile.setValidResult("false");
            }
            else if(spamClass == false && spamProbability > 0.5)
            {
                testFile.setValidResult("false");
            }
            else
            {
                testFile.setValidResult("true");
            }
            result.add(testFile);
        }
    }

    // Create key value pairs
    @SafeVarargs
    private final <TKey, TValue> List<TKey> mergeMapsByKey(Map<TKey, TValue>... maps) {
        List<TKey> list = new ArrayList<>();
        for (Map<TKey, TValue> map : maps) {
            for (Map.Entry<TKey, TValue> pair : map.entrySet()) {
                TKey key = pair.getKey();
                if (!list.contains(key)) {
                    list.add(key);
                }
            }
        }
        return list;
    }
}
