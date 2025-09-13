package com.enosi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Mojo(name = "generate-r", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresProject = true)
public class GenerateRMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}/src/main/resources")
    private File resourcesDir;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/java/com/enosi/generated")
    private File outputDir;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private org.apache.maven.project.MavenProject project;

    public void execute() throws MojoExecutionException {
        if (!outputDir.exists()) outputDir.mkdirs();
        File rFile = new File(outputDir, "R.java");

        try (FileWriter writer = new FileWriter(rFile)) {
            writer.write("package com.enosi.generated;\n\npublic class R {\n");

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
