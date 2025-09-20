package com.enosistudio;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.annotations.Contract;

/**
 * Represents a resource file with utility methods.
 */
public final class RFile {
    private final String resourcePath;
    private final String fileName;

    public RFile(String resourcePath) {
        this.resourcePath = resourcePath;
        int lastSlash = resourcePath.lastIndexOf('/');
        this.fileName = lastSlash >= 0 ? resourcePath.substring(lastSlash + 1) : resourcePath;
    }

    /**
     * Returns the resource path as string.
     */
    @Override
    public String toString() {
        return resourcePath;
    }

    /**
     * Returns the resource path with leading slash for getResourceAsStream().
     */
    @Contract(pure = true)
    public String toResourcePath() {
        return "/" + resourcePath;
    }

    /**
     * Returns a File object pointing to this resource.
     */
    public File toFile(File baseDir) {
        return new File(baseDir, resourcePath);
    }

    /**
     * Reads the entire content of the resource as a UTF-8 string.
     */
    public String readContent() throws IOException {
        try (InputStream in = openStream()) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Returns a Path object for this resource.
     */
    public Path toPath() {
        return Paths.get(resourcePath);
    }

    /**
     * Opens an InputStream for this resource.
     */
    public InputStream openStream() {
        return getClass().getResourceAsStream(toResourcePath());
    }

    /**
     * Gets the URL for this resource.
     */
    public URL getURL() {
        return getClass().getResource(toResourcePath());
    }

    /**
     * Gets just the filename part of the resource.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the file extension.
     */
    public String getExtension() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * Gets the full resource path.
     */
    public String getPath() {
        return resourcePath;
    }

    /**
     * Checks if the resource exists.
     */
    public boolean exists() {
        return getURL() != null;
    }
}