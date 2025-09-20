package com.enosistudio;

/**
 * Base class for resource folders with utility methods.
 */
public class RFolder {
    protected final String folderName;
    protected final String folderPath;

    protected RFolder(String folderName, String folderPath) {
        this.folderName = folderName;
        this.folderPath = folderPath;
    }

    /**
     * Gets the folder name.
     */
    public String getName() {
        return folderName;
    }

    /**
     * Gets the full folder path.
     */
    public String getPath() {
        return folderPath;
    }

    /**
     * Returns the folder path as string.
     */
    @Override
    public String toString() {
        return folderPath;
    }
}