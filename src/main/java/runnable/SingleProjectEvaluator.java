package runnable;

import lombok.Getter;
import lombok.Setter;
import model.PLCQualityModelImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.evaluation.Project;
import pique.model.Diagnostic;
import pique.model.QualityModel;
import pique.model.QualityModelImport;
import pique.runnable.ASingleProjectEvaluator;
import pique.utility.PiqueProperties;
import tool.CODESYSWrapper;
import utilities.helperFunctions;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleProjectEvaluator extends ASingleProjectEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleProjectEvaluator.class);

    //default properties location
    @Getter
    @Setter
    private String propertiesLocation = "src/main/resources/pique-properties.properties";

    public SingleProjectEvaluator(String projectsToAnalyze) {
        init(projectsToAnalyze);
    }

    public void init(String projectsToAnalyze) {
        LOGGER.info("Starting Analysis");
        Properties prop = null;
        try {
            prop = propertiesLocation == null ? PiqueProperties.getProperties() : PiqueProperties.getProperties(propertiesLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path resultsDir = Paths.get(prop.getProperty("results.directory"));

        Path qmLocation = Paths.get(prop.getProperty("derived.qm"));

        ITool CODESYSWrapper = new CODESYSWrapper();
        Set<ITool> tools = Stream.of(CODESYSWrapper).collect(Collectors.toSet());

        Set<Path> projectRoots = new HashSet<>();

        File[] subDirectories = Paths.get(projectsToAnalyze).toFile().listFiles();
        for (File file : subDirectories) {
            projectRoots.add(file.toPath());
            System.out.println("Queuing directory: " + file + " for analysis");
            LOGGER.info("Queuing directory: " + file + " for analysis");
        }

        for (Path plcProject : projectRoots) {
            Path outputPath = runEvaluator(plcProject, resultsDir, qmLocation, tools).getParent();
            try {
                //create output directory if not exist
                Files.createDirectories(outputPath);
            } catch (IOException e) {
                System.out.println("Could not create output directory for compact file");
                throw new RuntimeException(e);
            }
            LOGGER.info("output directory: " + outputPath.getFileName());
            System.out.println("Output directory (known bug with the 'input/' directory being included in the output path): " + resultsDir.getFileName() + "/" + plcProject.getParent());
            System.out.println("Exporting compact output file for use with the pique visualizer : " + project.exportToJson(resultsDir, true));
        }

    }

    @Override
    public Path runEvaluator(Path projectDir, Path resultsDir, Path qmLocation, Set<ITool> tools){
        // Initialize data structures
        PLCQualityModelImport qmImport = new PLCQualityModelImport(qmLocation);
        QualityModel qualityModel = qmImport.importQualityModel();
        project = new Project(projectDir.toString(), projectDir, qualityModel);

        // Validate State
        // TODO: validate more objects such as if the quality model has thresholds and weights, are there expected diagnostics, etc
        validatePreEvaluationState(project);

        // Run the static analysis tools process
        Map<String, Diagnostic> allDiagnostics = new HashMap<>();
        tools.forEach(tool -> {
            allDiagnostics.putAll(runTool(projectDir, tool));
        });

        // Apply tool results to Project object
        project.updateDiagnosticsWithFindings(allDiagnostics);

        BigDecimal tqiValue = project.evaluateTqi();

        // Create a file of the results and return its path
        return project.exportToJson(resultsDir);
    }

    //region Get / Set
    public Project getEvaluatedProject() {
        return project;
    }


}
