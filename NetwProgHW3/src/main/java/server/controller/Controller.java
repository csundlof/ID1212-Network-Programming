/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import common.FileData;
import common.FileExplorer;
import common.FilePermissions;
import common.OutputHandler;
import common.UserDTO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import server.integration.FileDBException;
import server.integration.FileDatabaseDAO;
import server.model.User;

public class Controller extends UnicastRemoteObject implements FileExplorer {

    private final FileDatabaseDAO fileDb;

    public Controller(String datasource, String dbms) throws RemoteException, FileDBException {
        super();
        fileDb = new FileDatabaseDAO(dbms, datasource);
    }

    @Override
    public synchronized boolean register(String name, String password) {
        try {
            User usr = fileDb.findAccount(name);
            if (usr == null) {
                fileDb.registerAccount(name, password);
                return true;
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public synchronized boolean unRegister(UserDTO user) {
        try {
            fileDb.unregisterAccount(user);
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public synchronized boolean upload(FileData fd, UserDTO user) {
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                return usr.upload(fd);
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public synchronized FileData download(String name, UserDTO user) {
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                return usr.download(name);
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized UserDTO login(String name, String password) {
        try {
            User usr = fileDb.findAccount(name);
            if (usr != null && usr.getPassword().equals(password)) {
                return usr;
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public synchronized void logout(UserDTO user) {
        ((User) user).logout();
    }

    @Override
    public synchronized boolean delete(String name, UserDTO user) {
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                return usr.deleteFile(name);
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public synchronized ArrayList<FileData> listFiles(UserDTO user) {
        ArrayList<FileData> files = new ArrayList<FileData>();
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                files = usr.listFiles();
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
        }
        return files;
    }

    @Override
    public synchronized boolean subscribe(String[] files, UserDTO user, OutputHandler oh) throws RemoteException {
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                usr.setOutputHandler(oh);
                return usr.subscribe(files);
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public synchronized boolean resize(String name, int newSize, UserDTO user) throws RemoteException {
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                return usr.resizeFile(name, newSize);
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public synchronized boolean rename(String name, String newName, UserDTO user) throws RemoteException {
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                return usr.renameFile(name, newName);
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public synchronized boolean changePermissions(String name, FilePermissions permissions, UserDTO user) throws RemoteException {
        try {
            User usr = fileDb.findAccount(user.getName());
            if (usr != null && user.getPassword().equals(usr.getPassword())) {
                return usr.changePermissions(name, permissions);
            }
        } catch (FileDBException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

}
