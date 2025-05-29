package it.isw2.flaviosimonelli.model;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Class to hold method information
 */
public class Method {
    private String name;
    private String className;
    private String path;
    private String version;

    public Method(String name, String className, String path, String version) {
        this.name = name;
        this.className = className;
        this.path = path;
        this.version = version;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
