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
 * Resource files are read-only files bundled within your application (especially when packaged in JARs)
 * and should never be modified at runtime.
 *
 * <p><b>JAR vs Filesystem compatibility:</b></p>
 * <ul>
 *   <li><b>Always work</b> (JAR + filesystem): {@link #openStream()}, {@link #readContent()},
 *       {@link #openBufferedReader()}, {@link #exists()}, {@link #size()}</li>
 *   <li><b>Create temp files for JAR</b>: {@link #toFile()}, {@link #toPath()}</li>
 *   <li><b>Metadata only</b>: {@link #getFileName()}, {@link #getExtension()},
 *       {@link #getBaseName()}, etc.</li>
 * </ul>
 */
public final class RFile {
    private final String resourcePath;
    private final String fileName;

    /**
     * Creates a new RFile instance for the specified resource path.
     *
     * @param resourcePath the resource path (leading slash will be normalized away)
     * @throws IllegalArgumentException if resourcePath is null or empty
     */
    public RFile(String resourcePath) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be null or empty");
        }

        // Normalize resource path (remove leading slash if present)
        this.resourcePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        int lastSlash = this.resourcePath.lastIndexOf('/');
        this.fileName = lastSlash >= 0 ? this.resourcePath.substring(lastSlash + 1) : this.resourcePath;
    }

    /**
     * Returns the resource path as string.
     *
     * @return the normalized resource path without leading slash
     */
    @Override
    public String toString() {
        return resourcePath;
    }

    /**
     * Gets the absolute URL from the classpath root.
     *
     * @return the absolute resource URL, or {@code null} if resource doesn't exist
     * <p><b>Note:</b> Works for both JAR and filesystem resources.</p>
     */
    @Contract(pure = true)
    public URL getAbsoluteURL() {
        return getClass().getClassLoader().getResource(resourcePath);
    }

    /**
     * Opens an InputStream for this resource.
     *
     * @return a new InputStream for reading the resource
     * @throws IOException if the resource cannot be found or opened
     * <p><b>Note:</b> Works for both JAR and filesystem resources.</p>
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
     *
     * @return the complete resource content as a UTF-8 string
     * @throws IOException if the resource cannot be read
     * <p><b>Note:</b> Consider using {@link #openBufferedReader()} for large files.</p>
     * @see #readContent(Charset)
     */
    @Contract(pure = true)
    public String readContent() throws IOException {
        return readContent(StandardCharsets.UTF_8);
    }

    /**
     * Reads the entire content of the resource as a string using the specified charset.
     *
     * @param charset the charset to use for decoding the resource content
     * @return the complete resource content as a string
     * @throws IOException if the resource cannot be read
     * @throws NullPointerException if charset is null
     * <p><b>Note:</b> Consider using {@link #openBufferedReader(Charset)} for large files.</p>
     * @see #readContent()
     */
    @Contract(pure = true)
    public String readContent(Charset charset) throws IOException {
        if (charset == null) {
            throw new NullPointerException("Charset cannot be null");
        }
        try (InputStream in = openStream()) {
            return new String(in.readAllBytes(), charset);
        }
    }

    /**
     * Opens a BufferedReader for this resource using UTF-8 charset.
     *
     * @return a new BufferedReader for reading the resource
     * @throws IOException if the resource cannot be opened
     * @see #openBufferedReader(Charset)
     */
    @Contract(pure = true)
    public BufferedReader openBufferedReader() throws IOException {
        return openBufferedReader(StandardCharsets.UTF_8);
    }

    /**
     * Opens a BufferedReader for this resource using the specified charset.
     *
     * @param charset the charset to use for decoding
     * @return a new BufferedReader for reading the resource
     * @throws IOException if the resource cannot be opened
     * @throws NullPointerException if charset is null
     * @see #openBufferedReader()
     */
    @Contract(pure = true)
    public BufferedReader openBufferedReader(Charset charset) throws IOException {
        if (charset == null) {
            throw new NullPointerException("Charset cannot be null");
        }
        InputStream in = openStream();
        return new BufferedReader(new InputStreamReader(in, charset));
    }

    /**
     * Converts the resource to a File object.
     * If the resource is inside a JAR, it will be copied to a temporary file.
     *
     * @return a File object representing this resource
     * @throws IOException if the resource cannot be accessed or copied
     * <p><b>Note:</b> JAR resources are extracted to temporary files that are deleted on JVM exit.</p>
     * @see #toPath()
     */
    @Contract(pure = true)
    public File toFile() throws IOException {
        return createTemporaryFileIfNeeded().toFile();
    }

    /**
     * Converts the resource to a Path object.
     * If the resource is inside a JAR, it will be copied to a temporary file.
     *
     * @return a Path object representing this resource
     * @throws IOException if the resource cannot be accessed or copied
     * <p><b>Note:</b> JAR resources are extracted to temporary files that are deleted on JVM exit.</p>
     * @see #toFile()
     */
    @Contract(pure = true)
    public Path toPath() throws IOException {
        return createTemporaryFileIfNeeded();
    }

    /**
     * Creates a temporary file if the resource is in a JAR, otherwise returns the direct path.
     *
     * @return the path to the resource (direct or temporary file)
     * @throws IOException if the resource cannot be accessed or copied
     */
    private Path createTemporaryFileIfNeeded() throws IOException {
        URL url = getAbsoluteURL();
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
     *
     * @return the path to the temporary file
     * @throws IOException if the resource cannot be copied
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
     *
     * @return the filename (last segment after the last slash)
     * @see #getBaseName()
     * @see #getExtension()
     */
    @Contract(pure = true)
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the filename without extension.
     *
     * @return the base filename without extension
     * @see #getFileName()
     * @see #getExtension()
     */
    @Contract(pure = true)
    public String getBaseName() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(0, lastDot) : fileName;
    }

    /**
     * Gets the file extension (without the dot).
     *
     * @return the file extension, or empty string if no extension
     * @see #getFileName()
     * @see #getBaseName()
     */
    @Contract(pure = true)
    public String getExtension() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }

    /**
     * Guesses the MIME type of the resource based on its content.
     *
     * @return the MIME type, or {@code null} if it cannot be determined
     * @throws IOException if an I/O error occurs while reading the resource
     * <p><b>Note:</b> Works for both JAR and filesystem resources. Consider using a library
     *          like Apache Tika for more accurate detection.</p>
     */
    @Contract(pure = true)
    public String getMimeType() throws IOException {
        return URLConnection.guessContentTypeFromStream(openStream());
    }

    /**
     * Gets the resource path.
     *
     * @return the normalized resource path without leading slash
     * @see #getResourcePathWithSlash()
     */
    @Contract(pure = true)
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Gets the resource path with a leading slash.
     *
     * @return the resource path prefixed with a slash
     * @see #getResourcePath()
     */
    @Contract(pure = true)
    public String getResourcePathWithSlash() {
        return "/" + resourcePath;
    }

    /**
     * Gets the parent directory path of this resource.
     *
     * @return the parent directory path, or empty string if resource is at root
     */
    @Contract(pure = true)
    public String getParentResourcePath() {
        int lastSlash = resourcePath.lastIndexOf('/');
        return lastSlash >= 0 ? resourcePath.substring(0, lastSlash) : "";
    }

    /**
     * Checks if the resource exists.
     *
     * @return {@code true} if the resource exists, {@code false} otherwise
     * <p><b>Note:</b> Works for both JAR and filesystem resources.</p>
     */
    @Contract(pure = true)
    public boolean exists() {
        return getAbsoluteURL() != null;
    }

    /**
     * Gets the size of the resource in bytes.
     *
     * @return the size in bytes, or {@code -1} if resource doesn't exist
     * @throws IOException if an I/O error occurs while calculating the size
     * <p><b>Note:</b> Works for both JAR and filesystem resources.</p>
     */
    @Contract(pure = true)
    public long size() throws IOException {
        URL url = getAbsoluteURL();
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