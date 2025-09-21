package com.enosistudio;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base class for resource folders with utility methods for managing resource directory paths.
 * Provides consistent path handling for resource folders in both JAR and filesystem environments.
 *
 * <p><b>JAR vs Filesystem compatibility:</b></p>
 * <ul>
 *   <li><b>Always work:</b> {@link #getName()}, {@link #getResourcePath()}, {@link #getAbsoluteURL()}, {@link #getAbsolutePath()}, {@link #exists()}</li>
 * </ul>
 */
public class RFolder {
    protected final String folderName;
    protected final String folderPath;

    /**
     * Creates a new RFolder instance with the specified folder name and path.
     *
     * @param folderName the name of the folder
     * @param folderPath the complete path to the folder
     * @throws IllegalArgumentException if folderName or folderPath is null or empty
     */
    protected RFolder(String folderName, String folderPath) {
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be null or empty");
        }
        if (folderPath == null || folderPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder path cannot be null or empty");
        }

        this.folderName = folderName;
        this.folderPath = folderPath;
    }

    /**
     * Gets the folder name.
     *
     * @return the folder name
     */
    @Contract(pure = true)
    public String getName() {
        return folderName;
    }

    /**
     * Gets the resource path (relative to classpath root).
     *
     * @return the resource path (e.g., "assets/images")
     */
    @Contract(pure = true)
    public String getResourcePath() {
        return folderPath;
    }

    /**
     * Gets the absolute URL for this folder from the classpath.
     *
     * @return the absolute folder URL, or {@code null} if folder doesn't exist
     */
    @Contract(pure = true)
    public URL getAbsoluteURL() {
        String normalizedPath = folderPath.startsWith("/") ? folderPath.substring(1) : folderPath;
        return getClass().getClassLoader().getResource(normalizedPath);
    }

    /**
     * Gets the absolute path for this folder.
     *
     * @return an absolute Path object (supports both filesystem and JAR resources)
     * @throws IOException if the folder cannot be accessed or path cannot be resolved
     */
    @Contract(pure = true)
    public Path getAbsolutePath() throws IOException {
        URL url = getAbsoluteURL();
        if (url == null) {
            throw new IOException("Resource folder not found: " + folderPath);
        }

        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid resource folder URL: " + url, e);
        }
    }

    /**
     * Checks if the resource folder exists.
     *
     * @return {@code true} if the folder exists, {@code false} otherwise
     */
    @Contract(pure = true)
    public boolean exists() {
        return getAbsoluteURL() != null;
    }

    /**
     * Returns the resource path as string.
     *
     * @return the resource path
     */
    @Override
    public String toString() {
        return folderPath;
    }
}