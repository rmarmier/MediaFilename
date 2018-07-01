package net.marmier.mediafilename.ui;

import net.marmier.mediafilename.MediaFilename;
import net.marmier.mediafilename.timezone.Offset;

import javax.swing.*;
import java.awt.*;
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
                    MediaFilename.prepareStart(offset, new File(filename), consoleAppender, resultAppender);
                } catch (IOException e) {
                    String message = String.format("An error occured during processing: %s", e.getMessage());
                    consoleAppender.println(message);
                    throw new RuntimeException(e);
                }
            }
        });

        doLayout(this);
        pack();
    }

    private void doLayout(PhotoFilenameConverterFrame frame) {

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new SpringLayout());

        JPanel commandPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        commandPane.add(frame.getPickerButton());
        commandPane.add(frame.getTimezoneDropdown());
        commandPane.add(frame.getConvertButton());
        commandPane.setMinimumSize(new Dimension(600, frame.getTimezoneDropdown().getHeight()));
        contentPane.add(commandPane);

        contentPane.add(frame.getFilenameLabel());
        contentPane.add(frame.getFilenameField());

        contentPane.add(frame.getConsoleLabel());
        contentPane.add(frame.getConsoleArea());

        contentPane.add(frame.getResultLabel());
        contentPane.add(frame.getResultArea());

        SpringUtilities.makeCompactGrid(contentPane, 7, 1, 6, 6, 6, 6);
    }

    private boolean checkMissingParams(MediaFilename.OutputAppender appender, String filename, String selectedTimezone) {
        boolean hasMissingParams = false;
        if (filename == null || filename.isEmpty()) {
            appender.println("Select the file or directory");
            hasMissingParams = true;
        }
        if (selectedTimezone == null || selectedTimezone.isEmpty() || !selectedTimezoneIsValid(selectedTimezone)) {
            appender.println("Please choose the timezone offset to be used");
            hasMissingParams = true;
        }
        return hasMissingParams;
    }

    private boolean selectedTimezoneIsValid(String selectedTimezone) {
        try {
            Offset.forCode(selectedTimezone);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private void initTimezone() {
        timezoneLabel = new JLabel();
        timezoneLabel.setText("Select timezone offset");
        timezoneDropdown = new java.awt.Choice();
        timezoneDropdown.add("Select timezone offset");
        Arrays.stream(Offset.values()).forEach(o -> timezoneDropdown.add(o.getCode()));
    }

    private void initConvertButton() {
        convertLabel = new JLabel();
        convertLabel.setText("Generate renames");
        convertButton = new JButton();
        convertButton.setText("Generate new filenames");
    }

    private void initResultArea() {
        resultLabel = new JLabel();
        resultLabel.setText("Results:");
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setAutoscrolls(true);
        resultArea.setPreferredSize(new Dimension(600, 300));
    }

    private void initConsoleArea() {
        consoleLabel = new JLabel();
        consoleLabel.setText("Errors:");
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setLineWrap(true);
        consoleArea.setAutoscrolls(true);
        consoleArea.setPreferredSize(new Dimension(600, 300));
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
        filenameLabel.setText("Selected file / directory:");
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

        @Override
        public void flush() {
            // no-op
        }
    }

    public JLabel getFilenameLabel() {
        return filenameLabel;
    }

    public JTextField getFilenameField() {
        return filenameField;
    }

    public JButton getPickerButton() {
        return pickerButton;
    }

    public JLabel getPickerLabel() {
        return pickerLabel;
    }

    public JFileChooser getFilePicker() {
        return filePicker;
    }

    public Choice getTimezoneDropdown() {
        return timezoneDropdown;
    }

    public JLabel getTimezoneLabel() {
        return timezoneLabel;
    }

    public JButton getConvertButton() {
        return convertButton;
    }

    public JLabel getConvertLabel() {
        return convertLabel;
    }

    public JLabel getConsoleLabel() {
        return consoleLabel;
    }

    public JTextArea getConsoleArea() {
        return consoleArea;
    }

    public JLabel getResultLabel() {
        return resultLabel;
    }

    public JTextArea getResultArea() {
        return resultArea;
    }

    public File getSelectedFile() {
        return selectedFile;
    }
}
