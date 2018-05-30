package uk.ac.ebi.biostd.webapp.server.config;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import javax.persistence.EntityManagerFactory;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.biostd.webapp.server.export.TaskConfig;
import uk.ac.ebi.biostd.webapp.server.export.TaskInfo;
import uk.ac.ebi.biostd.webapp.server.mng.ServiceManager;

@Getter
@Setter
public class ConfigBean {

    private String dataMountPath;
    private String recaptchaPublicKey;
    private String recaptchaPrivateKey;

    private ServiceManager defaultServiceManager;
    private EntityManagerFactory emf;
    private TaskInfo expTaskInfo;

    private boolean createFileStructure = false;

    private Path baseDirectory;
    private Path workDirectory;

    private Path userGroupDropboxPath;

    private Path userGroupIndexPath;
    private Path usersIndexPath;
    private Path groupsIndexPath;

    private Path submissionsPath;
    private Path submissionsHistoryPath;
    private Path submissionsTransactionPath;
    private Path submissionUpdatePath;

    private Path publicFTPPath;

    private String updateListenerURLPfx;
    private String updateListenerURLSfx;

    private String defaultSubmissionAccPrefix = null;
    private String defaultSubmissionAccSuffix = null;
    private String frontendUpdateFormat = null;

    private int updateWaitPeriod = 5;
    private int maxUpdatesPerFile = 50;

    private boolean fileLinkAllowed = true;

    private boolean publicDropboxes = false;

    private boolean enableUnsafeRequests = true;
    private boolean mandatoryAccountActivation = true;

    private boolean searchEnabled = false;

    private boolean webConfigEnabled = false;


    private long activationTimeout;
    private long passResetTimeout;

    private Map<String, Object> databaseConfig;
    private Map<String, Object> emailConfig;
    private TaskConfig taskConfig;
    private Timer timer;
    private String uiURL;

    // SSO support
    private String ssoPemURL;
    private String ssoDerURL;
    private String ssoAuthURL;

    public Map<String, Object> getDatabaseConfig() {
        return defaultIfNull(databaseConfig, new HashMap<String, Object>());
    }

    public Map<String, Object> getEmailConfig() {
        return defaultIfNull(emailConfig, new HashMap<String, Object>());
    }
}
