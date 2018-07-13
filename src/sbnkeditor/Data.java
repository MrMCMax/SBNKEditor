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
public class Data {
    private String type;
    private int nSize;
    private int[] reserved;
    private int nCount;
    public SBNKInstrument[] pointers;
    public SBNKInstrument[] orderedPointers;
    
    public Data(RandomAccessFile d) throws IOException {
        type = CorrectReading.readString(d, 4);
        nSize = CorrectReading.readIntLittleEndian(d);
        reserved = new int[8];
        for (int i = 0; i < 8; i++) {
            reserved[i] = CorrectReading.readIntLittleEndian(d);
        } //Should be all zero, but we must check
        nCount = CorrectReading.readIntLittleEndian(d);
        pointers = new SBNKInstrument[nCount];
        for (int i = 0; i < nCount; i++) {
            pointers[i] = new SBNKInstrument(d);
        }
        updateOrderedPointers();
    }
    
    public int getSize() { return nSize; }
    
    public int getCount() { return nCount; }
    
    /**
     * Returns the pointer at the specified position.
     * @param pos the pointer position.
     * @return the corresponding SBNKInstrument pointer.
     */
    public SBNKInstrument getPointer(int pos) {
        return new SBNKInstrument(pointers[pos]);
    }
    
    /**
     * Adds a pointer after the last one.
     * @param p the new pointer to add
     */
    public void addPointer(SBNKInstrument p) {
        SBNKInstrument[] newArray = new SBNKInstrument[nCount + 1];
        System.arraycopy(pointers, 0, newArray, 0, nCount);
        newArray[nCount] = p;
        pointers = newArray;
        nSize += 4;
        nCount++;
        updateOrderedPointers();
        advancePointersSince(0, 4);
    }
    
    public void removePointer(SBNKInstrument p) {
        nSize -= 4;
        int i = 0;
        while (i < nCount && !(orderedPointers[i].equals(p))) {
            i++;
        }
        if (i == nCount) {
            System.out.println("error en la búsqueda");
        } else {
            advancePointersSince(i + 1, -4);
        }
        SBNKInstrument[] newArray = new SBNKInstrument[nCount - 1];
        int j = 0;
        for (SBNKInstrument ins : pointers) {
            if (!(ins.equals(p))) {
                newArray[j] = ins;
                j++;
            }
        }
        nCount--;
        pointers = newArray;
        updateOrderedPointers();
    }
    
    public void addSize(int bytes) {
        nSize += bytes;
    }
    
    /**
     * Adds the number of bytes specified to the pointers from pos to the end.
     * @param pos the first pointer to advance [0 .. nCount - 1]
     * @param bytes the number of bytes to add (usually 32)
     */
    public void advancePointersSince(int pos, int bytes) {
        for (int i = pos; i < nCount; i++) {
            if (orderedPointers[i].getType() != SBNKInstrument.EMPTY) {
                orderedPointers[i].addOffset(bytes);
            }
        }
    }
    
    private void updateOrderedPointers() {
        orderedPointers = new SBNKInstrument[nCount];
        System.arraycopy(pointers, 0, orderedPointers, 0, nCount);
        SBNKFile.directOrder(orderedPointers, (i, j) -> i.compareTo(j));
    }
    
    public int orderedPositionOf(SBNKInstrument p) {
        int i = 0;
        while (i < nCount && !(orderedPointers[i].equals(p))) {
            i++;
        }
        if (i == nCount) {
            System.out.println("Error en la búsqueda");
            return -1;
        } else {
            return i;
        }
    }
}
