package org.javamodularity.moduleplugin.internal;

import org.gradle.api.Project;
import org.javamodularity.moduleplugin.JavaProjectHelper;

import java.util.List;

public class MutatorHelper {

    public static void configureModuleVersion(JavaProjectHelper helper, List<String> args) {
        String version = helper.modularityExtension().optionContainer().getModuleVersion();
        if(version == null) {
            Object projectVersion = helper.project().getVersion();
            if(projectVersion != null) {
                version = projectVersion.toString();
            }
        }
        if((version != null) && !Project.DEFAULT_VERSION.equals(version)) {
            new TaskOption("--module-version", version).mutateArgs(args);
        }
    }
}
