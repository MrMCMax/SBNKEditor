/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import sbnkeditor.instrument.*;
import java.util.function.Consumer;
import java.util.function.ToIntBiFunction;
/**
 *
 * @author Max
 */
public class SBNKFile {
    private TagNdsStdFile header;
    private Data dataHeader;
    private InstrumentRecord[] records;
    private int nRecords;
    
    public static byte[] TYPE = new byte[] { 0x53, 0x42, 0x4E, 0x4B };
    
    public SBNKFile(File f) throws IOException, CorruptedSBNKException
    {
        RandomAccessFile reader = null;
        boolean error = false;
        try {
            reader = new RandomAccessFile(f, "r");
            header = new TagNdsStdFile(reader);
            dataHeader = new Data(reader);
            //Now we will read each Instrument
            nRecords = dataHeader.getCount();
            records = new InstrumentRecord[nRecords];
            SBNKInstrument[] pointers = dataHeader.pointers;    //Instrument pointers will be linked with Data pointers
            for (int i = 0; i < nRecords; i++) {
                int recordType = pointers[i].getType();
                switch(recordType) {
                    case 0x0:
                        records[i] = new InstrumentRecord(InstrumentRecord.EMPTY, pointers[i]);
                        break;
                    case 0x10:
                        records[i] = new InstrumentPerNoteRecord(reader, pointers[i]);
                        break;
                    case 0x11:
                        records[i] = new InstrumentRangeRecord(reader, pointers[i]);
                        break;
                    default:
                        records[i] = new SingleInstrumentRecord(reader, pointers[i]);
                        break;
                }
            }
            
        } catch (IOException e) {
            error = true;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        if (error) {
            throw new IOException();
        }
    }
    
    public int nRecords() { return nRecords; }
    
    public InstrumentRecord[] getRecords() {
        InstrumentRecord[] copy = new InstrumentRecord[nRecords];
        for (int i = 0; i < nRecords; i++) {
            copy[i] = InstrumentRecord.copyInstrument(records[i]);
        }
        return copy;
    }
    
    public InstrumentRecord getRecord(int index) {
        return InstrumentRecord.copyInstrument(records[index]);
    }
    
    public void setInstrumentData(int mainIndex, int subIndex, Consumer<SingleInstrumentRecord> operation) {
        if (records[mainIndex] instanceof SingleInstrumentRecord) {
            operation.accept((SingleInstrumentRecord)(records[mainIndex]));
        } else if (records[mainIndex] instanceof MultipleInstrumentRecord) {
            MultipleInstrumentRecord rec = (MultipleInstrumentRecord)records[mainIndex];
            rec.setInstrumentData(subIndex, operation);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public void setMultiInstrumentData(int mainIndex, int subIndex, boolean firstNoteComponent, int value)
        throws NoteOutOfRangeException {
        if (records[mainIndex].getType() == InstrumentRecord.INSTRUMENT_RANGE) {
            InstrumentRangeRecord rec = (InstrumentRangeRecord) records[mainIndex];
            if (firstNoteComponent) {
                rec.setLowerBound(subIndex, value);
            } else {
                rec.setUpperBound(subIndex, value);
            }
        } else if (records[mainIndex].getType() == InstrumentRecord.INSTRUMENT_PER_NOTE){
            ((InstrumentPerNoteRecord) records[mainIndex]).setLowerNote(value);
        }
    }
    
    public void addNewInstrument(byte type) {
        //This is the big deal (check)
        header.incrementFileSize(4);
        SBNKInstrument pointer = new SBNKInstrument(type, (short) header.getFileLength(), (byte) 0x00);
        dataHeader.addPointer(pointer); //Also advances the pointers
        InstrumentRecord[] newArray = new InstrumentRecord[nRecords + 1];
        System.arraycopy(records, 0, newArray, 0, nRecords);
        records = newArray;
        InstrumentRecord newRecord;
        switch (type) {
            case SBNKInstrument.EMPTY:
                newRecord = new InstrumentRecord(InstrumentRecord.EMPTY, pointer);
                break;
            case SBNKInstrument.RANGE_OF_INSTRUMENTS:
                newRecord = new InstrumentRangeRecord(pointer);
                header.incrementFileSize(20);   //8 bytes of range, 2 bytes of unknown, 10 bytes of S.I.R.
                dataHeader.addSize(20);
                break;
            case SBNKInstrument.INSTRUMENT_PER_NOTE:
                newRecord = new InstrumentPerNoteRecord(pointer);
                header.incrementFileSize(14);   //2 bytes of lower note and upper note, 2 bytes of unknown, 10 bytes of S.I.R.
                dataHeader.addSize(14);
                break;
            default: //Single Instrument
                newRecord = new SingleInstrumentRecord(pointer, true);
                header.incrementFileSize(10);
                dataHeader.addSize(10);
                break;
        }
        records[nRecords] = newRecord;
        nRecords++;
    }
    
    public void addNewSubInstrument(int index, int value) throws NoteOutOfRangeException {
        header.incrementFileSize(12);
        MultipleInstrumentRecord rec = (MultipleInstrumentRecord)records[index];
        rec.addSubRecord(value);
        int pos = dataHeader.orderedPositionOf(rec.getPointer());
        if (pos != -1) {
            dataHeader.advancePointersSince(pos + 1, 12);
        }
    }
    
    public void removeInstrument(int index) {
        SBNKInstrument p = records[index].getPointer();
        InstrumentRecord[] newRecords = new InstrumentRecord[nRecords - 1];
        int i, j = 0;
        for (i = 0; i < nRecords; i++) {
            if (!(records[i].getPointer().equals(p))) {
                newRecords[j] = records[i];
                j++;
            }
        }
        dataHeader.removePointer(p);    //Also sets the pointer offsets
        records = newRecords;
        nRecords--;
        header.incrementFileSize(-4);
    }
    /**
     * Precondition: number of sub-records > 1
     * @param mainIndex index of the main record
     * @param subIndex  index of the sub-record to remove
     */
    public void removeSubRecord(int mainIndex, int subIndex) {
        header.incrementFileSize(-12);
        SBNKInstrument p = records[mainIndex].getPointer();
        int pos = dataHeader.orderedPositionOf(p);
        if (pos != -1) {
            dataHeader.advancePointersSince(pos + 1, -12);
            dataHeader.addSize(-12);
        }
        ((MultipleInstrumentRecord)records[mainIndex]).removeSubRecord(subIndex);
        
    }
    
    public void write(File f) {
         
    }
    
    public static <T> void directOrder(T[] array, ToIntBiFunction<T, T> compare) {
        int i, j;
        T x;
        for (i = 1; i < array.length; i++) {
            x = array[i];
            j = i - 1;
            while((j >= 0) && compare.applyAsInt(array[j], x) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = x;
        }
    }
}
