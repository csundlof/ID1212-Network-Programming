/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.integration;

import common.FileData;
import common.FilePermissions;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import common.UserDTO;
import java.util.ArrayList;
import server.model.User;

public class FileDatabaseDAO {

    private static final String USER_TABLE = "FILE_APP_USERS";
    private static final String[] USER_TABLE_COLUMNS = {"username", "password"};
    private static final String FILE_TABLE = "FILE_APP_FILES";
    private static final String[] FILE_TABLE_COLUMNS = {"name", "size", "owner", "publicFile", "readPerm", "writePerm"};
    private PreparedStatement registerAccountStmt;
    private PreparedStatement unregisterAccountStmt;
    private PreparedStatement findAccountStmt;
    private PreparedStatement saveFileStmt;
    private PreparedStatement deleteFileStmt;
    private PreparedStatement getFileStmt;
    private PreparedStatement listFilesStmt;
    private PreparedStatement renameFileStmt;
    private PreparedStatement resizeFileStmt;
    private PreparedStatement changePermissionOfFileStmt;

    public FileDatabaseDAO(String dbms, String datasource) throws FileDBException {
        try {
            Connection connection = createDatasource(dbms, datasource);
            prepareStatements(connection);
        } catch (ClassNotFoundException | SQLException exception) {
            throw new FileDBException("Could not connect to datasource.", exception);
        }
    }

    public void saveFile(FileData fd) throws FileDBException {
        String errMsg = "Could not save file with name " + fd.getName();
        try {
            saveFileStmt.setString(1, fd.getName());
            saveFileStmt.setInt(2, fd.getSize());
            saveFileStmt.setString(3, fd.getOwner());
            saveFileStmt.setBoolean(4, fd.isPublic());
            saveFileStmt.setBoolean(5, fd.getPermissions().readable());
            saveFileStmt.setBoolean(6, fd.getPermissions().writable());
            int rows = saveFileStmt.executeUpdate();
            if (rows != 1) {
                throw new FileDBException(errMsg);
            }
        } catch (SQLException e) {
            throw new FileDBException(errMsg, e);
        }
    }

    public FileData getFile(String name, UserDTO user) throws FileDBException {
        String errMsg = "Could not get file with name " + name;
        ResultSet result = null;
        try {
            getFileStmt.setString(1, name);
            getFileStmt.setString(2, user.getName());
            result = getFileStmt.executeQuery();
            if (result.next()) {
                return resultToFileData(result);
            }
        } catch (SQLException e) {
            throw new FileDBException(errMsg, e);
        } finally {
            try {
                result.close();
            } catch (SQLException ex) {
                throw new FileDBException(errMsg, ex);
            }
        }
        return null;
    }

    private FileData resultToFileData(ResultSet result) throws SQLException {
        FilePermissions perm = new FilePermissions(result.getBoolean(FILE_TABLE_COLUMNS[3]), result.getBoolean(FILE_TABLE_COLUMNS[4]),
                result.getBoolean(FILE_TABLE_COLUMNS[5]));
        return new FileData(result.getString(FILE_TABLE_COLUMNS[0]), result.getInt(FILE_TABLE_COLUMNS[1]), result.getString(FILE_TABLE_COLUMNS[2]),
                perm);
    }

    public ArrayList<FileData> listFiles(UserDTO user) throws FileDBException {
        String errMsg = "Could not list files.";
        ArrayList<FileData> files = new ArrayList<FileData>();
        ResultSet result = null;
        try {
            listFilesStmt.setString(1, user.getName());
            result = listFilesStmt.executeQuery();
            while (result.next()) {
                files.add(resultToFileData(result));
            }
        } catch (SQLException ex) {
            throw new FileDBException(errMsg, ex);
        } finally {
            try {
                result.close();
            } catch (SQLException ex) {
                throw new FileDBException(errMsg, ex);
            }
        }
        return files;
    }

    public void deleteFile(FileData fd) throws FileDBException {
        try {
            deleteFileStmt.setString(1, fd.getName());
            deleteFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new FileDBException("Could not delete the file: " + fd.getName(), sqle);
        }
    }

    public void unregisterAccount(UserDTO user) throws FileDBException {
        try {
            unregisterAccountStmt.setString(1, user.getName());
            unregisterAccountStmt.setString(2, user.getPassword());
            unregisterAccountStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new FileDBException("Could not unregister the user: " + user.getName(), sqle);
        }
    }

    public void registerAccount(String name, String password) throws FileDBException {
        try {
            registerAccountStmt.setString(1, name);
            registerAccountStmt.setString(2, password);
            registerAccountStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new FileDBException("Could not register the user: " + name, sqle);
        }
    }

    public User findAccount(String name) throws FileDBException {
        String errMsg = "Could not get user with name " + name;
        ResultSet result = null;
        try {
            findAccountStmt.setString(1, name);
            result = findAccountStmt.executeQuery();
            if (result.next()) {
                return new User(result.getString(USER_TABLE_COLUMNS[0]), result.getString(USER_TABLE_COLUMNS[1]), this);
            }
        } catch (SQLException e) {
            throw new FileDBException(errMsg, e);
        } finally {
            try {
                result.close();
            } catch (SQLException ex) {
                throw new FileDBException(errMsg, ex);
            }
        }
        return null;
    }

