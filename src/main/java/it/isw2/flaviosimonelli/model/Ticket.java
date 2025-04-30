package it.isw2.flaviosimonelli.model;

public class Ticket {
    private String key;
    private String summary;
    private String status;
    private String resolution;

    public Ticket(String key, String summary, String status, String resolution) {
        this.key = key;
        this.summary = summary;
        this.status = status;
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return String.format("✔️ [%s] %s (%s / %s)", key, summary, status, resolution);
    }
}
