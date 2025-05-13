package it.isw2.flaviosimonelli.model;

import java.time.LocalDate;

public class Version {
    private String id;
    private String name;
    private boolean archived;
    private boolean released;
    private LocalDate releaseDate;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isArchived() {
        return archived;
    }
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isReleased() {
        return released;
    }
    public void setReleased(boolean released) {
        this.released = released;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}
