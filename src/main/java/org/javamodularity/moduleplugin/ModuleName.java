package org.javamodularity.moduleplugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.GradleVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

class ModuleName {

    private static final Logger LOGGER = Logging.getLogger(ModuleName.class);

    Optional<String> findModuleName(Project project) {
        SourceSet main;
        try {
            if (GradleVersion.current().compareTo(GradleVersion.version("7.1")) >= 0) {
                // JavaPluginExtension#getSourceSets() is supported from Gradle 7.1
                // https://docs.gradle.org/7.1/javadoc/org/gradle/api/plugins/JavaPluginExtension.html#getSourceSets--
                JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
                main = javaPluginExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            } else {
                JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
                main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            }
        } catch (IllegalStateException | UnknownDomainObjectException e) {
            LOGGER.warn("Cannot obtain JavaPluginConvention", e);
            return Optional.empty();
        }

        Optional<Path> moduleInfoJava = main.getAllJava()
                .getSourceDirectories()
                .getFiles()
                .stream()
                .map(sourceDir -> sourceDir.toPath().resolve("module-info.java"))
                .filter(Files::exists)
                .findAny();

        String projectPath = project.getPath();
        if (moduleInfoJava.isEmpty()) {
            LOGGER.lifecycle("Project {} => no module-info.java found", projectPath);
            return Optional.empty();
        }

        return findModuleName(moduleInfoJava.get(), projectPath);
    }

    private Optional<String> findModuleName(Path moduleInfoJava, String projectPath) {
        try {
            JavaParser parser = new JavaParser();
            parser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11);

            Optional<CompilationUnit> compilationUnit = parser.parse(moduleInfoJava).getResult();
            if (compilationUnit.isEmpty()) {
                LOGGER.debug("Project {} => compilation unit is empty", projectPath);
                return Optional.empty();
            }

            Optional<ModuleDeclaration> module = compilationUnit.get().getModule();
            if (module.isEmpty()) {
                LOGGER.warn("Project {} => module-info.java found, but module name could not be parsed", projectPath);
                return Optional.empty();
            }

            String name = module.get().getName().toString();
            LOGGER.lifecycle("Project {} => '{}' Java module", projectPath, name);
            return Optional.of(name);

        } catch (IOException e) {
            LOGGER.error("Project {} => error opening module-info.java at {}", projectPath, moduleInfoJava);
            return Optional.empty();
        }
    }

}
