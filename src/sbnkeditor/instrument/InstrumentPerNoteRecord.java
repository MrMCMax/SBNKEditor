/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor.instrument;

import sbnkeditor.SBNKInstrument;
import java.io.RandomAccessFile;
import java.io.IOException;

/**
 *
 * @author Max
 */
public class InstrumentPerNoteRecord extends MultipleInstrumentRecord {
    private byte lower_note;
    private byte upper_note = 0;
    
    public InstrumentPerNoteRecord(RandomAccessFile d, SBNKInstrument pointer) throws IOException {
        super(InstrumentRecord.INSTRUMENT_PER_NOTE, d, pointer);
        lower_note = d.readByte();
        upper_note = d.readByte();
        nRecords = upper_note - lower_note + 1;
        records = new SingleInstrumentRecord[nRecords];
        for (int i = 0; i < nRecords; i++) {
            records[i] = new SingleInstrumentRecord(d);
        }
    }
    
    public InstrumentPerNoteRecord(SBNKInstrument pointer) {
        super(InstrumentRecord.INSTRUMENT_PER_NOTE, pointer);
        lower_note = 0;
        upper_note = 0;
    }
    
    public InstrumentPerNoteRecord(InstrumentPerNoteRecord original) {
        super(original);
        lower_note = original.lower_note;
        upper_note = original.upper_note;
    }
    
    public byte getLowerNote() { return lower_note; }
    public byte getUpperNote() { return upper_note; }
    
    @Override
    public boolean isFull() {
        return nRecords >= 127;
    }
    
    @Override
    public String toString(int instrumentIndex) {
        return "Note: " + (lower_note + instrumentIndex);
    }
    /**
     * Adds a note at the upper bound of the interval.
     * @param note We do not use the param in this method.
     * @throws NoteOutOfRangeException if the upper bound is already the maximum (127).
     */
    @Override
    protected void addNewRecord(int note) throws NoteOutOfRangeException {
        if (upper_note == 127) {
            throw new NoteOutOfRangeException();
        } else {
            upper_note++;
        }
    }
    /**
     * Precondition: nº of sub-records > 1
     * @param index unused
     */
    @Override
    protected void removeRecord(int index) {
        upper_note--;
    }
    
    public void setLowerNote(int note) throws NoteOutOfRangeException {
        int interval = upper_note - lower_note;
        if ((note + interval) > 127) {
            throw new NoteOutOfRangeException();
        } else {
            lower_note = (byte) note;
            upper_note = (byte) (lower_note + interval);
        }
    }
}
