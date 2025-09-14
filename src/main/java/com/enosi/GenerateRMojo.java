package com.enosi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mojo to generate a Java class with constants for resource files.
 * The generated class will be named R and will contain a constant for each file
 */
@SuppressWarnings("unused")
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class GenerateRMojo extends AbstractMojo {
    private static final String PATH_SEPARATOR = "/";

    /**
     * If true, generated files will be placed in src/main/generated instead of target/generated-sources/java.
     */
    @Parameter(defaultValue = "true")
    private boolean keepInProjectFiles;

    /**
     * Directory containing resource files to scan.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/resources")
    private File resourcesDir;

    /**
     * Java package name for the generated R class.
     */
    @Parameter(defaultValue = "com.enosi.generated")
    private String packageName;

    /**
     * Base directory for generated sources (when keepInProjectFiles is false).
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources")
    private File outputTargetDirectory;

    /**
     * Base directory for generated sources (when keepInProjectFiles is true).
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/java")
    private File outputSrcDirectory;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private org.apache.maven.project.MavenProject project;

    public void execute() throws MojoExecutionException {
        // Choisir le dossier de base
        File baseDir = keepInProjectFiles ? outputSrcDirectory : outputTargetDirectory;
        String packagePath = packageName.replace('.', '/');
        File outputDir = new File(baseDir, packagePath);

        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new MojoExecutionException("Failed to generate R.java" + outputDir);
        }

        File rFile = new File(outputDir, "R.java");

        try (FileWriter writer = new FileWriter(rFile)) {
            generateREnum(writer);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate R.java", e);
        }

        getLog().info("Generated R.java enum with " + countFiles(resourcesDir) + " resource constants");
    }

    /**
     * Generates the R enum source code and writes it to the provided FileWriter.
     * @param writer the FileWriter to write the generated code to
     * @throws IOException if an I/O error occurs
     */
    private void generateREnum(FileWriter writer) throws IOException {
        // Write package and imports
        writer.write("""
                package com.enosi.generated;
                
                import java.io.File;
                import java.io.IOException;
                import java.io.InputStream;
                import java.nio.charset.StandardCharsets;
                import java.nio.file.Path;
                import java.nio.file.Paths;
                import java.net.URL;
                
                """);

        // Write enum declaration
        writer.write("""
                
                /**
                 * Generated resource constants enum.
                 * Contains constants for all resource files with utility methods.
                 */
                public enum R {
                """);

        // Generate enum constants
        List<ResourceInfo> resources = scanAllResources();
        for (int i = 0; i < resources.size(); i++) {
            ResourceInfo resource = resources.get(i);
            String constantName = toValidJavaName(resource.fileName());

            writer.write("    " + constantName + "(\"" + resource.path + "\")");

            if (i < resources.size() - 1) {
                writer.write(",\n");
            } else {
                writer.write(";\n\n");
            }
        }

        // Property and constructor
        writer.write("""
                    private final String resourcePath;
                
                    R(String resourcePath) {
                        this.resourcePath = resourcePath;
                    }
                
                """);

        // Utility methods
        generateUtilityMethods(writer);

        writer.write("}\n");
    }

    private void generateUtilityMethods(FileWriter writer) throws IOException {
        writer.write("""
                    /**
                     * Returns the resource path as string.
                     *
                     * @return the resource path
                     */
                    @Override
                    public String toString() {
                        return resourcePath;
                    }
                
                    /**
                     * Returns the resource path with leading slash for getResourceAsStream().
                     *
                     * @return the resource path with leading slash
                     */
                    public String toResourcePath() {
                        return "/" + resourcePath;
                    }
                
                    /**
                     * Returns a File object pointing to this resource.
                     *
                     * @param baseDir the base directory to resolve against
                     * @return File object
                     */
                    public File toFile(File baseDir) {
                        return new File(baseDir, resourcePath);
                    }
                
                    /**
                     * Reads the entire content of the resource as a UTF-8 string.
                     *
                     * @return content of the resource
                     * @throws IOException if an I/O error occurs
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
                     *
                     * @return Path object
                     */
                    public Path toPath() {
                        return Paths.get(resourcePath);
                    }
                
                    /**
                     * Opens an InputStream for this resource.
                     *
                     * @return InputStream or null if resource not found
                     */
                    public InputStream openStream() {
                        return getClass().getResourceAsStream(toResourcePath());
                    }
                
                    /**
                     * Gets the URL for this resource.
                     *
                     * @return URL or null if resource not found
                     */
                    public URL getURL() {
                        return getClass().getResource(toResourcePath());
                    }
                
                    /**
                     * Gets just the filename part of the resource.
                     *
                     * @return filename
                     */
                    public String getFileName() {
                        int lastSlash = resourcePath.lastIndexOf('/');
                        return lastSlash >= 0 ? resourcePath.substring(lastSlash + 1) : resourcePath;
                    }
                
                    /**
                     * Gets the file extension.
                     *
                     * @return file extension (without dot) or empty string
                     */
                    public String getExtension() {
                        String fileName = getFileName();
                        int lastDot = fileName.lastIndexOf('.');
                        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
                    }
                
                    /**
                     * Checks if the resource exists.
                     *
                     * @return true if resource exists
                     */
                    public boolean exists() {
                        return getURL() != null;
                    }
                """);
    }

    /**
     * Scans the resources directory recursively and collects all resource files.
     * @return list of ResourceInfo objects representing each resource file
     */
    private List<ResourceInfo> scanAllResources() {
        List<ResourceInfo> resources = new ArrayList<>();
        scanResourcesRecursive(resourcesDir, "", resources);
        return resources;
    }

    /**
     * Recursively scans a directory for resource files.
     * @param dir the directory to scan
     * @param currentPath the current relative path within the resources directory
     * @param resources the list to collect found resources
     */
    private void scanResourcesRecursive(File dir, String currentPath, List<ResourceInfo> resources) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String filePath = currentPath.isEmpty() ? file.getName() : currentPath + PATH_SEPARATOR + file.getName();
            if (file.isFile()) {
                String displayName = currentPath.isEmpty() ? file.getName() : currentPath.replace("/", "_") + "_" + file.getName();
                resources.add(new ResourceInfo(displayName, filePath));
            } else if (file.isDirectory()) {
                scanResourcesRecursive(file, filePath, resources);
            }
        }
    }

    private String toValidJavaName(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("^(\\d)", "_$1")
                .toUpperCase();
    }

    private int countFiles(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return 0;

        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFiles(file);
                }
            }
        }
        return count;
    }

    private record ResourceInfo(String fileName, String path) {
    }
}
