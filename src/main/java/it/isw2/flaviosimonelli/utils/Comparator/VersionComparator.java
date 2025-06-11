package it.isw2.flaviosimonelli.utils.Comparator;

import it.isw2.flaviosimonelli.model.Version;

import java.util.Comparator;

public class VersionComparator implements Comparator<Version> {

    @Override
    public int compare(Version v1, Version v2) {
        NameVersionComparator comparator = new NameVersionComparator();
        return comparator.compare(v1.getName(), v2.getName());
    }
}