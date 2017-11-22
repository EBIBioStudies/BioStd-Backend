package uk.ac.ebi.biostd.webapp.server;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationException;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationManager;


/**
 * Application Lifecycle Listener implementation class WebAppInit
 */
@Slf4j
@WebListener
@AllArgsConstructor
public class WebAppInit implements ServletContextListener {

    private final ConfigurationManager configurationManager;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        if (BackendConfig.isConfigValid()) {
            BackendConfig.getConfigurationManager().stopServices();
        }

        if ("true".equals(BackendConfig.getDatabaseConfig().get(ConfigurationManager.IsEmbeddedH2Parameter))) {

            Connection conn;
            try {
                conn = DriverManager.getConnection(
                        BackendConfig.getDatabaseConfig().get(ConfigurationManager.HibernateDBConnectionURLParameter)
                                .toString(), "", "");

                Statement stat = conn.createStatement();
                stat.execute("SHUTDOWN");
                stat.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        try {
            Driver h2Drv = DriverManager.getDriver("jdbc:h2:xxx");
            if (h2Drv != null) {
                DriverManager.deregisterDriver(h2Drv);
            }
        } catch (SQLException e) {
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent ctxEv) {
        try {
            contextInitializedUnsafe(ctxEv);
        } catch (ConfigurationException ec) {
            log.error("Configuration is not valid: " + ec.getMessage());
            BackendConfig.setConfigValid(false);
        } catch (Throwable e) {
            log.error("Configuration is not valid: " + e.getMessage(), e);
            BackendConfig.setConfigValid(false);
        }
    }

    private void contextInitializedUnsafe(ServletContextEvent servletContextEvent) throws ConfigurationException {
        ServletContext ctx = servletContextEvent.getServletContext();
        BackendConfig.setInstanceId(ctx.getContextPath().hashCode());
        BackendConfig.setConfigurationManager(configurationManager);
        BackendConfig.getConfigurationManager().loadConfiguration();
        BackendConfig.setConfigValid(true);
    }

}
