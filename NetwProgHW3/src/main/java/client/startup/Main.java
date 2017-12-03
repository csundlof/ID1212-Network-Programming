/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.startup;

import client.view.Console;
import common.FileExplorer;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Main {

    public static void main(String[] args) {
        try {
            FileExplorer fileExplorer = (FileExplorer) Naming.lookup(FileExplorer.FILESERVER_NAME_IN_REGISTRY);
            new Console().start(fileExplorer);
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            System.out.println("Could not start client.");
        }
    }

}
