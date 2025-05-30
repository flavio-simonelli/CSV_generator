package it.isw2.flaviosimonelli.model;

import java.util.List;
import it.isw2.flaviosimonelli.model.method.Method;

public class Version {
    private String name; // e.g., "1.0.0"
    private boolean released; // true if the version is released, false otherwise
    private String hashCommit; // hash of the tag associated with this version
    private List<Method> methods; // list of methods associated with this version

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }
    public boolean isReleased() {
        return released;
    }

    public void setHashCommit(String hash) {
        this.hashCommit = hash;
    }
    public String getHashCommit() {
        return hashCommit;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
    public List<Method> getMethods() {
        return methods;
    }
}
