package it.isw2.flaviosimonelli.model;

import java.util.Date;
import java.util.List;

    public class Ticket {
        private String id;
        private String key;
        private String url;

        private List<String> fixVersion;
        private List<String> affectedVersion;


        private String priorityName;

        private String assignee;
        private String creator;
        private String reporter;


        private String summary;
        private String description;

        private Date created;
        private Date updated;
        private Date resolutionDate;

        private int votes;
        private int watchers;

        // Getter e Setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<String> getFixVersion() {
            return fixVersion;
        }

        public void setFixVersion(List<String> fixVersion) {
            this.fixVersion = fixVersion;
        }


        public String getPriorityName() {
            return priorityName;
        }

        public void setPriorityName(String priorityName) {
            this.priorityName = priorityName;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getReporter() {
            return reporter;
        }

        public void setReporter(String reporter) {
            this.reporter = reporter;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }

        public Date getUpdated() {
            return updated;
        }

        public void setUpdated(Date updated) {
            this.updated = updated;
        }

        public Date getResolutionDate() {
            return resolutionDate;
        }

        public void setResolutionDate(Date resolutionDate) {
            this.resolutionDate = resolutionDate;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }

        public int getWatchers() {
            return watchers;
        }

        public void setWatchers(int watchers) {
            this.watchers = watchers;
        }

        public List<String> getAffectedVersion() {
            return affectedVersion;
        }

        public void setAffectedVersion(List<String> affectedVersion) {
            this.affectedVersion = affectedVersion;
        }
    }