package uk.ac.ebi.biostd.exporter.rest;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.partial.PartialSubmissionExporter;
import uk.ac.ebi.biostd.exporter.jobs.pmc.importer.PmcImporter;

@RestController
public class TasksController {

    private final ExportPipeline fullExporter;
    private final ExportPipeline pmcExporter;
    private final PartialSubmissionExporter partialExporter;
    private final PmcImporter pmcImporter;
    private final ExportPipeline statsExporter;

    public TasksController(
            @Qualifier("full") ExportPipeline fullExporter,
            @Qualifier("pmc") ExportPipeline pmcExporter,
            @Qualifier("stats") ExportPipeline statsExporter,
            PartialSubmissionExporter partialExporter,
            PmcImporter pmcImporter) {
        this.fullExporter = fullExporter;
        this.pmcExporter = pmcExporter;
        this.partialExporter = partialExporter;
        this.pmcImporter = pmcImporter;
        this.statsExporter = statsExporter;
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

    @GetMapping("/api/force/pmc")
    public String pmcExport() {
        pmcExporter.execute();
        return "ok";
    }

    @GetMapping("/api/force/pmc-import")
    public String pmcImporter() {
        pmcImporter.execute();
        return "ok";
    }

    @GetMapping("/api/force/stats")
    public String statsExport() {
        statsExporter.execute();
        return "ok";
    }
}
