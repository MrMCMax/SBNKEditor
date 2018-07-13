/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor.instrument;
import java.io.IOException;
import java.io.RandomAccessFile;
import sbnkeditor.SBNKInstrument;
import java.util.function.Consumer;
/**
 *
 * @author Max
 */
public abstract class MultipleInstrumentRecord extends InstrumentRecord {
    protected SingleInstrumentRecord[] records;
    protected int nRecords;
    
    protected MultipleInstrumentRecord(int type, RandomAccessFile d, SBNKInstrument pointer) 
        throws IOException {
        super(type, pointer);
        d.seek((long) pointer.getOffset());
    }
    
    protected MultipleInstrumentRecord(int type, SBNKInstrument p) {
        super(type, p);
        records = new SingleInstrumentRecord[] { new SingleInstrumentRecord(null, false) };
        nRecords = 1;
    }
    
    public MultipleInstrumentRecord(MultipleInstrumentRecord original) {
        super(original.type, original.pointer);
        pointer = new SBNKInstrument(original.pointer);
        nRecords = original.nRecords;
        records = new SingleInstrumentRecord[nRecords];
        for (int i = 0; i < nRecords; i++) {
            records[i] = new SingleInstrumentRecord(original.records[i]);
        }
    }
    
    public abstract String toString(int instrumentIndex);
    public abstract boolean isFull();
    protected abstract void addNewRecord(int input) throws NoteOutOfRangeException;
    protected abstract void removeRecord(int index);
    
    public void addSubRecord(int input) throws NoteOutOfRangeException {
        addNewRecord(input);
        SingleInstrumentRecord[] newRecords = new SingleInstrumentRecord[nRecords + 1];
        System.arraycopy(records, 0, newRecords, 0, nRecords);
        newRecords[nRecords] = new SingleInstrumentRecord(null, false);
        records = newRecords;
        nRecords++;
    }
    
    public void removeSubRecord(int index) {
        removeRecord(index);
        SingleInstrumentRecord[] newRecords = new SingleInstrumentRecord[nRecords - 1];
        int j = 0;
        for (int i = 0; i < nRecords; i++) {
            if (i != index) {
                newRecords[j] = records[i];
                j++;
            }
        }
        nRecords--;
        records = newRecords;
    }
    
    public SingleInstrumentRecord[] getRecords() {
        SingleInstrumentRecord[] recs = new SingleInstrumentRecord[nRecords];
        for (int i = 0; i < records.length; i++) {
            recs[i] = new SingleInstrumentRecord(records[i]);
        }
        return recs;
    }
    /**Returns a copy of the sub-record at the specified index.
     * @param pos the index of the sub-record
     * @return the matching sub-record (SingleInstrumentRecord)
     */
    public SingleInstrumentRecord getRecord(int pos) {
        return new SingleInstrumentRecord(records[pos]);
    }
    
    public int nRecords() { return nRecords; }
    public boolean isEmpty() { return nRecords == 0; }
    
    public void setInstrumentData(int index, Consumer<SingleInstrumentRecord> operation) {
        operation.accept(records[index]);
    }
}
