package com.enosi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Mojo to generate a Java class with constants for resource files.
 * The generated class will be named R and will contain a constant for each file
 */
@Mojo(name = "generate-r", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresProject = true)
public class GenerateRMojo extends AbstractMojo {

    /**
     * If true, generated files will be placed in src/main/generated instead of target/generated-sources/java.
     */
    @Parameter(defaultValue = "false")
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
    private File targetBaseDir;

    /**
     * Base directory for generated sources (when keepInProjectFiles is true).
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/java")
    private File srcBaseDir;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private org.apache.maven.project.MavenProject project;

    public void execute() throws MojoExecutionException {
        // Choisir le dossier de base
        File baseDir = keepInProjectFiles ? srcBaseDir : targetBaseDir;
        String packagePath = packageName.replace('.', '/');
        File outputDir = new File(baseDir, packagePath);
        if (!outputDir.exists()) outputDir.mkdirs();

        File rFile = new File(outputDir, "R.java");

        try (FileWriter writer = new FileWriter(rFile)) {
            writer.write("package " + packageName + ";\n\npublic class R {\n");

            for (File file : resourcesDir.listFiles()) {
                String name = file.getName().replaceAll("[^a-zA-Z0-9]", "_");
                writer.write("    public static final String " + name + " = \"" + file.getName() + "\";\n");
            }

            writer.write("}\n");
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate R.java", e);
        }
    }
}