    private Connection createDatasource(String dbms, String datasource) throws ClassNotFoundException, SQLException, FileDBException {
        Connection connection = connectToDB(dbms, datasource);
        if (!tablesExist(connection)) {
            String a = "CREATE TABLE " + FILE_TABLE + " (" + FILE_TABLE_COLUMNS[0] + " VARCHAR(32) PRIMARY KEY, "
                    + FILE_TABLE_COLUMNS[1] + " int, " + FILE_TABLE_COLUMNS[2] + " VARCHAR(32), " + FILE_TABLE_COLUMNS[3] + " BOOLEAN, "
                    + FILE_TABLE_COLUMNS[4] + " BOOLEAN, " + FILE_TABLE_COLUMNS[5] + " BOOLEAN)";
            System.out.println(a);
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE " + USER_TABLE + " (" + USER_TABLE_COLUMNS[0] + " VARCHAR(32) PRIMARY KEY, "
                    + USER_TABLE_COLUMNS[1] + " VARCHAR(32))");
            Statement statement2 = connection.createStatement();
            statement2.executeUpdate("CREATE TABLE " + FILE_TABLE + " (" + FILE_TABLE_COLUMNS[0] + " VARCHAR(32) PRIMARY KEY, "
                    + FILE_TABLE_COLUMNS[1] + " int, " + FILE_TABLE_COLUMNS[2] + " VARCHAR(32), " + FILE_TABLE_COLUMNS[3] + " BOOLEAN, "
                    + FILE_TABLE_COLUMNS[4] + " BOOLEAN, " + FILE_TABLE_COLUMNS[5] + " BOOLEAN)");
        }
        return connection;
    }

    private void prepareStatements(Connection connection) throws SQLException {
        registerAccountStmt = connection.prepareStatement("INSERT INTO " + USER_TABLE + " VALUES (?, ?)");
        unregisterAccountStmt = connection.prepareStatement("DELETE FROM " + USER_TABLE + " where username = ? AND password = ?");
        findAccountStmt = connection.prepareStatement("SELECT * FROM " + USER_TABLE + " where username = ?");
        saveFileStmt = connection.prepareStatement("INSERT INTO " + FILE_TABLE + " VALUES (?, ?, ?, ?, ?, ?)");
        deleteFileStmt = connection.prepareStatement("DELETE FROM " + FILE_TABLE + " where name = ?");
        getFileStmt = connection.prepareStatement("SELECT * FROM " + FILE_TABLE + " where name = ? AND (owner = ? OR (publicFile = TRUE AND readPerm = TRUE))");
        listFilesStmt = connection.prepareStatement("Select * FROM " + FILE_TABLE + " where owner = ? OR (publicFile = TRUE AND readPerm = TRUE)");
        renameFileStmt = connection.prepareStatement("UPDATE " + FILE_TABLE + " SET " + FILE_TABLE_COLUMNS[0] + " = ? where " + FILE_TABLE_COLUMNS[0] + " = ?");
        resizeFileStmt = connection.prepareStatement("UPDATE " + FILE_TABLE + " SET " + FILE_TABLE_COLUMNS[1] + " = ? where " + FILE_TABLE_COLUMNS[0] + " = ?");
        changePermissionOfFileStmt = connection.prepareStatement("UPDATE " + FILE_TABLE + " SET " + FILE_TABLE_COLUMNS[3] + " = ?, "
                + FILE_TABLE_COLUMNS[4] + " = ?," + FILE_TABLE_COLUMNS[5] + " = ? where " + FILE_TABLE_COLUMNS[0] + " = ?");
    }

    private Connection connectToDB(String dbms, String datasource) throws ClassNotFoundException, SQLException, FileDBException {
        if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            return DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else {
            throw new FileDBException("Unable to create datasource, unknown dbms.");
        }
    }

    private boolean tablesExist(Connection connection) throws SQLException {
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        boolean[] tableExists = new boolean[2];
        try (ResultSet rs = dbm.getTables(null, null, null, null)) {
            for (; rs.next();) {
                if (rs.getString(tableNameColumn).equals(USER_TABLE)) {
                    tableExists[0] = true;
                } else if (rs.getString(tableNameColumn).equals(FILE_TABLE)) {
                    tableExists[1] = true;
                }
            }
        }
        return tableExists[0] && tableExists[1];
    }

    public void rename(FileData fd, String newName) throws FileDBException {
        try {
            renameFileStmt.setString(1, newName);
            renameFileStmt.setString(2, fd.getName());
            renameFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new FileDBException("Could not rename the file: " + fd.getName(), sqle);
        }
    }

    public void resize(FileData fd, int newSize) throws FileDBException {
        try {
            resizeFileStmt.setInt(1, newSize);
            resizeFileStmt.setString(2, fd.getName());
            resizeFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new FileDBException("Could not resize the file: " + fd.getName(), sqle);
        }
    }

    public void changePermissions(FileData fd, FilePermissions permissions) throws FileDBException {
        try {
            changePermissionOfFileStmt.setBoolean(1, permissions.isPublic());
            changePermissionOfFileStmt.setBoolean(2, permissions.readable());
            changePermissionOfFileStmt.setBoolean(3, permissions.writable());
            changePermissionOfFileStmt.setString(4, fd.getName());
            changePermissionOfFileStmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new FileDBException("Could not change permissions of file: " + fd.getName(), sqle);
        }
    }
}
