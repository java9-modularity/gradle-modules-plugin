package org.javamodularity.moduleplugin.extensions;

public class OptionContainer {
    private String moduleVersion;
    private final PatchModuleContainer patchModuleContainer = new PatchModuleContainer();

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public PatchModuleContainer getPatchModuleContainer() {
        return patchModuleContainer;
    }
}
