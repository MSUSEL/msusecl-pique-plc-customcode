package runnable;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pique.analysis.ITool;
import pique.evaluation.Project;
import pique.model.Diagnostic;
import pique.model.QualityModel;
import pique.model.QualityModelImport;
import pique.runnable.ASingleProjectEvaluator;
import pique.utility.PiqueProperties;
import tool.GrypeWrapper;
import tool.TrivyWrapper;
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

        //projectLocation is a json file, need to parse. 
        Path dockerfileJSONPath = Paths.get(projectsToAnalyze);

        Set<Path> dockerfilesToAnalyze = helperFunctions.getDockerImagesToAnalyze(dockerfileJSONPath);


        Path projectsRepo = Paths.get(prop.getProperty("project.root"));

        Path resultsDir = Paths.get(prop.getProperty("results.directory"));

        LOGGER.info("Projects to analyze from file: " + dockerfileJSONPath.toString());
        System.out.println("Projects to analyze from file: " + dockerfileJSONPath.toString());
        for (Path p : dockerfilesToAnalyze) {
            LOGGER.info("\t" + p.toString());
            System.out.println("\t" + p.toString());
        }

        Path qmLocation = Paths.get(prop.getProperty("derived.qm"));

        ITool gyrpeWrapper = new GrypeWrapper(prop.getProperty("github-token-path"), prop.getProperty("nvd-api-key-path"));
        ITool trivyWrapper = new TrivyWrapper(prop.getProperty("github-token-path"));
        Set<ITool> tools = Stream.of(gyrpeWrapper, trivyWrapper).collect(Collectors.toSet());

        for (Path dockerfile : dockerfilesToAnalyze) {
            //tricky here. getParent  because the Path points to a json file, and we need the parent.
            Path outputPath = runEvaluator(dockerfile, resultsDir, qmLocation, tools).getParent();

            try {
                //create output directory if not exist
                Files.createDirectories(outputPath);
            } catch (IOException e) {
                System.out.println("Could not create output directory");
                throw new RuntimeException(e);
            }
            LOGGER.info("output: " + outputPath.getFileName());
            System.out.println("output: " + outputPath.getFileName());
            System.out.println("exporting compact: " + project.exportToJson(resultsDir, true));
        }
    }

    @Override
    public Path runEvaluator(Path projectDir, Path resultsDir, Path qmLocation, Set<ITool> tools){
        // Initialize data structures
        QualityModelImport qmImport = new QualityModelImport(qmLocation);
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
