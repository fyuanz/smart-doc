package com.smartdoc.agent.maven;

import com.smartdoc.agent.core.ServiceSkillUpdater;
import com.smartdoc.agent.core.SkillGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Generates and safely publishes one service Skill from configured local documents. */
@Mojo(name = "generate-skill", defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public final class GenerateSkillMojo extends AbstractMojo {
    @Parameter
    String serviceId;

    @Parameter
    String skillName;

    @Parameter(defaultValue = "${project.build.directory}/smartdoc")
    File outputDirectory;

    @Parameter(defaultValue = "30")
    long timeoutSeconds;

    @Parameter
    List<DocumentSource> documents;

    @Override
    public void execute() {
        String service = serviceId == null || serviceId.isBlank() ? "<unconfigured>" : serviceId;
        try {
            validateConfiguration();
            List<DocumentSource> configuredDocuments = List.copyOf(documents);
            var result = new ServiceSkillUpdater().update(serviceId, skillName, outputDirectory.toPath(),
                    Duration.ofSeconds(timeoutSeconds),
                    () -> new SkillGenerator().generate(serviceId, skillName, readDocuments(configuredDocuments)));
            String summary = "SmartDoc [" + serviceId + "] " + result.outcome() + ": " + result.message()
                    + "; Skill=" + result.skillDirectory();
            if (result.outcome() == ServiceSkillUpdater.Outcome.SUCCESS) getLog().info(summary);
            else getLog().warn(summary);
        } catch (Exception error) {
            String detail = error.getMessage() == null || error.getMessage().isBlank()
                    ? error.getClass().getSimpleName() : error.getMessage();
            if (!detail.startsWith("CONFIG:")) detail = "CONFIG: " + detail;
            getLog().warn("SmartDoc [" + service + "] FAILED: " + detail);
            getLog().debug("SmartDoc update failure", error);
        }
    }

    private void validateConfiguration() {
        if (serviceId == null || serviceId.isBlank()) throw new IllegalArgumentException("CONFIG: serviceId is required");
        if (skillName == null || skillName.isBlank()) throw new IllegalArgumentException("CONFIG: skillName is required");
        if (outputDirectory == null) throw new IllegalArgumentException("CONFIG: outputDirectory is required");
        if (timeoutSeconds <= 0) throw new IllegalArgumentException("CONFIG: timeoutSeconds must be positive");
        if (documents == null || documents.isEmpty())
            throw new IllegalArgumentException("CONFIG: at least one document is required");
    }

    private Map<String, byte[]> readDocuments(List<DocumentSource> configuredDocuments) throws Exception {
        var result = new TreeMap<String, byte[]>();
        for (DocumentSource document : configuredDocuments) {
            if (document == null || document.getId() == null || document.getId().isBlank())
                throw new IllegalArgumentException("CONFIG: every document requires an id");
            if (document.getPath() == null)
                throw new IllegalArgumentException(document.getId() + ": CONFIG: document path is required");
            if (result.containsKey(document.getId()))
                throw new IllegalArgumentException(document.getId() + ": CONFIG: duplicate document id");
            var path = document.getPath().toPath().toAbsolutePath().normalize();
            if (!Files.isRegularFile(path))
                throw new IllegalArgumentException(document.getId() + ": INPUT: required document is unavailable at " + path);
            result.put(document.getId(), Files.readAllBytes(path));
        }
        return result;
    }
}
