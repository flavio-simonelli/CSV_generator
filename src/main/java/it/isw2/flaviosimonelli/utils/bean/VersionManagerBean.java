package it.isw2.flaviosimonelli.utils.bean;

public class VersionManagerBean {
    private String URL;
    private final String directory;
    private String branch;
    private final int choiceCloneOpen;

    public VersionManagerBean(String URL, String directory, String branch) {
        this.URL = URL;
        this.directory = directory;
        this.branch = branch;
        this.choiceCloneOpen = 1;
    }
    public VersionManagerBean(String directory) {
        this.directory = directory;
        this.choiceCloneOpen = 0;
    }

    public int getChoiceCloneOpen() {
        return choiceCloneOpen;
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
