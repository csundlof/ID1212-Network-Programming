package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface FileExplorer extends Remote {

    public static final String FILESERVER_NAME_IN_REGISTRY = "files";

    public boolean register(String name, String password) throws RemoteException;

    public boolean unRegister(UserDTO user) throws RemoteException;

    public boolean upload(FileData fd, UserDTO user) throws RemoteException;

    public FileData download(String name, UserDTO user) throws RemoteException;

    public UserDTO login(String name, String password) throws RemoteException;

    public void logout(UserDTO user) throws RemoteException;

    public boolean delete(String name, UserDTO user) throws RemoteException;

    public boolean rename(String name, String newName, UserDTO user) throws RemoteException;

    public boolean resize(String name, int newSize, UserDTO user) throws RemoteException;
    
    public boolean changePermissions(String name, FilePermissions permissions, UserDTO user) throws RemoteException;

    public ArrayList<FileData> listFiles(UserDTO user) throws RemoteException;

    public boolean subscribe(String[] files, UserDTO user, OutputHandler oh) throws RemoteException;
}
