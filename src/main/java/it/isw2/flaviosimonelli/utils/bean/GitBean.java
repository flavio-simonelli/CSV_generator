package it.isw2.flaviosimonelli.utils.bean;

/**
 * Bean class representing Git repository configuration.
 * Supports both local repositories and remote repositories to be cloned.
 */
public class GitBean {
    private final RepositoryType type;
    private final String remoteUrl;
    private final String localPath;
    private final String branch;
    private final String parentDirectory;
    private final String releaseTagFormat;

    /**
     * Repository type enumeration
     */
    public enum RepositoryType {
        LOCAL,
        REMOTE
    }

    /**
     * Creates a configuration for a remote repository that will be cloned.
     *
     * @param remoteUrl        URL of the remote Git repository
     * @param parentDirectory  Directory where repository will be cloned
     * @param branch           Branch to work with
     * @param releaseTagFormat Format pattern for release tags (e.g. "v{VERSION}")
     */
    public GitBean(String remoteUrl, String parentDirectory, String branch, String releaseTagFormat) {
        this.type = RepositoryType.REMOTE;
        this.remoteUrl = remoteUrl;
        this.parentDirectory = parentDirectory;
        this.localPath = null;
        this.branch = branch;
        this.releaseTagFormat = releaseTagFormat;
    }

    /**
     * Creates a configuration for an existing local repository.
     *
     * @param localPath        Path to the local Git repository
     * @param branch           Branch to work with
     * @param releaseTagFormat Format pattern for release tags (e.g. "v{VERSION}")
     */
    public GitBean(String localPath, String branch, String releaseTagFormat) {
        this.type = RepositoryType.LOCAL;
        this.localPath = localPath;
        this.branch = branch;
        this.releaseTagFormat = releaseTagFormat;
        this.remoteUrl = null;
        this.parentDirectory = null;
    }

    public String getBranch() {
        return branch;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getParentDirectory() {
        return parentDirectory;
    }

    public String getReleaseTagFormat() {
        return releaseTagFormat;
    }

    public RepositoryType getType() {
        return type;
    }

    public boolean isLocalRepository() {
        return type == RepositoryType.LOCAL;
    }
}
