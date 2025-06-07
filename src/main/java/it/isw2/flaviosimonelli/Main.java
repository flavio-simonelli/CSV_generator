package it.isw2.flaviosimonelli;

import it.isw2.flaviosimonelli.view.ViewCLI;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        System.out.println("SLF4J implementation: " + LoggerFactory.getILoggerFactory().getClass().getName());

        new ViewCLI().start();
    }
}