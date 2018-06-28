package uk.ebi.biostd.ftp;

import com.google.common.base.Preconditions;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Slf4j
public class FtpApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FtpApplication.class).run(args);
    }

    @Autowired
    private ConfigProperties configProperties;

    @PostConstruct
    public void startFtp() throws FtpException {
        Preconditions.checkNotNull(configProperties.getPath());
        Preconditions.checkNotNull(configProperties.getPort());
        log.info("Running ftp server for path: '{}' in port {}", configProperties.getPath(),
                configProperties.getPort());

        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.addListener("default", configureListener());
        serverFactory.setConnectionConfig(configureConnection());
        serverFactory.getUserManager().save(configureUser());
        serverFactory.createServer().start();
    }

    private User configureUser() {
        BaseUser user = new BaseUser();
        user.setName("anonymous");
        user.setHomeDirectory(configProperties.getPath());
        return user;
    }

    private ConnectionConfig configureConnection() {
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);
        return connectionConfigFactory.createConnectionConfig();
    }

    private Listener configureListener() {
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(configProperties.getPort());
        return factory.createListener();
    }

    @Component
    @Getter
    @Setter
    @ConfigurationProperties(prefix = "ftp")
    public class ConfigProperties {

        private String path;
        private Integer port;
    }
}
