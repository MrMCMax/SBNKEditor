/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
/**
 *
 * @author Max
 */
public class CorrectReading {
    
     /* @param d the stream reader
     * @return the integer value in Little Endian
     * @throws IOException if d cannot read int.
     */
    public static int readIntLittleEndian(RandomAccessFile d) throws IOException {
        byte[] array =  new byte[4];
        d.read(array);
        return  (((array[3] & 0xff) << 24) | ((array[2] & 0xff) << 16) |
                ((array[1] & 0xff) <<  8) | (array[0] & 0xff));
    }
    
    /* @param d the stream reader
     * @return the short value in Little Endian
     * @throws IOException if d cannot read short.
     */
    public static short readShortLittleEndian(RandomAccessFile d) throws IOException {
        byte[] array = new byte[2];
        d.read(array);
        return (short)((array[1] << 8) | (array[0] & 0xff));
    }
    
    public static String readString(RandomAccessFile d, int length) throws IOException {
        byte[] buffer = new byte[length];
        d.read(buffer);
        String res = new String(buffer, StandardCharsets.UTF_8);
        return res;
    }
    
    public static int getIntFromSpinner(javax.swing.JSpinner spinner) {
        short y;
        int x;
        if (!(spinner.getValue() instanceof Integer)) {
            y = (short) spinner.getValue();
            x = (int) y;
        } else {
            x = (int) spinner.getValue();
        }
        return x;
    }
    
    public static short getShortFromSpinner(javax.swing.JSpinner spinner) {
        int y;
        short x;
        if (!(spinner.getValue() instanceof Short)) {
            y = (int) spinner.getValue();
            x = (short) y;
        } else {
            x = (short) spinner.getValue();
        }
        return x;
    }
    
    public static byte getByteFromSpinner(javax.swing.JSpinner spinner) {
        int y;
        byte x;
        if (!(spinner.getValue() instanceof Byte)) {
            y = (int) spinner.getValue();
            x = (byte) y;
        } else {
            x = (byte) spinner.getValue();
        }
        return x;
    }
}
