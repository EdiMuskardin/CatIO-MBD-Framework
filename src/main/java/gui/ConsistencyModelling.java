package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import consistency.mhsAlgs.RcTree;
import consistency.CbModel;
import org.logicng.io.parsers.ParserException;
import util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConsistencyModelling {
    public JPanel panel;
    private JTextArea cnfModelArea;
    private JTextField observationArea;
    private JButton diagnoseObservationButton;
    private JTextArea propLogModelArea;
    private JButton convertToCNFButton;
    private JTextArea diagnosisArea;
    private JButton exportModelButton;
    private JButton checkSatisfiabilityButton;
    private JTextArea picosatOutput;
    private JTabbedPane cnf_dimnc;
    private JTextArea dimacsTextArea;
    private CbModel cbModel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("CBD Modeling");
        frame.setContentPane(new ConsistencyModelling().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    ConsistencyModelling() {
        diagnoseObservationButton.addActionListener(e -> {
            convertToCnf();
            diagnosisArea.setText(null);
            String obsStr = observationArea.getText();
            if (obsStr.isEmpty() || cbModel == null)
                return;
            obsStr = obsStr.replaceAll("\\s+", "");
            Set<String> obs = new HashSet<>(Arrays.asList(obsStr.split(",")));
            RcTree rcTree = new RcTree(cbModel, cbModel.observationToInt(obs));
            for (List<Integer> mhs : rcTree.getDiagnosis()) {
                List<String> diag = cbModel.diagnosisToComponentNames(mhs);
                diagnosisArea.append(String.join(", ", diag) + "\n");
            }
        });

        convertToCNFButton.addActionListener(e -> {
            convertToCnf();
        });

        exportModelButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(Util.getCurrentDir());
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    FileWriter fw = new FileWriter(file);
                    fw.write(propLogModelArea.getText());
                    fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        checkSatisfiabilityButton.addActionListener(e -> {
            convertToCnf();
            picosatOutput.setText(null);
            Runtime rt = Runtime.getRuntime();
            try {
                File tmpFile = new File("cnfModel.tmp");
                FileWriter writer = new FileWriter(tmpFile);
                writer.write(dimacsTextArea.getText());
                writer.close();
                String[] commands = {"lib/picomus", "cnfModel.tmp"};
                Process proc = rt.exec(commands);

                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));

                String s;
                boolean unsat = false;
                String[] modelCnfNames = cnfModelArea.getText().split("\n");

                while ((s = stdInput.readLine()) != null) {
                    String[] line = s.split(" ");
                    if (line[0].equals("c") || line[1] == null)
                        continue;
                    if (line[0].equals("s") && line[1].equals("UNSATISFIABLE")) {
                        unsat = true;
                        picosatOutput.append("UNSATISFIABLE\nConjunction of following clauses is still unsatisfiable\n");
                        continue;
                    } else if (line[0].equals("s") && line[1].equals("SATISFIABLE")) {
                        picosatOutput.append("SATISFIABLE\n");
                        picosatOutput.append("Example assigment of variables\n");
                        unsat = false;
                        continue;
                    }
                    if (unsat) {
                        int lineIndex = Integer.parseInt(line[1]) - 1;
                        if (lineIndex >= 0)
                            picosatOutput.append(modelCnfNames[lineIndex] + "\n");
                    } else {
                        int varIndex;
                        varIndex = Integer.parseInt(line[1]);
                        if (varIndex < 0) {
                            picosatOutput.append(cbModel.getPredicates().getPredicateList().get((varIndex + 1) * -1) + " - FALSE\n");
                        } else if (varIndex > 0)
                            picosatOutput.append(cbModel.getPredicates().getPredicateList().get(varIndex - 1) + " - TRUE\n");
                    }
                }
                tmpFile.deleteOnExit();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void convertToCnf() {
        cnfModelArea.setText(null);
        dimacsTextArea.setText(null);
        String model = propLogModelArea.getText();
        if (model.isEmpty())
            return;
        cbModel = new CbModel();
        try {
            cbModel.modelToCNF(model);
        } catch (ParserException e) {
            Util.errorMsg("Parsing error!", JOptionPane.ERROR_MESSAGE);
            //e.printStackTrace();
        }
        cbModel.setNumOfDistinct(cbModel.getPredicates().getSize());
        for (List<String> line : cbModel.modelToString())
            cnfModelArea.append(String.join(", ", line) + "\n");

        dimacsTextArea.append("c DIMACS CNF representation\n");
        dimacsTextArea.append("p cnf " + cbModel.getPredicates().getSize() + " " + cbModel.getModel().size() + "\n");
        for (List<Integer> cnfLIne : cbModel.getModel())
            dimacsTextArea.append(cnfLIne.toString().substring(1, cnfLIne.toString().length() - 1) + " 0\n");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(5, 2, new Insets(10, 10, 10, 10), -1, -1));
        panel.setMinimumSize(new Dimension(800, 1000));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        propLogModelArea = new JTextArea();
        propLogModelArea.setRows(10);
        scrollPane1.setViewportView(propLogModelArea);
        final JLabel label1 = new JLabel();
        label1.setText("CNF Representation");
        panel.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Propositional Logic Representation");
        panel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(3, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder("Observations"));
        diagnoseObservationButton = new JButton();
        diagnoseObservationButton.setText("Run Diagnosis");
        panel1.add(diagnoseObservationButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder("Diagnosis"));
        diagnosisArea = new JTextArea();
        diagnosisArea.setEditable(false);
        diagnosisArea.setLineWrap(false);
        diagnosisArea.setRows(8);
        diagnosisArea.setText("");
        panel2.add(diagnosisArea, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
        observationArea = new JTextField();
        observationArea.setText("");
        panel1.add(observationArea, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        convertToCNFButton = new JButton();
        convertToCNFButton.setActionCommand("Convert To CNF");
        convertToCNFButton.setText("Convert To CNF");
        panel.add(convertToCNFButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel3, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        exportModelButton = new JButton();
        exportModelButton.setText("Export Model");
        panel3.add(exportModelButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkSatisfiabilityButton = new JButton();
        checkSatisfiabilityButton.setText("Check Satisfiability");
        panel.add(checkSatisfiabilityButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel.add(scrollPane2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
        picosatOutput = new JTextArea();
        picosatOutput.setColumns(25);
        picosatOutput.setEditable(false);
        picosatOutput.setLineWrap(false);
        picosatOutput.setRows(15);
        picosatOutput.setWrapStyleWord(false);
        scrollPane2.setViewportView(picosatOutput);
        cnf_dimnc = new JTabbedPane();
        panel.add(cnf_dimnc, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cnf_dimnc.addTab("CNF", panel4);
        final JScrollPane scrollPane3 = new JScrollPane();
        panel4.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cnfModelArea = new JTextArea();
        cnfModelArea.setColumns(25);
        cnfModelArea.setEditable(false);
        cnfModelArea.setRows(15);
        cnfModelArea.setWrapStyleWord(false);
        scrollPane3.setViewportView(cnfModelArea);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cnf_dimnc.addTab("DIMACS CNF", panel5);
        final JScrollPane scrollPane4 = new JScrollPane();
        panel5.add(scrollPane4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        dimacsTextArea = new JTextArea();
        dimacsTextArea.setEditable(false);
        dimacsTextArea.setRows(10);
        scrollPane4.setViewportView(dimacsTextArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

}
