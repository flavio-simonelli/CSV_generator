package it.isw2.flaviosimonelli;

import it.isw2.flaviosimonelli.view.ViewCLI;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        // Disabilita log jgit
        ch.qos.logback.classic.Logger jgitLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.eclipse.jgit");
        jgitLogger.setLevel(ch.qos.logback.classic.Level.ERROR);

        new ViewCLI().start();
    }
}