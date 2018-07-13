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
public class TagNdsStdFile {
    private String type;
    private int magic;
    private int nFileSize;
    private short nSize;
    private short nBlock;
    
    public TagNdsStdFile(RandomAccessFile d) throws IOException {
        type = CorrectReading.readString(d, 4);
        magic = CorrectReading.readIntLittleEndian(d);
        nFileSize = CorrectReading.readIntLittleEndian(d);
        nSize = CorrectReading.readShortLittleEndian(d);
        nBlock = CorrectReading.readShortLittleEndian(d);
    }
    
    public void incrementFileSize(int bytes) {
        nFileSize += bytes;
    }
    
    public int getFileLength() { return nFileSize; }
}
