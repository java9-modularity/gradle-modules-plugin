package org.javamodularity.moduleplugin.internal;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.extensions.PatchModuleExtension;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/** @deprecated As of 1.7.0, this class is no longer used and can be removed */
@Deprecated(since = "1.7.0", forRemoval = true)
public final class PatchModuleResolver {

    private static final Logger LOGGER = Logging.getLogger(PatchModuleResolver.class);

    private final PatchModuleExtension patchModuleExtension;
    /**
     * Takes a JAR name and resolves it to a full JAR path. If returned JAR patch is empty, the JAR is skipped.
     */
    private final UnaryOperator<String> jarNameResolver;

    public PatchModuleResolver(PatchModuleExtension patchModuleExtension, UnaryOperator<String> jarNameResolver) {
        this.patchModuleExtension = patchModuleExtension;
        this.jarNameResolver = jarNameResolver;
    }

    public void mutateArgs(List<String> args) {
        buildOptionStream().forEach(option -> option.mutateArgs(args));
    }

    public Stream<TaskOption> buildOptionStream() {
        return patchModuleExtension.getConfig().stream()
                .map(patch -> patch.split("="))
                .map(this::resolvePatchModuleValue)
                .filter(Objects::nonNull)
                .map(value -> new TaskOption("--patch-module", value));
    }

    private String resolvePatchModuleValue(String[] parts) {
        String moduleName = parts[0];
        String jarName = parts[1];

        String jarPath = jarNameResolver.apply(jarName);
        if (jarPath.isEmpty()) {
            LOGGER.warn("Skipped patching {} into {}", jarName, moduleName);
            return null;
        }
        return moduleName + "=" + jarPath;
    }
}
