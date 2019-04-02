package org.javamodularity.moduleplugin;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

class ModuleName {

    private static final Logger LOGGER = Logging.getLogger(ModuleName.class);

    Optional<String> findModuleName(Project project) {
        SourceSet main;
        try {
            JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
            main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        } catch (IllegalStateException | UnknownDomainObjectException e) {
            LOGGER.warn("Cannot obtain JavaPluginConvention", e);
            return Optional.empty();
        }
        Optional<File> moduleInfoSrcDir = main.getAllJava()
                .getSourceDirectories()
                .getFiles()
                .stream()
                .filter(dir -> dir.toPath().resolve("module-info.java").toFile().exists())
                .findAny();

        if (moduleInfoSrcDir.isPresent()) {
            Path moduleInfoJava = moduleInfoSrcDir.get().toPath().resolve("module-info.java");
            try {
                JavaParser parser = new JavaParser();
                parser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11);
                Optional<CompilationUnit> compilationUnit = parser.parse(moduleInfoJava).getResult();
                if (compilationUnit.isPresent()) {
                    Optional<ModuleDeclaration> module = compilationUnit.get().getModule();
                    if (module.isPresent()) {
                        Name name = module.get().getName();
                        LOGGER.lifecycle("Found module name '{}'", name);
                        return Optional.of(name.toString());
                    } else {
                        LOGGER.warn("module-info.java found, but module name could not be parsed");
                        return Optional.empty();
                    }
                } else {
                    LOGGER.debug("Compilation unit is empty");
                    return Optional.empty();
                }
            } catch (IOException e) {
                LOGGER.error("Error opening module-info.java in source dir {}", moduleInfoJava);
                return Optional.empty();
            }

        } else {
            LOGGER.debug("No module-info.java found in module {}", project.getName());
            return Optional.empty();
        }
    }

}
