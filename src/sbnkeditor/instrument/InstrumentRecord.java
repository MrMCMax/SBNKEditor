/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor.instrument;

import java.io.RandomAccessFile;
import sbnkeditor.SBNKInstrument;


/**
 *
 * @author Max
 */
public class InstrumentRecord {
    protected int type;
    protected SBNKInstrument pointer;
    public static final int SINGLE_INSTRUMENT = 0;
    public static final int INSTRUMENT_PER_NOTE = 1;
    public static final int INSTRUMENT_RANGE = 2;
    public static final int EMPTY = 3;
    
    /** Use only with empty records. For other records,
     *  use the sub-classes instead.
     * @param type the record type.
     * @param pointer the pointer to this record.
     */
    public InstrumentRecord(int type, SBNKInstrument pointer) {
        this.type = type;
        this.pointer = pointer;
    }
    /**
     * Only use with empty records!!
     * @param original the original InstrumentRecord
     */
    public InstrumentRecord(InstrumentRecord original) {
        type = original.type;
        pointer = new SBNKInstrument(original.pointer);
    }
    
    public int getType() { return type; }
    
    public SBNKInstrument getPointer() {
        return new SBNKInstrument(pointer);
    }
    
    public static InstrumentRecord copyInstrument(InstrumentRecord original) {
        InstrumentRecord copy = null;
        switch (original.type) {
            case InstrumentRecord.EMPTY:
                copy = new InstrumentRecord(original);
                break;
            case InstrumentRecord.SINGLE_INSTRUMENT:
                copy = new SingleInstrumentRecord((SingleInstrumentRecord) original);
                break;
            case InstrumentRecord.INSTRUMENT_RANGE:
                copy = new InstrumentRangeRecord((InstrumentRangeRecord) original);
                break;
            case InstrumentRecord.INSTRUMENT_PER_NOTE:
                copy = new InstrumentPerNoteRecord((InstrumentPerNoteRecord) original);
                break;
        }
        return copy;
    }
}
