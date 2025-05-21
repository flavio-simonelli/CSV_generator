package it.isw2.flaviosimonelli.model;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Class to hold method information
 */
public class Method {
    private String packageName;
    private String className;
    private String methodName;
    private List<Parameter> parameters;

    // getter e setter
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

}
