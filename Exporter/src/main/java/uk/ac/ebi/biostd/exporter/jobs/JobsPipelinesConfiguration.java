package uk.ac.ebi.biostd.exporter.jobs;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.biostd.exporter.jobs.common.api.ExportPipeline;
import uk.ac.ebi.biostd.exporter.jobs.full.FullExport;
import uk.ac.ebi.biostd.exporter.jobs.full.FullJobJobsFactory;
import uk.ac.ebi.biostd.exporter.jobs.pmc.export.PmcExport;
import uk.ac.ebi.biostd.exporter.jobs.pmc.export.PmcJobsFactory;
import uk.ac.ebi.biostd.remote.service.RemoteService;

@Configuration
@Slf4j
public class JobsPipelinesConfiguration {


    @Bean
    @Qualifier("full")
    public ExportPipeline exportPipeline(FullExport fullExport, FullJobJobsFactory fullJobsFactory) {
        return new ExportPipeline(fullExport.getWorkers(), ImmutableList.of(fullExport), fullJobsFactory);
    }

    @Bean
    @Qualifier("pmc")
    public ExportPipeline pmcExportPipeline(PmcExport pmcExport, PmcJobsFactory jobsFactory) {
        return new ExportPipeline(pmcExport.getWorkers(), ImmutableList.of(pmcExport), jobsFactory);
    }

    @Bean
    public RemoteService remoteService(@Value("${jobs.backend-url}") String backendUrl) {
        log.info("creating remote service with url {}", backendUrl);
        return new RemoteService(backendUrl);
    }
}
