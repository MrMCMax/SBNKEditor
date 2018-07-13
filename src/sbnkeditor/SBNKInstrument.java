/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor;

import java.io.RandomAccessFile;
import java.io.IOException;

/**
 *
 * @author Max
 */
public class SBNKInstrument {
    private byte fRecord;
    private short nOffset;
    private byte reserved;
    public static final byte EMPTY = 0x00;
    public static final byte SINGLE_INSTRUMENT = 0x01; //Note that this could be in the range [1..9]
    public static final byte RANGE_OF_INSTRUMENTS = 0x11;
    public static final byte INSTRUMENT_PER_NOTE = 0x10;
        
    public SBNKInstrument(RandomAccessFile d) throws IOException {
        fRecord = d.readByte();
        nOffset = CorrectReading.readShortLittleEndian(d);
        reserved = d.readByte();
    }
    
    public SBNKInstrument(byte fRecord, short nOffset, byte reserved) {
        this.fRecord = fRecord;
        this.nOffset = nOffset;
        this.reserved = reserved;
    }
    
    /**Creates a copy of a SBNKInstrument
     * @param original the original SBNKInstrument
     */
    public SBNKInstrument(SBNKInstrument original) {
        fRecord = original.fRecord;
        nOffset = original.nOffset;
        reserved = original.reserved;
    }
    
    public byte getType() { return fRecord; }
    public int getOffset() { return nOffset; }
    
    /**
     * Increments (decrements) the offset by the specified bytes.
     * @param bytes the number of bytes to increment (decrement)
     */
    public void addOffset(int bytes) {
        nOffset += bytes;
    }
    
    public int compareTo(SBNKInstrument s) {
        return nOffset - s.nOffset;
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof SBNKInstrument &&
                ((SBNKInstrument) o).fRecord == fRecord &&
                ((SBNKInstrument) o).nOffset == nOffset &&
                ((SBNKInstrument) o).reserved == reserved;
    }
//
//    @Override
//    public int hashCode() {
//        int hash = 3;
//        hash = 79 * hash + this.fRecord;
//        hash = 79 * hash + this.nOffset;
//        hash = 79 * hash + this.reserved;
//        return hash;
//    }
}
