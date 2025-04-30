package it.isw2.flaviosimonelli.model;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    private final String url;
    private final String localpath;
    private final List<Release> listReleases;

    public Repository(String url, String localpath) {
        this.url = url;
        this.localpath = localpath;
        this.listReleases = new ArrayList<>(); // initialize empty list
    }

    public String getUrl() {
        return url;
    }

    public List<Release> getListReleases() {
        return listReleases;
    }

    public void insertRelease(Release r) {
        this.listReleases.add(r);
    }

    public String getLocalPath() {
        return localpath;
    }
}
