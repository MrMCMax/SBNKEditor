/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor;

import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import sbnkeditor.instrument.*;

/**
 *
 * @author Max
 */
public class SBNKWindow extends javax.swing.JFrame {

    private SBNKFile file;
    private DefaultListModel recordModel;
    private DefaultListModel subRecordModel;
    private boolean subElementsActive;
    private boolean instrumentRangeActive;
    private int mainIndex;
    private int maxIndex;
    private int subIndex;
    private int maxSubIndex;
    
    /**
     * Creates new form SBNKWindow
     * @param f the sbnkfile
     */
    public SBNKWindow(SBNKFile f) {
        file = f;
        initComponents();
        loadComponents();
    }

    private void loadComponents() {
        recordModel = new DefaultListModel();
        subRecordModel = new DefaultListModel();
        recordList.setModel(recordModel);
        subRecordList.setModel(subRecordModel);
        //Load recordList registers
        InstrumentRecord[] items = file.getRecords();
        for (int i = 0; i < items.length; i++) {
            recordModel.addElement("Record " + i);
        }
        maxIndex = items.length - 1;
        maxSubIndex = -1;
        subElementsActive = false;
        instrumentRangeActive = false;
        disableSubRecords();
        recordAbility(false);
        mainIndex = -1;
        subIndex = -1;
    }
    
    private void loadRecord() {
        int type;
        type = mainIndex >= 0 ? file.getRecord(mainIndex).getType() : -1;
        typeDropdown.setEnabled(mainIndex >= 0);
        subRecordModel.clear();
        maxSubIndex = -1;
        upRecordButton.setEnabled(mainIndex != 0 && mainIndex != -1);
        downRecordButton.setEnabled(mainIndex != file.nRecords() - 1 && mainIndex != -1);
        removeRecordButton.setEnabled(mainIndex != -1);
        switch (type) {
            case InstrumentRecord.SINGLE_INSTRUMENT:
                recordAbility(true);
                disableSubRecords();
                typeDropdown.setSelectedItem("Single Instrument");
                loadInstrument((SingleInstrumentRecord) file.getRecord(mainIndex));
                break;
            case InstrumentRecord.EMPTY:
                recordAbility(false);
                disableSubRecords();
                typeDropdown.setSelectedItem("Empty");
                break;
            case InstrumentRecord.INSTRUMENT_RANGE:
                recordAbility(false);
                instrumentRangeActive = true;
                enableSubRecords();
                loadSubRecordList();
                typeDropdown.setSelectedItem("Multiple Instruments");
                break;
            case InstrumentRecord.INSTRUMENT_PER_NOTE:
                recordAbility(false);
                instrumentRangeActive = false;
                enableSubRecords();
                loadSubRecordList();
                typeDropdown.setSelectedItem("Instrument per note");
                break;
            default:
                recordAbility(false);
                disableSubRecords();
                break;
        }
    }
    
    private void loadSubRecordList() {
        subRecordModel.clear();
        MultipleInstrumentRecord current = (MultipleInstrumentRecord) file.getRecord(mainIndex);
        maxSubIndex = current.nRecords() - 1;
        for (int i = 0; i <= maxSubIndex; i++) {
            subRecordModel.addElement(current.toString(i));
        }
        addSubRecordButton.setEnabled(!current.isFull());
    }
    
    private void recordAbility(boolean b) {
        swavNumberComp.setEnabled(b);
        swarNumberComp.setEnabled(b);
        noteNumberComp.setEnabled(b);
        attackRateComp.setEnabled(b);
        decayRateComp.setEnabled(b);
        sustainLevelComp.setEnabled(b);
        releaseRateComp.setEnabled(b);
        panComp.setEnabled(b);
        if (!b) {
            unknownComp.setEnabled(false);
            swavNumberComp.setValue(0);
            swarNumberComp.setValue(0);
            noteNumberComp.setValue(0);
            attackRateComp.setValue(0);
            decayRateComp.setValue(0);
            sustainLevelComp.setValue(0);
            releaseRateComp.setValue(0);
            panComp.setValue(0);
            unknownComp.setValue(0);
        }
    }
    
