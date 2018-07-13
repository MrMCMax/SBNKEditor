/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor.instrument;

import sbnkeditor.SBNKInstrument;
import sbnkeditor.CorrectReading;
import java.io.RandomAccessFile;
import java.io.IOException;

/**
 *
 * @author Max
 */
public class SingleInstrumentRecord extends InstrumentRecord {
    private SBNKInstrument pointer;
    private boolean isIndependent;
    private short unknown;
    private short swav_number;
    private short swar_number;
    private byte note_number;
    private byte attack_rate;
    private byte decay_rate;
    private byte sustain_level;
    private byte release_rate;
    private byte pan;
    
    public SingleInstrumentRecord(RandomAccessFile d) throws IOException {
        super(InstrumentRecord.SINGLE_INSTRUMENT, null);
        isIndependent = false;
        unknown = CorrectReading.readShortLittleEndian(d);
        readData(d);
    }
    
    public SingleInstrumentRecord(RandomAccessFile d, SBNKInstrument pointer) throws IOException {
        super(InstrumentRecord.SINGLE_INSTRUMENT, pointer);
        isIndependent = true;
        this.pointer = pointer;
        d.seek(pointer.getOffset());
        readData(d);
    }
    
    public SingleInstrumentRecord(SingleInstrumentRecord original) {
        super(InstrumentRecord.SINGLE_INSTRUMENT, original.pointer);
        isIndependent = original.isIndependent;
        if (isIndependent) {
            pointer = new SBNKInstrument(original.pointer);
        } else {
            pointer = null;
        }
        unknown = original.unknown;
        swav_number = original.swav_number;
        swar_number = original.swar_number;
        note_number = original.note_number;
        attack_rate = original.attack_rate;
        decay_rate = original.decay_rate;
        sustain_level = original.sustain_level;
        release_rate = original.release_rate;
        pan = original.pan;
    }
    
    public SingleInstrumentRecord(SBNKInstrument p, boolean independent) {
        super(InstrumentRecord.SINGLE_INSTRUMENT, p);
        isIndependent = independent;
        pointer = p;
        unknown = swav_number = swar_number = (short) 0x0000;
        note_number = attack_rate = decay_rate = sustain_level = release_rate = pan = (byte) 0x00;
    }
    
    private void readData(RandomAccessFile d) throws IOException {
        swav_number = CorrectReading.readShortLittleEndian(d);
        swar_number = CorrectReading.readShortLittleEndian(d);
        note_number = d.readByte();
        attack_rate = d.readByte();
        decay_rate = d.readByte();
        sustain_level = d.readByte();
        release_rate = d.readByte();
        pan = d.readByte();
    }
    
    //Getters
    public boolean isIndependent() { return isIndependent; }
    public short getUnknown() { return unknown; }
    public short getSWAVNumber() { return swav_number; }
    public short getSWARNumber() { return swar_number; }
    public byte getNoteNumber() { return note_number; }
    public byte getAttack() { return attack_rate; }
    public byte getDecay() { return decay_rate; }
    public byte getSustain() { return sustain_level; }
    public byte getRelease() { return release_rate; }
    public byte getPan() { return pan; }
    
    //Setters
    public void setUnknown(short n) { unknown = n; }
    public void setSwavNumber(short n) { swav_number = n; }
    public void setSwarNumber(short n) { swar_number = n; }
    public void setNoteNumber(byte n) { note_number = n; }
    public void setAttack(byte n) { attack_rate = n; }
    public void setDecay(byte n) { decay_rate = n; }
    public void setSustain(byte n) { sustain_level = n; }
    public void setRelease(byte n) { release_rate = n; }
    public void setPan(byte n) { pan = n; }
}
