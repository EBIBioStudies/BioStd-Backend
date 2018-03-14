package uk.ac.ebi.biostd.webapp.server.config;

import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.HibernateDBConnectionURLParameter;
import static uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager.HibernateSearchIndexDirParameter;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DefaultConfiguration {

    static void loadDefaults(ConfigBean cfgBean) {
        cfgBean.setDatabaseConfig(setupDbConfig());

        cfgBean.setActivationTimeout(BackendConfig.defaultActivationTimeout);
        cfgBean.setPassResetTimeout(BackendConfig.defaultPassResetTimeout);
        cfgBean.setDefaultSubmissionAccPrefix(BackendConfig.DefaultSubmissionPrefix);
        cfgBean.setWebConfigEnabled(true);
        cfgBean.setCreateFileStructure(true);
        cfgBean.setFileLinkAllowed(true);
        cfgBean.setWorkDirectory(Paths.get("work"));
        cfgBean.setSubmissionsPath(Paths.get("submission"));
        cfgBean.setSubmissionsHistoryPath(Paths.get("history"));
        cfgBean.setSubmissionsTransactionPath(Paths.get("transaction"));
        cfgBean.setSubmissionUpdatePath(Paths.get("updates"));
        cfgBean.setUserGroupIndexPath(Paths.get("ug_index"));
        cfgBean.setUserGroupDropboxPath(Paths.get("ug_data"));
        cfgBean.setPublicDropboxes(false);
        cfgBean.setEnableUnsafeRequests(false);
        cfgBean.setUpdateWaitPeriod(10);
        cfgBean.setMaxUpdatesPerFile(50);
        cfgBean.setMandatoryAccountActivation(false);
        cfgBean.setDefaultSubmissionAccPrefix("S-");

        // Sso Settings
        cfgBean.setSsoPemURL("https://explore.api.aap.tsi.ebi.ac.uk/meta/public.pem");
        cfgBean.setSsoDerURL("https://explore.api.aap.tsi.ebi.ac.uk/meta/public.der");
        cfgBean.setSsoAuthURL("https://explore.api.aap.tsi.ebi.ac.uk/auth");
        cfgBean.setFrontendUpdateFormat("xml");
    }

    private static Map<String, Object> setupDbConfig() {
        Map<String, Object> dbConf = new HashMap<>();
        dbConf.put("hibernate.connection.driver_class", "org.h2.Driver");
        dbConf.put("hibernate.connection.username", "");
        dbConf.put("hibernate.connection.password", "");
        dbConf.put("hibernate.cache.use_query_cache", "false");
        dbConf.put("hibernate.ejb.discard_pc_on_close", "true");
        dbConf.put(HibernateDBConnectionURLParameter, "jdbc:h2:db/appdb;IFEXISTS=FALSE");
        dbConf.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        dbConf.put("hibernate.hbm2ddl.auto", "update");
        dbConf.put("hibernate.c3p0.max_size", "30");
        dbConf.put("hibernate.c3p0.min_size", "0");
        dbConf.put("hibernate.c3p0.timeout", "5000");
        dbConf.put("hibernate.c3p0.max_statements", "0");
        dbConf.put("hibernate.c3p0.idle_test_period", "300");
        dbConf.put("hibernate.c3p0.acquire_increment", "2");
        dbConf.put("hibernate.c3p0.unreturnedConnectionTimeout", "18000");
        dbConf.put(HibernateSearchIndexDirParameter, "index");
        dbConf.put("hibernate.search.default.directory_provider", "filesystem");
        dbConf.put("hibernate.search.lucene_version", "LUCENE_54");
        return dbConf;
    }
}