    private void disableSubRecords() {
        if (subElementsActive) {
            subElementsActive = false;
            subRecordModel.clear();
            subRecordList.setEnabled(false);
            addSubRecordButton.setEnabled(false);
            removeSubRecordButton.setEnabled(false);
            upSubRecordButton.setEnabled(false);
            downSubRecordButton.setEnabled(false);
            firstNoteLabel.setText("First Note");
            firstNoteComp.setEnabled(false);
            lastNoteComp.setEnabled(false);
            firstNoteComp.setValue(0);
            lastNoteComp.setValue(0);
            unknownComp.setEnabled(false);
            instrumentRangeActive = false;
        }
    }
    
    private void enableSubRecords() {
        if (!subElementsActive) {
            subElementsActive = true;
            subRecordList.setEnabled(true);
            //firstNoteComp.setEnabled(true);
            //lastNoteComp.setEnabled(instrumentRangeActive);
        }
        if (instrumentRangeActive) {
            firstNoteLabel.setText("First Note");
        } else {
            firstNoteLabel.setText("Lower Note");
        }
    }
    
    private void loadSubRecord() {
        if (subIndex == -1) {
            recordAbility(false);
        } else {
            recordAbility(true);
            unknownComp.setEnabled(true);
            upSubRecordButton.setEnabled(subIndex != 0);
            downSubRecordButton.setEnabled(subIndex != maxSubIndex);
            removeSubRecordButton.setEnabled(true);
            int firstNoteVal;
            if (instrumentRangeActive) {
                InstrumentRangeRecord i = (InstrumentRangeRecord) file.getRecord(mainIndex);
                firstNoteVal = subIndex == 0 ? 0 : i.getRangeValue(subIndex - 1) + 1;
                firstNoteComp.setEnabled(subIndex != 0);
                lastNoteComp.setEnabled(subIndex != maxSubIndex);
                lastNoteComp.setValue(i.getRangeValue(subIndex));
                loadInstrument(i.getRecord(subIndex));
            } else {
                InstrumentPerNoteRecord i = (InstrumentPerNoteRecord) file.getRecord(mainIndex);
                firstNoteVal = i.getLowerNote() + subIndex;
                loadInstrument(i.getRecord(subIndex));
                firstNoteComp.setEnabled(true);
            }
            firstNoteComp.setValue(firstNoteVal);
        }
    }
    
