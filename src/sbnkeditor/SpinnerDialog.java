/*
 * Credits to Oracle Help Center for this. I don't have much knowledge of dialogs,
 * so I had to copy-paste most of the code xD
 *
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
package sbnkeditor;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.beans.*; //property change stuff
import java.awt.*;
import java.awt.event.*;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
/**
 *
 * @author Max
 */
public class SpinnerDialog extends JDialog implements PropertyChangeListener {
    private int typedNumber = 0;
    private JSpinner numberField;

    private JOptionPane optionPane;

    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
    
    public int getInput() {
        return typedNumber;
    }
    
    public SpinnerDialog(Frame aFrame) {
        super(aFrame, true);
        setTitle("Range of the new sub-record");
        SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, 127, 1);
        numberField = new JSpinner(sModel);
        String msgString1 = "Enter the new sub-record range: ";
        Object[] array = {msgString1, numberField};
        Object[] options = {btnString1, btnString2};
        optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);
        setContentPane(optionPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                    optionPane.setValue(new Integer(
                                        JOptionPane.CLOSED_OPTION));
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                numberField.requestFocusInWindow();
            }
        });
        optionPane.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
         && (e.getSource() == optionPane)
         && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(
                    JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                    typedNumber = CorrectReading.getIntFromSpinner(numberField);
                clearAndHide();
            } else { //user closed dialog or clicked cancel
                typedNumber = -1;
                clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        numberField.setValue(1);
        setVisible(false);
    }
}
