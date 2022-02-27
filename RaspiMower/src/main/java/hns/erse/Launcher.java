package hns.erse;

import hns.erse.application.MowerDemo;
import picocli.CommandLine;

@CommandLine.Command(name = "CrowPi Example Launcher", version = "1.0.0", mixinStandardHelpOptions = true)
public final class Launcher  {
    /**
     * This list must contain all applications which should be executable through the launcher.
     * Each class instance must implement the Application interface and gets automatically added as a subcommand.
     */


    /**
     * Main application entry point which executes the launcher and exits afterwards.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        MowerDemo.showDemo();
    }
}
