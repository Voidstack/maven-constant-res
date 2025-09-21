package com.enosistudio;

import org.jetbrains.annotations.Contract;

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
    @Contract(pure = true)
    public String getName() {
        return folderName;
    }

    /**
     * Gets the full folder path.
     */
    @Contract(pure = true)
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