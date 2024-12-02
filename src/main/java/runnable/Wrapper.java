/**
 * MIT License
 *
 * Copyright (c) 2024 Montana State University Software Engineering Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package runnable;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import pique.utility.PiqueProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Wrapper {

    public static String[] args;

    public Wrapper(){
        Path projectsToAnalyze = loadConfig();
        handleArgs(projectsToAnalyze);
    }

    public void handleArgs(Path projectsToAnalyze){
        try {
            boolean helpFlag = check_help(args);
            ArgumentParser parser = ArgumentParsers.newFor("runnable.Wrapper").build()
                    .defaultHelp(true).description("Entry point for PIQUE-PLC-CUSTOMCODE analysis");
            parser.addArgument("--run")
                    .setDefault("evaluate")
                    .choices("derive", "evaluate")
                    .help("derive: derives a new quality model from the benchmark repository, using --file throws an IllegalArgumentException and print the stack trace" +
                            "\n evaluate: evaluates output from two CODESYS output analysis files (a rules file and a metrics file)" +
                            " with derived quality model, --file must exist otherwise throw an IllegalArgumentException and print the stack trace");
            parser.addArgument("--version")
                    .action(Arguments.storeTrue())
                    .setDefault(false)
                    .help("print version information and terminate program");

            Namespace namespace = null;
            if (helpFlag) {
                System.out.println(parser.formatHelp());
                System.exit(0);
            } else {
                namespace = parser.parseArgs(args);
            }

            String runType = namespace.getString("run");
            boolean printVersion = namespace.getBoolean("version");
            Properties prop = PiqueProperties.getProperties();

            if (printVersion) {
                Path version = Paths.get(prop.getProperty("version"));
                System.out.println("PIQUE-PLC-CUSTOMCODE version " + version);
                System.exit(0);
            }

            if ("derive".equals(runType)) {
                // kick off deriver
                new QualityModelDeriver();
            }
            else if ("evaluate".equals(runType)) {
                new SingleProjectEvaluator(projectsToAnalyze.toString());
            }
            else {
                throw new IllegalArgumentException("Incorrect input parameters given. Use --help for more information");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Path loadConfig(){
        //need to read in project.root
        Properties prop = null;
        String defaultPropertiesLocation = "src/main/resources/pique-properties.properties";
        try {
            prop = defaultPropertiesLocation == null ? PiqueProperties.getProperties() : PiqueProperties.getProperties(defaultPropertiesLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path projectRoot = Paths.get(prop.getProperty("project.root"));
        return projectRoot;
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
