package examples

import groovy.transform.CompileStatic

@CompileStatic
class MinorVersion {
    static final int value = 14

    static String getMinorAndPatchValue() {
        "${value}.${PatchVersion.getValue()}"
    }
}
