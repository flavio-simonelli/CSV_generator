package it.isw2.flaviosimonelli.utils.bean;

public class VersionManagerBean {
    private String URL;
    private final String directory;
    private String branch;
    private String choice;

    public VersionManagerBean(String URL, String directory, String branch, String choice) {
        this.URL = URL;
        this.directory = directory;
        this.branch = branch;
        this.choice = choice;
    }
    public VersionManagerBean(String directory, String choice) {
        this.directory = directory;
        this.choice = choice;
    }

    public String getChoiceCloneOpen() {
        return choice;
    }
    public String getBranch() {
        return branch;
    }
    public String getURL() {
        return URL;
    }
    public String getDirectory() {
        return directory;
    }

}
