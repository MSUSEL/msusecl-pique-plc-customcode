package calibration;

import org.checkerframework.checker.units.qual.A;
import pique.analysis.ITool;
import pique.calibration.AbstractBenchmarker;
import pique.calibration.IBenchmarker;
import pique.calibration.NaiveBenchmarker;
import pique.evaluation.Project;
import pique.model.QualityModel;
import pique.utility.FileUtility;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;

public class PLCBenchmarker extends NaiveBenchmarker {

    public Set<Path> CollectProjectPaths(Path benchmarkRepository, String projectRootFlag) {
        Set<Path> projectRoots = new HashSet<>();

        File[] subDirectories = benchmarkRepository.toFile().listFiles();
        for (File file : subDirectories) {
            projectRoots.add(file.toPath());
        }
        return projectRoots;
    }
}
