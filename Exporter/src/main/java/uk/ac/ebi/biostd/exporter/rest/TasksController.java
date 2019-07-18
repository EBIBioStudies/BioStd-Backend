package uk.ac.ebi.biostd.exporter.rest;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.ftp.FtpService;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;
import uk.ac.ebi.biostd.exporter.jobs.users.UserService;

@RestController
public class TasksController {

    private final ExportPipeline fullExporter;
    private final ExportPipeline pmcExporter;
    private final PartialSubmissionExporter partialExporter;
    private final ExportPipeline statsExporter;
    private final FtpService ftpService;
    private final UserService userService;

    public TasksController(
            @Qualifier("full") ExportPipeline fullExporter,
            @Qualifier("pmc") ExportPipeline pmcExporter,
            @Qualifier("stats") ExportPipeline statsExporter,
            PartialSubmissionExporter partialExporter,
            FtpService ftpService,
            UserService userService) {
        this.fullExporter = fullExporter;
        this.pmcExporter = pmcExporter;
        this.partialExporter = partialExporter;
        this.statsExporter = statsExporter;
        this.ftpService = ftpService;
        this.userService = userService;
    }

    @GetMapping("/api/force/full")
    public String forceFull() {
        fullExporter.execute();
        return "ok";
    }

    @GetMapping("/api/force/partial")
    public String forcePartial() {
        partialExporter.execute();
        return "ok";
    }

    @GetMapping("/api/force/partial/{accNo}")
    public String forcePartial(@PathVariable String accNo) {
        partialExporter.execute(accNo);
        return "ok";
    }

    @GetMapping("/api/force/pmc")
    public String pmcExport() {
        pmcExporter.execute();
        return "ok";
    }

    @GetMapping("/api/force/stats")
    public String statsExport() {
        statsExporter.execute();
        return "ok";
    }

    @GetMapping("/api/force/ftp")
    public String executeFtp() throws IOException {
        ftpService.execute();
        return "ok";
    }

    @GetMapping("/api/force/users")
    public String executeUsers() {
        userService.execute();
        return "ok";
    }
}
