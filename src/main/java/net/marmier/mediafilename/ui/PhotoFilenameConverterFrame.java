package net.marmier.mediafilename.ui;

import net.marmier.mediafilename.MediaFilename;
import net.marmier.mediafilename.timezone.Offset;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Added by raphael on 24.07.17.
 */
public class PhotoFilenameConverterFrame extends javax.swing.JFrame {

    private final JFrame self = this;

    private javax.swing.JLabel filenameLabel;
    private javax.swing.JTextField filenameField;

    private javax.swing.JButton pickerButton;
    private javax.swing.JLabel pickerLabel;
    private JFileChooser filePicker;

    private java.awt.Choice timezoneDropdown;
    private javax.swing.JLabel timezoneLabel;

    private javax.swing.JButton convertButton;
    private javax.swing.JLabel convertLabel;

    private javax.swing.JLabel consoleLabel;
    private javax.swing.JTextArea consoleArea;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JTextArea resultArea;

    private File selectedFile;

    public PhotoFilenameConverterFrame() {
        initComponents();
    }

    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Photo Filename Converter");

        initFilenameField();
        initFilePicker();
        initTimezone();
        initConvertButton();
        initConsoleArea();
        initResultArea();

        pickerButton.addActionListener(evt -> {
            //Handle open button action.
            if (evt.getSource() == pickerButton) {
                int returnVal = filePicker.showOpenDialog(self);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    selectedFile = filePicker.getSelectedFile();
                    filenameField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        convertButton.addActionListener((ActionEvent evt) -> {
            //Handle open button action.
            if (evt.getSource() == convertButton) {
                TextAreaAppender consoleAppender = new TextAreaAppender(consoleArea);
                TextAreaAppender resultAppender = new TextAreaAppender(resultArea);
                try {
                    String filename = filenameField.getText();
                    String selectedTimezone = timezoneDropdown.getSelectedItem();
                    if (checkMissingParams(consoleAppender, filename, selectedTimezone)) {
                        return;
                    }
                    Offset offset = Offset.forCode(selectedTimezone);
                    String directoryMessage = String.format("Chosen file/directory is %s", filename);
                    consoleAppender.println(directoryMessage);
                    String offsetMessage = String.format("Chosen offset is %s", offset.toString());
                    consoleAppender.println(offsetMessage);
                    MediaFilename.realStart(offset, new File(filename), consoleAppender, resultAppender);
                } catch (IOException e) {
                    String message = String.format("An error occured during processing: %s", e.getMessage());
                    consoleAppender.println(message);
                    throw new RuntimeException(e);
                }
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(filenameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(filenameField, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(pickerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pickerButton))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(timezoneLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(timezoneDropdown))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(convertLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(convertButton)))
                    .addContainerGap(27, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(consoleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(consoleArea, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(resultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(resultArea, javax.swing.GroupLayout.PREFERRED_SIZE, 548,
                                javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, pickerButton, filenameField);

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(filenameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(filenameLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(pickerButton)
                        .addComponent(pickerLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(timezoneDropdown)
                        .addComponent(timezoneLabel))
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(convertButton)
                        .addComponent(convertLabel))
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(consoleArea, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(consoleLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(resultArea, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(resultLabel))
                    .addContainerGap(21, Short.MAX_VALUE))
        );
        pack();
    }

    private boolean checkMissingParams(MediaFilename.OutputAppender appender, String filename, String selectedTimezone) {
        boolean hasMissingParams = false;
        if (filename == null || filename.isEmpty()) {
            appender.println("Please choose the file or directory to operate on.");
            hasMissingParams = true;
        }
        if (selectedTimezone == null || selectedTimezone.isEmpty()) {
            appender.println("Please choose the timezone offset to be used.");
            hasMissingParams = true;
        }
        return hasMissingParams;
    }

    private void initTimezone() {
        timezoneLabel = new JLabel();
        timezoneLabel.setText("Select Timezone");
        timezoneDropdown = new java.awt.Choice();
        timezoneDropdown.add("");
        Arrays.stream(Offset.values()).forEach(o -> timezoneDropdown.addItem(o.getCode()));
    }

    private void initConvertButton() {
        convertLabel = new JLabel();
        convertLabel.setText("Generate renames");
        convertButton = new JButton();
        convertButton.setText("Convert");
    }

    private void initResultArea() {
        resultLabel = new JLabel();
        resultLabel.setText("Results");
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setAutoscrolls(true);
    }

    private void initConsoleArea() {
        consoleLabel = new JLabel();
        consoleLabel.setText("Error console");
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setLineWrap(true);
        consoleArea.setAutoscrolls(true);
    }

    private void initFilePicker() {
        filePicker = new JFileChooser();
        filePicker.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        pickerLabel = new JLabel();
        pickerLabel.setText("File / directory picker");
        pickerButton = new JButton();
        pickerButton.setText("Select file or folder");
    }

    private void initFilenameField() {
        filenameLabel = new JLabel();
        filenameLabel.setText("Selected file / directory");
        filenameField = new JTextField();
        filenameField.setSize(1000, filenameField.getHeight());
        filenameField.setEditable(false);
    }

    static class TextAreaAppender implements MediaFilename.OutputAppender {
        private JTextArea ta;

        public TextAreaAppender(JTextArea ta) {
            this.ta = ta;
        }

        @Override
        public void println(String line) {
            ta.append(line);
            ta.append("\n");
        }
    }

}
