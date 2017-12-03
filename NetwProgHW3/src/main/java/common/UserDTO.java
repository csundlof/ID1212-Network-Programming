/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

public interface UserDTO extends Serializable {

    public String getName();

    public String getPassword();

    public void ping(String message);

    public void setOutputHandler(OutputHandler oh);

}
