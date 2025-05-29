package it.isw2.flaviosimonelli.utils.bean;

public class GitBean {
    private String URL;
    private String directory;
    private final String branch;
    private String parentDirectory;
    private String conventionReleaseTag;

    public GitBean(String URL, String parentDirectory, String branch, String conventionReleaseTag) {
        this.URL = URL;
        this.parentDirectory = parentDirectory;
        this.branch = branch;
        this.conventionReleaseTag = conventionReleaseTag;
    }
    public GitBean(String directory, String branch, String conventionReleaseTag) {
        this.directory = directory;
        this.branch = branch;
        this.conventionReleaseTag = conventionReleaseTag;
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
    public String getParentDirectory() {return parentDirectory;}
    public String getConventionReleaseTag() {
        return conventionReleaseTag;
    }

}
