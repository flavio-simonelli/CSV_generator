package it.isw2.flaviosimonelli.model;

/**
 * Class to hold method information
 */
public class Method {
    private String signature;
    private String className;
    private String path;
    private String version;

    public Method(String signature, String className, String path, String version) {
        this.signature = signature;
        this.className = className;
        this.path = path;
        this.version = version;
    }

    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }


}
