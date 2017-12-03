/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import common.FileData;
import common.FileExplorer;
import common.FilePermissions;
import common.UserDTO;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Console implements Runnable {

    private final ScreenWriter screen;
    private static final String fileHeader = "FILENAME OWNER PUBLIC/PRIVATE SIZE READ/WRITE";
    //private Controller controller;
    private FileExplorer fileExplorer;

    public Console() throws RemoteException {
        screen = new ScreenWriter();
    }

    public void start(FileExplorer fileExplorer) {
        new Thread(screen).start();
        this.fileExplorer = fileExplorer;
        help();
        //controller = new Controller();
        new Thread(this).start();
    }

    @Override
    public void run() {
        String[] params;
        Scanner sc = new Scanner(System.in);
        UserDTO id = null;
        loose:
        while (true) {
            params = sc.nextLine().toLowerCase().split(" ");
            try {
                switch (params[0]) {
                    case "login":
                        if (id != null) {
                            screen.print("You are already authenticated, please log out first.");
                            break;
                        }
                        if (params.length < 3) {
                            screen.print("Please provide a username and a password separated by space, on the same line as your command.");
                            break;
                        }
                        id = fileExplorer.login(params[1], params[2]);
                        if (id != null) {
                            screen.print("Successfully logged in, your ID is: " + id);
                        } else {
                            screen.print("The given username/password combination was not found.");
                        }
                        break;
                    case "logout":
                        if (id == null) {
                            screen.print("You are not logged in.");
                            break;
                        }
                        fileExplorer.logout(id);
                        id = null;
                        screen.print("Logged out.");
                        break;
                    case "quit":
                        break loose;
                    case "help":
                        help();
                        break;
                    case "register":
                        if (params.length < 3) {
                            screen.print("Please provide a username and a password separated by space, on the same line as your command.");
                            break;
                        }
                        if (fileExplorer.register(params[1], params[2])) {
                            screen.print("Registration successful. You should now be able to log in.");
                        } else {
                            screen.print("A user with that username already exists.");
                        }
                        break;
                    case "unregister":
                        if (id == null) {
                            screen.print("You need to be logged in to unregister.");
                            break;
                        }
                        if (fileExplorer.unRegister(id)) {
                            screen.print("Unregistration successful.");
                            id = null;
                        } else {
                            screen.print("The given username/password combination was not found.");
                        }
                        break;
                    case "upload":
                        if (id == null) {
                            screen.print("You need to be logged in to upload.");
                            break;
                        }
                        if (params.length < 6) {
                            screen.print("Please input file data in the following format: upload <name> <size> <public? true/false> <publically readable? true/false> <publically writable true/false>.");
                            break;
                        }
                        FileData fd = new FileData(params[1], Integer.parseInt(params[2]), new FilePermissions(params[3].equals("true"), params[4].equals("true"), params[5].equals("true")));
                        if (fileExplorer.upload(fd, id)) {
                            screen.print("File uploaded successfully.");
                        } else {
                            screen.print("File could not be uploaded.");
                        }
                        break;
                    case "list":
                        if (id == null) {
                            screen.print("You need to be logged in to list files.");
                            break;
                        }
                        screen.print(fileHeader);
                        for (FileData f : fileExplorer.listFiles(id)) {
                            screen.print(f.toString());
                        }
                        break;
                    case "download":
                        if (id == null) {
                            screen.print("You need to be logged in to download files.");
                            break;
                        }
                        if (params.length < 2) {
                            screen.print("Please provide a file name when performing a download.");
                            break;
                        }
                        screen.print(fileHeader);
                        FileData temp = fileExplorer.download(params[1], id);
                        if (temp == null) {
                            screen.print("No such file exists on the server.");
                        } else {
                            screen.print(temp.toString());
                        }
                        break;
                    case "delete":
                        if (id == null) {
                            screen.print("You need to be logged in to delete files.");
                            break;
                        }
                        if (params.length < 2) {
                            screen.print("Please provide a file name when performing a delete operation.");
                            break;
                        }
                        if (fileExplorer.delete(params[1], id)) {
                            screen.print("File successfully deleted.");
                        } else {
                            screen.print("File could not be deleted.");
                        }
                        break;
                    case "subscribe":
                        if (id == null) {
                            screen.print("You need to be logged in to subscribe to files.");
                            break;
                        }
                        if (params.length < 2) {
                            screen.print("Please provide at least one file name when subscribing to files.");
                            break;
                        }

                        if (fileExplorer.subscribe(Arrays.copyOfRange(params, 1, params.length), id, screen)) {
                            screen.print("Subscribed to files successfully.");
                            break;
                        } else {
                            screen.print("Could not subscribe to one or more files.");
                            break;
                        }
                    case "rename":
                        if (id == null) {
                            screen.print("You need to be logged in to rename files.");
                            break;
                        }
                        if (params.length < 3) {
                            screen.print("Please provide the new and old file names when renaming files.");
                            break;
                        }
                        if (fileExplorer.rename(params[1], params[2], id)) {
                            screen.print("Renamed file successfully.");
                            break;
                        } else {
                            screen.print("Could not rename file.");
                            break;
                        }
                    case "resize":
                        if (id == null) {
                            screen.print("You need to be logged in to resizefiles.");
                            break;
                        }
                        if (params.length < 3) {
                            screen.print("Please provide the name of the file and its new size when resizing.");
                            break;
                        }
                        try {
                            Integer.parseInt(params[2]);
                        } catch (Exception e) {
                            screen.print("The new size must be a numeric value.");
                            break;
                        }
                        if (fileExplorer.resize(params[1], Integer.parseInt(params[2]), id)) {
                            screen.print("resized file successfully.");
                            break;
                        } else {
                            screen.print("Could not resize file.");
                            break;
                        }
                    case "permissions":
                        if (id == null) {
                            screen.print("You need to be logged in to change permissions of files.");
                            break;
                        }
                        if (params.length < 5) {
                            screen.print("Please provide the name of the file if it should be public, publically readable, publically writable.");
                            break;
                        }
                        FilePermissions fp = new FilePermissions(Boolean.parseBoolean(params[2]), Boolean.parseBoolean(params[3]), Boolean.parseBoolean(params[4]));
                        if (fileExplorer.changePermissions(params[1], fp, id)) {
                            screen.print("Changed permissions of file successfully.");
                            break;
                        } else {
                            screen.print("Could not change file permissions.");
                            break;
                        }
                    default:
                        screen.print("Unknown command.\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            screen.print("Exiting application.\n");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    private void help() {
        try {
            screen.print("Commands: login username password, logout, register username password, unregister, list, download file\n"
                    + "upload <name> <size> <public? true/false> <publically readable? true/false> <publically writable true/false>, delete file, quit, help.\n"
                    + "subscribe file1 file2 file3 .., rename file newname, resize file size,\n"
                    + " permissions file <publically readable? true/false> <publically writable true/false>");
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
}
