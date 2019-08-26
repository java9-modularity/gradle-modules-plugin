package org.javamodularity.moduleplugin;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class SmokeTestAppContext {
    private final Path binDirPath;
    private final Map<String,String> env;
    private final List<String> args;

    private SmokeTestAppContext(Path binDirPath, Map<String, String> env, List<String> args) {
        this.binDirPath = binDirPath;
        this.env = env;
        this.args = args;
    }

    static SmokeTestAppContext ofDefault(Path binDirPath) {
        return new SmokeTestAppContext(binDirPath, Map.of(), List.of());
    }

    static SmokeTestAppContext ofAliceAndBobAtHome(Path binDirPath) {
        return new SmokeTestAppContext(
                binDirPath,
                Map.of("JAVA_OPTS", "-Dgreeting.addition=home"),
                List.of("Alice", "Bob")
        );
    }

    String getAppOutput(String appName) {
        try {
            Process process = buildProcess(appName);
            process.waitFor(30, TimeUnit.SECONDS);
            if (process.exitValue() != 0) {
                String errText = getText(process.getErrorStream());
                System.err.println("Process terminated with exit code " + process.exitValue() + ": " + errText);
                return errText;
            }
            return getText(process.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    private Process buildProcess(String appName) throws IOException {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
        String scriptName = windows ? (appName + ".bat") : appName;

        File binDir = binDirPath.toFile().getAbsoluteFile();
        List<String> cmd = new ArrayList<>();
        cmd.add(new File(binDir, scriptName).getPath());
        cmd.addAll(args);
        ProcessBuilder processBuilder = new ProcessBuilder()
                .directory(binDir)
                .command(cmd);
        processBuilder.environment().putAll(env);
        return processBuilder.start();
    }

    private static String getText(InputStream inputStream) {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
