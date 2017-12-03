/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.startup;

import common.FileExplorer;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import server.controller.Controller;
import server.integration.FileDBException;

public class Server {

    private String fileServerName = FileExplorer.FILESERVER_NAME_IN_REGISTRY;
    private String datasource = "FileServer";
    private String dbms = "derby";

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.startRMIServant();
            System.out.println("Fileserver started.");
        } catch (RemoteException | MalformedURLException | FileDBException e) {
            System.out.println("Failed to start fileserver.");
            e.printStackTrace();
        }
    }

    private void startRMIServant() throws RemoteException, MalformedURLException, FileDBException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException noRegistryRunning) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        Controller contr = new Controller(datasource, dbms);
        Naming.rebind(fileServerName, contr);
    }
}
