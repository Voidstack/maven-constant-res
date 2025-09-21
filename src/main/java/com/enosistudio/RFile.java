package com.enosistudio;

import org.jetbrains.annotations.Contract;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Represents a resource file with utility methods for reading and manipulating resources.
 * Resources files are read-only files bundled within your application (especially when packaged in JARs) and should never be modified at runtime!
 *
 * JAR vs Filesystem compatibility:
 * <ul>
 *   <li><b>Always work</b> (JAR + filesystem): {@code openStream()}, {@code readContent()}, {@code openBufferedReader()}, {@code exists()}, {@code size()}</li>
 *   <li><b>Create temp files for JAR</b>: {@code toFile()}, {@code toPath()}</li>
 *   <li><b>Metadata only</b>: {@code getFileName()}, {@code getExtension()}, {@code getBaseName()}, etc.</li>
 * </ul>
 */
public final class RFile {
    private final String resourcePath;
    private final String fileName;

    public RFile(String resourcePath) {
        // Normalize resource path (remove leading slash if present)
        this.resourcePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        int lastSlash = this.resourcePath.lastIndexOf('/');
        this.fileName = lastSlash >= 0 ? this.resourcePath.substring(lastSlash + 1) : this.resourcePath;
    }

    /**
     * Returns the resource path as string.
     */
    @Override
    public String toString() {
        return resourcePath;
    }

    /**
     * Gets the resource URL from the classpath root.
     * Returns null if resource doesn't exist.
     * Note : Works for both JAR and filesystem resources
     */
    @Contract(pure = true)
    public URL getURL() {
        return getClass().getClassLoader().getResource(resourcePath);
    }

    /**
     * Opens an InputStream for this resource.
     * @throws IOException if the resource cannot be found
     * Note : Works for both JAR and filesystem resources
     */
    @Contract(pure = true)
    public InputStream openStream() throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return stream;
    }

    /**
     * Reads the entire content of the resource as a UTF-8 string.
     * Consider using openBufferedReader() for large files.
     */
    @Contract(pure = true)
    public String readContent() throws IOException {
        return readContent(StandardCharsets.UTF_8);
    }

    /**
     * Reads the entire content of the resource as a string using the specified charset.
     * Consider using openBufferedReader() for large files.
     */
    @Contract(pure = true)
    public String readContent(Charset charset) throws IOException {
        try (InputStream in = openStream()) {
            return new String(in.readAllBytes(), charset);
        }
    }

    /**
     * Opens a BufferedReader for this resource using UTF-8 charset.
     */
    @Contract(pure = true)
    public BufferedReader openBufferedReader() throws IOException {
        return openBufferedReader(StandardCharsets.UTF_8);
    }

    /**
     * Opens a BufferedReader for this resource using the specified charset.
     */
    @Contract(pure = true)
    public BufferedReader openBufferedReader(Charset charset) throws IOException {
        InputStream in = openStream();
        return new BufferedReader(new InputStreamReader(in, charset));
    }

    /**
     * Converts the resource to a File object.
     * If the resource is inside a JAR, it will be copied to a temporary file.
     *
     * Note : JAR resources are extracted to temporary files that are deleted on JVM exit
     */
    @Contract(pure = true)
    public File toFile() throws IOException {
        return createTemporaryFileIfNeeded().toFile();
    }

    /**
     * Converts the resource to a Path object.
     * If the resource is inside a JAR, it will be copied to a temporary file.
     *
     * Note : JAR resources are extracted to temporary files that are deleted on JVM exit
     */
    @Contract(pure = true)
    public Path toPath() throws IOException {
        return createTemporaryFileIfNeeded();
    }

    /**
     * Creates a temporary file if the resource is in a JAR, otherwise returns the direct path.
     */
    private Path createTemporaryFileIfNeeded() throws IOException {
        URL url = getURL();
        if (url == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        // Resource is on filesystem - return direct path
        if ("file".equals(url.getProtocol())) {
            try {
                return Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid resource URL: " + url, e);
            }
        }

        // Resource is in JAR - copy to temporary file
        return copyToTemporaryFile();
    }

    /**
     * Copies the resource to a temporary file.
     */
    private Path copyToTemporaryFile() throws IOException {
        String prefix = "res-";
        String suffix = "-" + fileName;

        try (InputStream in = openStream()) {
            Path tempFile = Files.createTempFile(prefix, suffix);
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    /**
     * Gets just the filename part of the resource.
     */
    @Contract(pure = true)
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the filename without extension.
     */
    @Contract(pure = true)
    public String getBaseName() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(0, lastDot) : fileName;
    }

    /**
     * Gets the file extension (without the dot).
     */
    @Contract(pure = true)
    public String getExtension() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * Guesses the MIME type of the resource based on its content.
     * Consider using a library like Apache Tika for more accurate detection
     * Note : Works for both JAR and filesystem resources
     * @throws IOException if an I/O error occurs
     * @return the MIME type, or null if it cannot be determined
     */
    @Contract(pure = true)
    public String getMimeType() throws IOException {
        return URLConnection.guessContentTypeFromStream(openStream());
    }

    /**
     * Gets the resource path.
     */
    @Contract(pure = true)
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Gets the resource path with a leading slash.
     */
    @Contract(pure = true)
    public String getResourcePathWithSlash() {
        return "/" + resourcePath;
    }

    /**
     * Gets the parent directory path of this resource.
     */
    @Contract(pure = true)
    public String getParentPath() {
        int lastSlash = resourcePath.lastIndexOf('/');
        return lastSlash >= 0 ? resourcePath.substring(0, lastSlash) : "";
    }

    /**
     * Checks if the resource exists.
     */
    @Contract(pure = true)
    public boolean exists() {
        return getURL() != null;
    }

    /**
     * Gets the size of the resource in bytes, or -1 if resource doesn't exist.
     *
     * Note : Works for both JAR and filesystem resources
     */
    @Contract(pure = true)
    public long size() throws IOException {
        URL url = getURL();
        if (url == null) {
            return -1;
        }

        if ("file".equals(url.getProtocol())) {
            try {
                return Files.size(Paths.get(url.toURI()));
            } catch (URISyntaxException e) {
                // Fallback to stream-based size calculation
            }
        }

        // For JAR resources, we need to read the stream to get the size
        try (InputStream in = openStream()) {
            long size = 0;
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                size += bytesRead;
            }
            return size;
        }
    }
}