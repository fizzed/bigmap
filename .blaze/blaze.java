import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Task;
import com.fizzed.jne.HardwareArchitecture;
import com.fizzed.jne.JavaHome;
import com.fizzed.jne.JavaHomeFinder;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.exec;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public class blaze {

    private final Logger log = Contexts.logger();
    private final Config config = Contexts.config();
    private final Path projectDir = withBaseDir("../").toAbsolutePath();

    @Task(order = 1)
    public void test() throws Exception {
        final Integer jdkVersion = this.config.value("jdk.version", Integer.class).orNull();
        final HardwareArchitecture jdkArch = ofNullable(this.config.value("jdk.arch").orNull())
            .map(HardwareArchitecture::resolve)
            .orElse(null);

        final long start = System.currentTimeMillis();
        final JavaHome jdkHome = new JavaHomeFinder()
            .jdk()
            .version(jdkVersion)
            .hardwareArchitecture(jdkArch)
            .preferredDistributions()
            .sorted(jdkVersion != null || jdkArch != null)  // sort if any criteria provided
            .find();

        log.info("");
        log.info("Detected {} (in {} ms)", jdkHome, (System.currentTimeMillis()-start));
        log.info("");

        exec("mvn", "clean", "test")
            .workingDir(this.projectDir)
            .env("JAVA_HOME", jdkHome.getDirectory().toString())
            .verbose()
            .run();
    }

    @Task(order = 2)
    public void test_all_jdks() throws Exception {
        // collect and find all the jdks we will test on
        final List<JavaHome> jdks = new ArrayList<>();
        for (int jdkVersion : asList(21, 17, 11, 8)) {
            jdks.add(new JavaHomeFinder()
                .jdk()
                .version(jdkVersion)
                .preferredDistributions()
                .sorted()
                .find());
        }

        log.info("Detected JDKs:");
        jdks.forEach(jdk -> log.info("  {}", jdk));

        for (JavaHome jdk : jdks) {
            try {
                log.info("");
                log.info("Using JDK {}", jdk);
                log.info("");

                exec("mvn", "clean", "test")
                    .workingDir(this.projectDir)
                    .env("JAVA_HOME", jdk.getDirectory().toString())
                    .verbose()
                    .run();
            } catch (Exception e) {
                log.error("");
                log.error("Failed on JDK " + jdk);
                log.error("");
                throw e;
            }
        }

        log.info("Success on JDKs:");
        jdks.forEach(jdk -> log.info("  {}", jdk));
    }

}