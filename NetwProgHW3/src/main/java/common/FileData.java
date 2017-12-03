package common;

import java.io.Serializable;

public class FileData implements Serializable {

    private String fileName, owner = "";
    private int size;
    private FilePermissions permissions;

    public FileData(String name, int size, String owner, FilePermissions permission) {
        this.permissions = permission;
        this.fileName = name;
        this.owner = owner;
        this.size = size;
    }

    public FileData(String name, int size, FilePermissions permission) {
        this.permissions = permission;
        this.fileName = name;
        this.size = size;
    }

    public FilePermissions getPermissions() {
        return permissions;
    }

    public boolean isPublic() {
        return permissions.isPublic();
    }

    public boolean canRead(String name) {
        return (permissions.isPublic() && permissions.readable()) | isOwner(name);
    }

    public boolean canWrite(String name) {
        return (permissions.isPublic() && permissions.writable()) | isOwner(name);
    }

    public boolean isOwner(String name) {
        return owner.equals(name);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fileName);
        sb.append(" ");
        sb.append(owner);
        sb.append(" ");
        sb.append(size);
        sb.append(" ");
        sb.append(isPublic() ? "PUBLIC " : "PRIVATE ");
        sb.append(permissions.toString());
        return sb.toString();
    }

    public String getName() {
        return fileName;
    }

    public int getSize() {
        return size;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
