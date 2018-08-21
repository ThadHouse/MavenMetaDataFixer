package edu.wpi.first;

import edu.wpi.first.maven.MetaDataFixer;

import java.io.Console;

public class Program {
    public static void main(String[] args) {
        boolean waitForExit = true;
        String pathRoot = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--skipWaitForExit")) {
                waitForExit = false;
                continue;
            }
            if (args[i] == "--rootPath") {
                i++;
                pathRoot = args[i];
            }
        }
        if (pathRoot == null) {
            System.out.println("No path entered. Quitting");
            Console console = System.console();
            if (console != null) {
                System.out.println("Press Enter to Exit");
                console.readLine();
            }
            return;
        }
        MetaDataFixer fixer = new MetaDataFixer(pathRoot);
        fixer.updateMetaData();
        Console console = System.console();
        if (console != null && waitForExit) {
            System.out.println("Press Enter to Exit");
            console.readLine();
        }
    }
}
