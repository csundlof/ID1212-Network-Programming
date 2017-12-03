package common;

import java.io.Serializable;

public class FilePermissions implements Serializable {

    private boolean publicFile;
    private boolean read = false, write = false;

    public FilePermissions(boolean publicFile, boolean read, boolean write) {
        this.publicFile = publicFile;
        this.read = publicFile & read;
        this.write = publicFile & write;
    }

    public boolean isPublic() {
        // TODO Auto-generated method stub
        return publicFile;
    }

    public boolean readable() {
        return read;
    }

    public boolean writable() {
        return write;
    }

    public String toString() {
        return (read ? "r" : "-") + (write ? "w" : "-");
    }

}
