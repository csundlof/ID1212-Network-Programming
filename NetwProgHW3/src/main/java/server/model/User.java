/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import common.FileData;
import common.FilePermissions;
import common.OutputHandler;
import common.UserDTO;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import server.integration.FileDBException;
import server.integration.FileDatabaseDAO;

public class User implements UserDTO {

    private static Map<String, User> fileNotificationSubscriptions = new HashMap<String, User>();
    private final static Object lock = new Object();

    private OutputHandler screenWriter;
    private final String name, password;
    private transient FileDatabaseDAO fileDB;

    public User(String name, String password, FileDatabaseDAO fileDB) {
        this.name = name;
        this.password = password;
        this.fileDB = fileDB;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public synchronized void ping(String message) {
        if (screenWriter == null) {
            return;
        }
        try {
            screenWriter.print(message);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            logout();
        }
    }

    @Override
    public void setOutputHandler(OutputHandler oh) {
        screenWriter = oh;
    }

    public void logout() {
        synchronized (lock) {
            fileNotificationSubscriptions = fileNotificationSubscriptions.entrySet().stream()
                    .filter(entry -> !entry.getValue().getName().equals(name))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        }
    }

    public boolean deleteFile(String fileName) throws FileDBException {
        FileData fd = fileDB.getFile(fileName, this);
        if (fd.canWrite(name)) {
            fileDB.deleteFile(fd);
            if (!fd.isOwner(name)) {
                synchronized (lock) {
                    User usr = fileNotificationSubscriptions.get(fd.getName());
                    if (usr != null) {
                        usr.ping(accessMessage(fileName, "deleted"));
                        fileNotificationSubscriptions.remove(fd.getName());
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public ArrayList<FileData> listFiles() throws FileDBException {
        ArrayList<FileData> files = fileDB.listFiles(this);
        for (FileData fd : files) {
            synchronized (lock) {
                User usr = fileNotificationSubscriptions.get(fd.getName());
                if (usr != null) {
                    usr.ping(accessMessage(fd.getName(), "listed"));
                }
            }
        }
        return fileDB.listFiles(this);
    }

    public boolean upload(FileData fd) throws FileDBException {
        fd.setOwner(name);
        fileDB.saveFile(fd);
        return fileDB.getFile(fd.getName(), this) != null;
    }

    public FileData download(String fileName) throws FileDBException {
        FileData fd = fileDB.getFile(fileName, this);
        if (fd == null) {
            return null;
        }
        synchronized (lock) {
            User usr = fileNotificationSubscriptions.get(fd.getName());
            if (usr != null) {
                usr.ping(accessMessage(fileName, "downloaded"));
            }
        }
        return fileDB.getFile(fileName, this);
    }

    public boolean subscribe(String[] files) throws FileDBException {
        for (String f : files) {
            FileData fd = fileDB.getFile(f, this);
            if (fd.isOwner(name)) {
                synchronized (lock) {
                    fileNotificationSubscriptions.put(fd.getName(), this);
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private String accessMessage(String fileName, String action) {
        return "User " + name + " " + action + " your public file " + fileName;
    }

    public boolean renameFile(String fileName, String newName) throws FileDBException {
        FileData fd = fileDB.getFile(fileName, this);
        if (fd == null) {
            return false;
        }
        if (fd.canWrite(name)) {
            fileDB.rename(fd, newName);
            if (!fd.isOwner(name)) {
                synchronized (lock) {
                    User usr = fileNotificationSubscriptions.get(fd.getName());
                    if (usr != null) {
                        usr.ping(accessMessage(fileName, "renamed"));
                        fileNotificationSubscriptions.put(fd.getName(), usr);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean resizeFile(String fileName, int newSize) throws FileDBException {
        FileData fd = fileDB.getFile(fileName, this);
        if (fd == null) {
            return false;
        }
        if (fd.canWrite(name)) {
            fileDB.resize(fd, newSize);
            if (!fd.isOwner(name)) {
                synchronized (lock) {
                    User usr = fileNotificationSubscriptions.get(fd.getName());
                    if (usr != null) {
                        usr.ping(accessMessage(fileName, "resized"));
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean changePermissions(String fileName, FilePermissions permissions) throws FileDBException {
        FileData fd = fileDB.getFile(fileName, this);
        if (fd == null) {
            return false;
        }
        if (fd.isOwner(name)) {
            fileDB.changePermissions(fd, permissions);
            return true;
        }
        return false;
    }
}
