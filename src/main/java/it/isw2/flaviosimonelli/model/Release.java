package it.isw2.flaviosimonelli.model;

import java.util.ArrayList;
import java.util.List;

public class Release {
    private String tag; //example "v1.0.0"
    private final List<Method> listMethods;

    public Release() {
        this.listMethods = new ArrayList<>(); // initialize empty ArrayList
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Method> getListMethods() {
        return listMethods;
    }

    public void insertMethods(Method m) {
        this.listMethods.add(m);
    }
}
