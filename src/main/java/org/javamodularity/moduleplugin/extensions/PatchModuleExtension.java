package org.javamodularity.moduleplugin.extensions;

import org.gradle.api.file.FileCollection;
import org.javamodularity.moduleplugin.internal.TaskOption;
import org.javamodularity.moduleplugin.internal.PatchModuleResolver;

import java.io.File;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * @deprecated As of 1.7.0, the preferred way to patch modules is via the {@link ModularityExtension#patchModule(String, String)} method.
 */
@Deprecated(since = "1.7.0")
public class PatchModuleExtension {
    private List<String> config = new ArrayList<>();
    private Map<String, String> indexByJar = new HashMap<>();

    public List<String> getConfig() {
        return config;
    }

    public void setConfig(List<String> config) {
        this.config = config;
        indexByJar = config.stream().map(s -> s.split("=")).collect(Collectors.toMap(c -> c[1], c -> c[0]));
    }

    /** @deprecated As of 1.7.0, this method is no longer used and can be removed */
    @Deprecated(since = "1.7.0", forRemoval = true)
    public PatchModuleResolver resolvePatched(FileCollection classpath) {
        return resolvePatched(jarName -> classpath.filter(jar -> jar.getName().endsWith(jarName)).getAsPath());
    }

    /** @deprecated As of 1.7.0, this method is no longer used and can be removed */
    @Deprecated(since = "1.7.0", forRemoval = true)
    public PatchModuleResolver resolvePatched(UnaryOperator<String> jarNameResolver) {
        return new PatchModuleResolver(this, jarNameResolver);
    }

    /** @deprecated As of 1.7.0, this method is no longer used and can be removed */
    @Deprecated(since = "1.7.0", forRemoval = true)
    public Set<String> getJars() {
        return indexByJar.keySet();
    }

    /** @deprecated As of 1.7.0, this method is no longer used and can be removed */
    @Deprecated(since = "1.7.0", forRemoval = true)
    public boolean isUnpatched(File jar) {
        return !indexByJar.containsKey(jar.getName());
    }

    /** @deprecated As of 1.7.0, this method is no longer used and can be removed */
    @Deprecated(since = "1.7.0", forRemoval = true)
    public Optional<TaskOption> buildModulePathOption(FileCollection classpath) {
        String modulePath = classpath.filter(this::isUnpatched).getAsPath();
        if (modulePath.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TaskOption("--module-path", modulePath));
    }

    @Override
    public String toString() {
        return "PatchModuleExtension{" +
                "config=" + config +
                '}';
    }
}
