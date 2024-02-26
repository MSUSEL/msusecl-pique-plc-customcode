package runnable;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import pique.utility.PiqueProperties;
import utilities.helperFunctions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Wrapper {

    public static String[] args;

    public Wrapper(){
        handleArgs();
        loadConfig();
    }

    public void handleArgs(){
        try {
            boolean helpFlag = check_help(args);
            ArgumentParser parser = ArgumentParsers.newFor("runnable.Wrapper").build()
                    .defaultHelp(true).description("Entry point for PIQUE-CLOUD-dockerfile analysis");
            parser.addArgument("--run")
                    .setDefault("evaluate")
                    .choices("derive", "evaluate")
                    .help("derive: derives a new quality model from the benchmark repository, using --file throws an IllegalArgumentException and print the stack trace" +
                            "\n evaluate: evaluates dockerfile with derived quality model, --file must exist otherwise throw an IllegalArgumentException and print the stack trace");
            parser.addArgument( "--file")
                    .dest("fileName")
                    .type(String.class)
                    .help("path to JSON of dockerfiles to evaluate (required if run argument is evaluate)");
            parser.addArgument("--version")
                    .action(Arguments.storeTrue())
                    .setDefault(false)
                    .help("print version information and terminate program");
            parser.addArgument("--downloadNVD")
                    .action(Arguments.storeTrue())
                    .setDefault(false)
                    .help("Download the latest version of the NVD database");

            Namespace namespace = null;
            if (helpFlag) {
                System.out.println(parser.formatHelp());
                System.exit(0);
            } else {
                namespace = parser.parseArgs(args);
            }

            String runType = namespace.getString("run");
            String fileName = namespace.getString("fileName");
            boolean printVersion = namespace.getBoolean("version");
            boolean downloadNVDFlag = namespace.getBoolean("downloadNVD");
            Properties prop = PiqueProperties.getProperties();

            if (printVersion) {
                Path version = Paths.get(prop.getProperty("version"));
                System.out.println("PIQUE-CLOUD version " + version);
                System.exit(0);
            }

            if (downloadNVDFlag) {
                System.out.println("Starting NVD download");
                helperFunctions.downloadNVD();
                System.exit(0);
            }

            String nvdDictionaryPath = Paths.get(prop.getProperty("nvd-dictionary.location")).toString();
            File f = new File(nvdDictionaryPath);
            if (!f.isFile()) {
                System.out.println("Error: the National Vulnerability Database must be downloaded before deriving or evaluating. Use --help for more information.");
                System.exit(1);
            }

            if ("derive".equals(runType)) {
                if (fileName != null) {
                    throw new IllegalArgumentException("Incorrect input parameters given. Use --help for more information");
                }
                else {
                    // kick off deriver
                    new QualityModelDeriver();
                }
            }
            else if ("evaluate".equals(runType)) {
                new SingleProjectEvaluator(fileName);
            }
            else {
                throw new IllegalArgumentException("Incorrect input parameters given. Use --help for more information");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadConfig(){

    }

    public static void main(String[] args) {
        Wrapper.args = args;
        new Wrapper();

    }
    private boolean check_help(String[] args) {
        // check if the help flag was used
        boolean help = false;
        for (String arg : args) {
            if (arg.equals("--help")) {
                return true;
            }
        }
        return false;
    }
}
