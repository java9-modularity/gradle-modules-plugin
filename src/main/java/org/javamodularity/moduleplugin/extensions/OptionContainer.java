package org.javamodularity.moduleplugin.extensions;

public class OptionContainer {
    private String moduleVersion;
    private final PatchModuleContainer patchModuleContainer = new PatchModuleContainer();

    private boolean effectiveArgumentsAdjustmentEnabled = true;

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public PatchModuleContainer getPatchModuleContainer() {
        return patchModuleContainer;
    }

    public boolean isEffectiveArgumentsAdjustmentEnabled() {
        return effectiveArgumentsAdjustmentEnabled;
    }

    public void setEffectiveArgumentsAdjustmentEnabled(boolean effectiveArgumentsAdjustmentEnabled) {
        this.effectiveArgumentsAdjustmentEnabled = effectiveArgumentsAdjustmentEnabled;
    }
}
