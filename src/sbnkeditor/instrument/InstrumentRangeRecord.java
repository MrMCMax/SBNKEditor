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
public class InstrumentRangeRecord extends MultipleInstrumentRecord {
    private byte[] range;
    
    public InstrumentRangeRecord(RandomAccessFile d, SBNKInstrument pointer) throws IOException {
        super(InstrumentRecord.INSTRUMENT_RANGE, d, pointer);
        range = new byte[8];
        d.read(range);
        nRecords = 1;
        for (int i = 0; i < 8 && range[i] != 0x7F; i++) {
            nRecords++;
        }
        records = new SingleInstrumentRecord[nRecords];
        for (int i = 0; i < nRecords; i++) {
            records[i] = new SingleInstrumentRecord(d);
        }
    }
    
    public InstrumentRangeRecord(SBNKInstrument pointer) {
        super(InstrumentRecord.INSTRUMENT_RANGE, pointer);
        range = new byte[] { 127, 0, 0, 0, 0, 0, 0, 0 };
    }
    
    public InstrumentRangeRecord(InstrumentRangeRecord original) {
        super(original);
        range = new byte[8];
        System.arraycopy(original.range, 0, range, 0, 8);
    }
     
    public byte[] getRange() {
        byte[] copy = new byte[8];
        System.arraycopy(range, 0, copy, 0, 8);
        return copy;
    }
    
    public byte getRangeValue(int pos) {
        return range[pos];
    }
    
    @Override
    public boolean isFull() { return range[7] == 127; }
    
    @Override
    public String toString(int instrumentIndex) {
        int up = range[instrumentIndex];
        int low;
        if (instrumentIndex == 0) {
            low = 0;
        } else {
            low = range[instrumentIndex - 1] + 1;
        }
        return "Range: from " + low + " to " + up;
    }

    @Override
    public void addNewRecord(int amplitude) throws NoteOutOfRangeException {
        if (range[7] == 127 || amplitude < 0 || amplitude > 127 - nRecords) {
            throw new NoteOutOfRangeException();
        } else {
            range[nRecords] = 127;
            if (nRecords == 1) {
                range[0] = (byte) (127 - amplitude);
            } else if (nRecords > 1) {
                //Home-made algorithm to adjust the range values
                int intervalsToStretch = 0;
                int currentInterval;
                do {
                    intervalsToStretch++;
                    if (nRecords - intervalsToStretch == 0) {
                        currentInterval = range[0];
                    } else {
                        currentInterval = 127 - range[nRecords - intervalsToStretch - 1];
                    }
                } while (amplitude >= currentInterval);
                range[nRecords - 1] = (byte) (127 - amplitude);
                for (int i = 0; i < intervalsToStretch - 1; i++) {
                    range[nRecords - 2 - i] = (byte)(range[nRecords - 1 - i] - 1);
                }
            }
        }
    }

    /**
     * Precondition: number of sub-records > 1
     * @param index the index of the range to remove
     */
    @Override
    protected void removeRecord(int index) {
        //range[nRecords - 1] = 0;
        if (index == 0) {
            System.arraycopy(range, 1, range, 0, nRecords - 1);
        } else {
            int j = 0;
            for (int i = index; i < nRecords; i++) {
                range[i - 1] = range[i];
            }
        }
        range[nRecords - 1] = 0;
    }
    
    /**
     * Precondition: 0 < index < 8 
     * @param index the interval to modify
     * @param lowerNote the lower bound of the interval
     * @throws NoteOutOfRangeException if that range cannot be applied
     */
    public void setLowerBound(int index, int lowerNote) 
        throws NoteOutOfRangeException {
        if (lowerNote < 0) {
            throw new NoteOutOfRangeException();
        } else {
            if (index > 1 && range[index - 2] >= lowerNote) {
                setLowerBound(index - 1, lowerNote - 1);
            }
            range[index - 1] = (byte) lowerNote;
        }
    }
    
    /**
     * Precondition: 0 <= index < 7
     * @param index the interval to modify
     * @param highNote the upper bound of the interval
     * @throws NoteOutOfRangeException if that range cannot be applied
     */
    public void setUpperBound(int index, int highNote) 
        throws NoteOutOfRangeException {
        if (highNote > 126) {
            throw new NoteOutOfRangeException();
        } else {
            if (range[index + 1] <= highNote) {
                setUpperBound(index + 1, highNote + 1);
            }
            range[index] = (byte) highNote;
        }
    }
}
