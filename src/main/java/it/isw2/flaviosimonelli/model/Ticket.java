package it.isw2.flaviosimonelli.model;

import java.util.Date;
import java.util.List;

    public class Ticket {
        private Version fixVersion; // version in which the issue is fixed
        private Version affectedVersion; // first version in which the issue is present

        public void setFixVersion(Version fixVersion) {
            this.fixVersion = fixVersion;
        }
        public Version getFixVersion() {
            return fixVersion;
        }

        public void setAffectedVersion(Version affectedVersion) {
            this.affectedVersion = affectedVersion;
        }
        public Version getAffectedVersion() {
            return affectedVersion;
        }


    }