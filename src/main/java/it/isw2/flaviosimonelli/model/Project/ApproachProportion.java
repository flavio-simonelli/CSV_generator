package it.isw2.flaviosimonelli.model.Project;

import it.isw2.flaviosimonelli.view.ViewCLI;

/*
* Enum attribute of Project class that represents the approach choice for the proportion applied to the ticket in a project
 */
public enum ApproachProportion {
    COMPLETE,
    CENTRAL_SLIDING_WINDOW;

    public static ApproachProportion fromString(String value) {
        if (value == null) return COMPLETE;
        switch (value.toUpperCase()) {
            case "CENTRAL_SLIDING_WINDOW":
                return CENTRAL_SLIDING_WINDOW;
            case "COMPLETE":
            default:
                return COMPLETE;
        }
    }

}
