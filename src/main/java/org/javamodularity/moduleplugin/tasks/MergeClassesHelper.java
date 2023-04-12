package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;
import org.gradle.util.GradleVersion;
import org.javamodularity.moduleplugin.JavaProjectHelper;
import org.javamodularity.moduleplugin.extensions.CompileModuleOptions;
import org.javamodularity.moduleplugin.internal.StreamHelper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MergeClassesHelper {
    private static final Logger LOGGER = Logging.getLogger(MergeClassesHelper.class);

    public static final String MERGE_CLASSES_TASK_NAME = "mergeClasses";
    public static final List<String> PRE_JAVA_COMPILE_TASK_NAMES = List.of("compileKotlin", JavaProjectHelper.COMPILE_TEST_FIXTURES_KOTLIN_TASK_NAME);
    public static final List<String> POST_JAVA_COMPILE_TASK_NAMES = List.of("compileGroovy");

    private final Project project;

    public MergeClassesHelper(Project project) {
        this.project = project;
    }

    public Project project() {
        return project;
    }

    public Stream<CompileTaskWrapper> otherCompileTaskStream() {
        return otherCompileTaskNameStream()
                .map(name -> helper().findTask(name, Task.class))
                .flatMap(Optional::stream)
                .map(this::toWrapper);
    }

    private Stream<String> otherCompileTaskNameStream() {
        return StreamHelper.concat(
                PRE_JAVA_COMPILE_TASK_NAMES.stream(),
                POST_JAVA_COMPILE_TASK_NAMES.stream(),
                Stream.of(CompileModuleOptions.COMPILE_MODULE_INFO_TASK_NAME)
        );
    }

    private CompileTaskWrapper javaCompileTask() {
        return toWrapper(helper().task(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class));
    }

    public Stream<CompileTaskWrapper> allCompileTaskStream() {
        return Stream.concat(Stream.of(javaCompileTask()), otherCompileTaskStream());
    }

    public boolean isMergeRequired() {
        return otherCompileTaskStream().anyMatch(CompileTaskWrapper::hasSource);
    }

    public Sync createMergeClassesTask() {
        Sync sync = project.getTasks().create(MERGE_CLASSES_TASK_NAME, Sync.class);
        sync.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
        return sync;
    }

    public FileCollection getMergeAdjustedClasspath(FileCollection classpath) {
        File testFixturesJar = helper().findTask("testFixturesJar", Jar.class)
                .map(task -> task.getArchiveFile().get().getAsFile())
                .orElse(null);
        if(testFixturesJar != null) {
            classpath = classpath.minus(project.files(testFixturesJar));
        }

        boolean mergeRequired = isMergeRequired();
        if (!mergeRequired) {
            return classpath;
        }

        Set<File> files = new HashSet<>(classpath.getFiles());
        allCompileTaskStream().map(CompileTaskWrapper::getDestinationDir).forEach(files::remove);
        files.add(helper().getMergedDir());
        return project.files(files.toArray());
    }

    private CompileTaskWrapper toWrapper(Task task) {
        return task instanceof AbstractCompile
            ? new GradleTaskWrapper((AbstractCompile) task)
            : new ReflectionTaskWrapper(task);
    }

    private JavaProjectHelper helper() {
        return new JavaProjectHelper(project);
    }

    public interface CompileTaskWrapper {
        Task getTask();

        File getDestinationDir();

        boolean hasSource();
    }

    private static final class GradleTaskWrapper implements CompileTaskWrapper {

        private final AbstractCompile task;

        GradleTaskWrapper(AbstractCompile task) {
            this.task = task;
        }

        @Override
        public Task getTask() {
            return task;
        }

        @Override
        public File getDestinationDir() {
            if (GradleVersion.current().compareTo(GradleVersion.version("6.1")) >= 0) {
                // AbstractCompile#getDestinationDirectory() is supported from Gradle 6.1
                // https://docs.gradle.org/6.1/javadoc/org/gradle/api/tasks/compile/AbstractCompile.html#getDestinationDirectory--
                return task.getDestinationDirectory().get().getAsFile();
            }
            return task.getDestinationDir();
        }

        @Override
        public boolean hasSource() {
            return !task.getSource().isEmpty();
        }
    }

    private static final class ReflectionTaskWrapper implements CompileTaskWrapper {

        private final Task task;
        private final Supplier<DirectoryProperty> destinationDir;
        private final Supplier<FileCollection> source;

        ReflectionTaskWrapper(Task task) {
            this.task = task;
            this.destinationDir = supplier(
                    task,
                    "getDestinationDirectory",
                    DirectoryProperty.class
            );
            this.source = supplier(
                    task,
                    "getSources",
                    FileCollection.class
            );
        }

        @Override
        public Task getTask() {
            return task;
        }

        @Override
        public File getDestinationDir() {
            return destinationDir.get().getAsFile().getOrNull();
        }

        @Override
        public boolean hasSource() {
            return !source.get().isEmpty();
        }

        private static <T> Supplier<T> supplier(Task task, String getterName, Class<T> getterReturnType) {
            final Method m = getMethod(task, getterName);

            return () -> {
                try {
                    final Object result = m.invoke(task);
                    return getterReturnType.cast(result);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof RuntimeException) {
                        throw (RuntimeException) e.getTargetException();
                    }
                    throw new RuntimeException(e.getTargetException());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to invoke " + getterName + " on " + task.getClass(), e);
                }
            };
        }

        private static Method getMethod(Task task, String name) {
            try {
                return task.getClass().getMethod(name);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Method " + name + " missing on " + task.getClass(), e);
            }
        }
    }
}
