/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.integration;

public class FileDBException extends Exception {

    public FileDBException(String reason) {
        super(reason);
    }

    public FileDBException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