    private void loadInstrument(SingleInstrumentRecord i) {
        if (!i.isIndependent()) {
            unknownComp.setValue(i.getUnknown());
        }
        swavNumberComp.setValue(i.getSWAVNumber());
        swarNumberComp.setValue(i.getSWARNumber());
        noteNumberComp.setValue(i.getNoteNumber());
        attackRateComp.setValue(i.getAttack());
        decayRateComp.setValue(i.getDecay());
        sustainLevelComp.setValue(i.getSustain());
        releaseRateComp.setValue(i.getRelease());
        panComp.setValue(i.getPan());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        recordList = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        addRecordButton = new javax.swing.JButton();
        removeRecordButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        subRecordList = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        addSubRecordButton = new javax.swing.JButton();
        removeSubRecordButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        swavNumberComp = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        swarNumberComp = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        noteNumberComp = new javax.swing.JSpinner();
        unknownComp = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        attackRateComp = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        decayRateComp = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        releaseRateComp = new javax.swing.JSpinner();
        sustainLevelComp = new javax.swing.JSpinner();
        panComp = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        typeDropdown = new javax.swing.JComboBox<>();
        firstNoteLabel = new javax.swing.JLabel();
        firstNoteComp = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        lastNoteComp = new javax.swing.JSpinner();
        jButton1 = new javax.swing.JButton();
        upRecordButton = new javax.swing.JButton();
        downRecordButton = new javax.swing.JButton();
        upSubRecordButton = new javax.swing.JButton();
        downSubRecordButton = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        recordList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        recordList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                recordListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(recordList);

        jLabel1.setText("Records");

        addRecordButton.setText("Add ");
        addRecordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRecordButtonActionPerformed(evt);
            }
        });

        removeRecordButton.setText("Remove");
        removeRecordButton.setEnabled(false);
        removeRecordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRecordButtonActionPerformed(evt);
            }
        });

        subRecordList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        subRecordList.setEnabled(false);
        subRecordList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                subRecordListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(subRecordList);

        jLabel2.setText("Sub-records");

        addSubRecordButton.setText("Add");
        addSubRecordButton.setEnabled(false);
        addSubRecordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSubRecordButtonActionPerformed(evt);
            }
        });

        removeSubRecordButton.setText("Remove");
        removeSubRecordButton.setEnabled(false);
        removeSubRecordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSubRecordButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("SWAV number");

        swavNumberComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));
        swavNumberComp.setEnabled(false);
        swavNumberComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                swavNumberCompStateChanged(evt);
            }
        });

        jLabel4.setText("SWAR number");

        swarNumberComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));
        swarNumberComp.setEnabled(false);
        swarNumberComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                swarNumberCompStateChanged(evt);
            }
        });

        jLabel5.setText("Note number");

        noteNumberComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        noteNumberComp.setEnabled(false);
        noteNumberComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                noteNumberCompStateChanged(evt);
            }
        });

        unknownComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));
        unknownComp.setEnabled(false);
        unknownComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                unknownCompStateChanged(evt);
            }
        });

        jLabel7.setText("Attack Rate");

        attackRateComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        attackRateComp.setEnabled(false);
        attackRateComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                attackRateCompStateChanged(evt);
            }
        });

        jLabel8.setText("Decay Rate");

        decayRateComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        decayRateComp.setEnabled(false);
        decayRateComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                decayRateCompStateChanged(evt);
            }
        });

        jLabel9.setText("Sustain Level");

        jLabel10.setText("Release Rate");

        releaseRateComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        releaseRateComp.setEnabled(false);
        releaseRateComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                releaseRateCompStateChanged(evt);
            }
        });

        sustainLevelComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        sustainLevelComp.setEnabled(false);
        sustainLevelComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sustainLevelCompStateChanged(evt);
            }
        });

        panComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        panComp.setEnabled(false);
        panComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panCompStateChanged(evt);
            }
        });

        jLabel11.setText("Type");

        typeDropdown.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Single Instrument", "Multiple Instruments", "Instrument per note", "Empty" }));
        typeDropdown.setEnabled(false);
        typeDropdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeDropdownActionPerformed(evt);
            }
        });

        firstNoteLabel.setText("First Note");

        firstNoteComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        firstNoteComp.setEnabled(false);
        firstNoteComp.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                firstNoteCompStateChanged(evt);
            }
        });

        jLabel14.setText("Last Note");

        lastNoteComp.setModel(new javax.swing.SpinnerNumberModel(0, 0, 127, 1));
        lastNoteComp.setEnabled(false);

        jButton1.setText("Save");

        upRecordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/upIcon.png"))); // NOI18N
        upRecordButton.setEnabled(false);

        downRecordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/downIcon.png"))); // NOI18N
        downRecordButton.setEnabled(false);

        upSubRecordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/upIcon.png"))); // NOI18N
        upSubRecordButton.setEnabled(false);

        downSubRecordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/downIcon.png"))); // NOI18N
        downSubRecordButton.setEnabled(false);

        jLabel13.setText("Unknown");

        jLabel15.setText("Pan");

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(addRecordButton)
                    .addComponent(removeRecordButton)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(upRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addContainerGap(512, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addSubRecordButton)
                            .addComponent(removeSubRecordButton)
                            .addComponent(firstNoteLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lastNoteComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(firstNoteComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(downSubRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(upSubRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(typeDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(swavNumberComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(swarNumberComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(noteNumberComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(releaseRateComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(sustainLevelComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(decayRateComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(attackRateComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))))))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(unknownComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(144, 144, 144)
                                        .addComponent(panComp, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(83, 83, 83)
                                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(97, 97, 97))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                            .addComponent(jScrollPane1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addRecordButton)
                            .addComponent(addSubRecordButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(removeRecordButton)
                            .addComponent(removeSubRecordButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(attackRateComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(typeDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(decayRateComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(swavNumberComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(sustainLevelComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(swarNumberComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(releaseRateComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(noteNumberComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(upSubRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(downSubRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(13, 13, 13)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(unknownComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(firstNoteLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(firstNoteComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(upRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(downRecordButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel14)
                        .addGap(9, 9, 9)
                        .addComponent(lastNoteComp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addRecordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRecordButtonActionPerformed
        String[] options = new String[] { "Single Instrument", "Multiple Instruments",
            "Instrument per note", "Empty" };
        String option = (String)JOptionPane.showInputDialog(this, "Choose the new record type: ",
                "Add new Record", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        System.out.println(option);
        if (option != null) {
            byte fRecord;
            switch (option) {
                case "Single Instrument":
                    fRecord = SBNKInstrument.SINGLE_INSTRUMENT;
                    break;
                case "Multiple Instruments":
                    fRecord = SBNKInstrument.RANGE_OF_INSTRUMENTS;
                    break;
                case "Instrument per note":
                    fRecord = SBNKInstrument.INSTRUMENT_PER_NOTE;
                    break;
                default:
                    fRecord = SBNKInstrument.EMPTY;
                    break;
            }
            file.addNewInstrument(fRecord);
            maxIndex++;
            recordModel.addElement("Record " + maxIndex);
        }
    }//GEN-LAST:event_addRecordButtonActionPerformed

    private void removeRecordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRecordButtonActionPerformed
        if (removeRecordButton.isEnabled()
            && mainIndex != -1) {
            int n = JOptionPane.showConfirmDialog(
                this, "Do you want to remove this record? \n (There will be no turning back)",
                "Confirm removal", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                int index = mainIndex;
                recordModel.clear();
                file.removeInstrument(index);
                int nRecords = file.nRecords();
                for (int i = 0; i < nRecords; i++) {
                    recordModel.addElement("Record " + i);
                }
                mainIndex = -1;
                maxIndex = nRecords - 1;
            }
        }
    }//GEN-LAST:event_removeRecordButtonActionPerformed

    private void typeDropdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeDropdownActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_typeDropdownActionPerformed

    private void recordListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_recordListValueChanged
        if (!evt.getValueIsAdjusting()) {
            ListSelectionModel lsm = recordList.getSelectionModel();
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            int i = minIndex;
            while (i >= 0 && i <= maxIndex && !lsm.isSelectedIndex(i)) { i++; }
            mainIndex = i;
            loadRecord();
        }
    }//GEN-LAST:event_recordListValueChanged

    private void subRecordListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_subRecordListValueChanged
        if (!evt.getValueIsAdjusting()) {
            ListSelectionModel lsm = subRecordList.getSelectionModel();
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            int i = minIndex;
            while (i >= 0 && i <= maxIndex && !lsm.isSelectedIndex(i)) { i++; }
            subIndex = i;
            loadSubRecord();
        }
    }//GEN-LAST:event_subRecordListValueChanged

    private void swarNumberCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_swarNumberCompStateChanged
        if (swarNumberComp.isEnabled()) {
            short x = CorrectReading.getShortFromSpinner(swarNumberComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setSwarNumber(x));
        }
    }//GEN-LAST:event_swarNumberCompStateChanged

    private void swavNumberCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_swavNumberCompStateChanged
        if (swavNumberComp.isEnabled()) {
            short x = CorrectReading.getShortFromSpinner(swavNumberComp);
            file.setInstrumentData(mainIndex,subIndex, i -> i.setSwavNumber(x));
        }
    }//GEN-LAST:event_swavNumberCompStateChanged

    private void noteNumberCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_noteNumberCompStateChanged
        if (noteNumberComp.isEnabled()) {
            byte b = CorrectReading.getByteFromSpinner(noteNumberComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setNoteNumber(b));
        }
    }//GEN-LAST:event_noteNumberCompStateChanged

    private void attackRateCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_attackRateCompStateChanged
        if (attackRateComp.isEnabled()) {
            byte b = CorrectReading.getByteFromSpinner(attackRateComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setAttack(b));
        }
    }//GEN-LAST:event_attackRateCompStateChanged

    private void decayRateCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_decayRateCompStateChanged
        if (decayRateComp.isEnabled()) {
            byte b = CorrectReading.getByteFromSpinner(decayRateComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setDecay(b));
        }
    }//GEN-LAST:event_decayRateCompStateChanged

    private void sustainLevelCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sustainLevelCompStateChanged
        if (sustainLevelComp.isEnabled()) {
            byte b = CorrectReading.getByteFromSpinner(sustainLevelComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setSustain(b));
        }
    }//GEN-LAST:event_sustainLevelCompStateChanged

    private void releaseRateCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_releaseRateCompStateChanged
        if (releaseRateComp.isEnabled()) {
            byte b = CorrectReading.getByteFromSpinner(releaseRateComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setRelease(b));
        }
    }//GEN-LAST:event_releaseRateCompStateChanged

    private void panCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panCompStateChanged
        if (panComp.isEnabled()) {
            byte b = CorrectReading.getByteFromSpinner(panComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setPan(b));
        }
    }//GEN-LAST:event_panCompStateChanged

    private void unknownCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_unknownCompStateChanged
        if (unknownComp.isEnabled()) {
            short s = CorrectReading.getShortFromSpinner(unknownComp);
            file.setInstrumentData(mainIndex, subIndex, i -> i.setUnknown(s));
        }
    }//GEN-LAST:event_unknownCompStateChanged

    private void addSubRecordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSubRecordButtonActionPerformed
        if (addSubRecordButton.isEnabled()) {
            if (instrumentRangeActive) {
                SpinnerDialog dialog = new SpinnerDialog(this);
                dialog.pack();
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
                int n = dialog.getInput();
                if (n != -1) {
                    try {
                        file.addNewSubInstrument(mainIndex, n);
                        loadSubRecordList();
                    } catch (NoteOutOfRangeException e) {
                        JOptionPane.showMessageDialog(this,
                        "The range specified is invalid (probably too big)",
                        "Range error",
                        JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                int n = JOptionPane.showConfirmDialog(this, "Would you like to add a new sub-record?",
                "Add new sub-record", JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    try {
                        file.addNewSubInstrument(mainIndex, 0);
                        maxSubIndex++;
                        subRecordModel.addElement("Note: " + maxSubIndex);
                    } catch (NoteOutOfRangeException e) {
                        JOptionPane.showMessageDialog(this,
                        "You can't add a higher note. Try lowering the first note and adding again",
                        "Range error",
                        JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }//GEN-LAST:event_addSubRecordButtonActionPerformed

    private void removeSubRecordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSubRecordButtonActionPerformed
        if (removeSubRecordButton.isEnabled() && subIndex != -1) {
            int n = JOptionPane.showConfirmDialog(
                this, "Do you want to remove this sub-record? \n (There will be no turning back)",
                "Confirm removal", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                file.removeSubRecord(mainIndex, subIndex);
                loadSubRecordList();
                subIndex = -1;
            }
        }
    }//GEN-LAST:event_removeSubRecordButtonActionPerformed

    private void firstNoteCompStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_firstNoteCompStateChanged
        if (firstNoteComp.isEnabled() && subIndex != -1) {
            try {
                file.setMultiInstrumentData(mainIndex, subIndex, true,
                    CorrectReading.getIntFromSpinner(firstNoteComp));
                for (int i = 0; i <= maxSubIndex; i++) {    //So that the selection doesn't change
                    subRecordModel.set(i,
                        ((MultipleInstrumentRecord) file.getRecord(mainIndex)).toString(i));
                }
            } catch (NoteOutOfRangeException e) {
                JOptionPane.showMessageDialog(this, "That range is not valid",
                "Range error", JOptionPane.ERROR_MESSAGE);
                if (instrumentRangeActive) {
                    firstNoteComp.setValue(
                        ((InstrumentRangeRecord) file.getRecord(mainIndex)).getRange()[subIndex - 1]);
                } else {
                    firstNoteComp.setValue(
                        ((InstrumentPerNoteRecord) file.getRecord(mainIndex)).getLowerNote());
                }
            }
        }
    }//GEN-LAST:event_firstNoteCompStateChanged

    /**
     * @param f the command line arguments
     */
    
    public static void main(SBNKFile f) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SBNKWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SBNKWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SBNKWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SBNKWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // Create and display the form 
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SBNKWindow(f).setVisible(true);
            }
        });
    }  


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRecordButton;
    private javax.swing.JButton addSubRecordButton;
    private javax.swing.JSpinner attackRateComp;
    private javax.swing.JSpinner decayRateComp;
    private javax.swing.JButton downRecordButton;
    private javax.swing.JButton downSubRecordButton;
    private javax.swing.JSpinner firstNoteComp;
    private javax.swing.JLabel firstNoteLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner lastNoteComp;
    private javax.swing.JSpinner noteNumberComp;
    private javax.swing.JSpinner panComp;
    private javax.swing.JList<String> recordList;
    private javax.swing.JSpinner releaseRateComp;
    private javax.swing.JButton removeRecordButton;
    private javax.swing.JButton removeSubRecordButton;
    private javax.swing.JList<String> subRecordList;
    private javax.swing.JSpinner sustainLevelComp;
    private javax.swing.JSpinner swarNumberComp;
    private javax.swing.JSpinner swavNumberComp;
    private javax.swing.JComboBox<String> typeDropdown;
    private javax.swing.JSpinner unknownComp;
    private javax.swing.JButton upRecordButton;
    private javax.swing.JButton upSubRecordButton;
    // End of variables declaration//GEN-END:variables

}
