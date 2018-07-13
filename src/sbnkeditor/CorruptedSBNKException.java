/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sbnkeditor;

/**
 *
 * @author Max
 */
public class CorruptedSBNKException extends Exception {
    public static final int NOTSBNK = 0;
    public static final int UNKNOWNERROR = 1;
    private int errorID;
    
    public CorruptedSBNKException() {
        super();
    }
    
    public CorruptedSBNKException(String message) {
        super(message);
    }
    
    public CorruptedSBNKException(int errorID) {
        super();
        this.errorID = errorID;
    }
}
