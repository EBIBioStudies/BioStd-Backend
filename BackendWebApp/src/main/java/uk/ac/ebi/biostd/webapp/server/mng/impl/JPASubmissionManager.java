package uk.ac.ebi.biostd.webapp.server.mng.impl;

import com.google.common.base.Strings;
import com.pri.util.AccNoUtil;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.Tag;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.in.AccessionMapping;
import uk.ac.ebi.biostd.in.ElementPointer;
import uk.ac.ebi.biostd.in.PMDoc;
import uk.ac.ebi.biostd.in.ParserConfig;
import uk.ac.ebi.biostd.in.SubmissionMapping;
import uk.ac.ebi.biostd.in.pagetab.FileOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SectionOccurrence;
import uk.ac.ebi.biostd.in.pagetab.SubmissionInfo;
import uk.ac.ebi.biostd.model.Section;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.model.SubmissionAttributeException;
import uk.ac.ebi.biostd.model.SubmissionTagRef;
import uk.ac.ebi.biostd.out.cell.CellFormatter;
import uk.ac.ebi.biostd.out.json.JSONFormatter;
import uk.ac.ebi.biostd.out.pageml.PageMLFormatter;
import uk.ac.ebi.biostd.treelog.ErrorCounter;
import uk.ac.ebi.biostd.treelog.ErrorCounterImpl;
import uk.ac.ebi.biostd.treelog.LogNode;
import uk.ac.ebi.biostd.treelog.LogNode.Level;
import uk.ac.ebi.biostd.treelog.SimpleLogNode;
import uk.ac.ebi.biostd.treelog.SubmissionReport;
import uk.ac.ebi.biostd.util.DataFormat;
import uk.ac.ebi.biostd.util.FilePointer;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.dto.EUToxRiskFileValidationError;
import uk.ac.ebi.biostd.webapp.application.validation.eutoxrisk.services.EUToxRiskFileValidatorService;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.AccessionManager;
import uk.ac.ebi.biostd.webapp.server.mng.FileManager;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionManager;
import uk.ac.ebi.biostd.webapp.server.mng.impl.AccNoMatcher.Match;
import uk.ac.ebi.biostd.webapp.server.shared.tags.TagRef;
import uk.ac.ebi.biostd.webapp.server.util.DatabaseUtil;
import uk.ac.ebi.biostd.webapp.server.util.ExceptionUtil;
import uk.ac.ebi.biostd.webapp.server.vfs.InvalidPathException;
import uk.ac.ebi.biostd.webapp.server.vfs.PathInfo;
import uk.ac.ebi.mg.spreadsheet.cell.XSVCellStream;

@Slf4j
public class JPASubmissionManager implements SubmissionManager {

    private enum SubmissionDirState {
        ABSENT,
        LINKED,
        COPIED,
        HOME
    }

    private static class FileTransactionUnit {

        Path submissionPath;
        Path historyPath;
        Path submissionPathTmp;
        Path historyPathTmp;
        SubmissionDirState state;
    }

    static class LockInfo {

        String lockOwner;
        Set<String> waiters;
    }

    private final Map<String, LockInfo> lockedSmbIds = new HashMap<>();
    private final Set<String> lockedSecIds = new HashSet<>();

    private final PTDocumentParser parser;

    private final EntityManagerFactory emf;
    private final EUToxRiskFileValidatorService eutoxriskFileValidator;

    public JPASubmissionManager(EntityManagerFactory emf, EUToxRiskFileValidatorService eutoxriskFileValidator) {

        ParserConfig parserCfg = new ParserConfig();
        parserCfg.setMultipleSubmissions(true);
        parserCfg.setPreserveId(false);
        parser = new PTDocumentParser(parserCfg);
        this.emf = emf;
        this.eutoxriskFileValidator = eutoxriskFileValidator;
    }

    @Override
    public LogNode deleteSubmissionByAccession(String acc, boolean toHistory, User usr) {
        ErrorCounter ec = new ErrorCounterImpl();
        SimpleLogNode gln = new SimpleLogNode(Level.SUCCESS,
                (toHistory ? "Deleting" : "Removing") + " submission '" + acc + "'", ec);

        EntityManager em = emf.createEntityManager();

        FileManager fileMngr = BackendConfig.getServiceManager().getFileManager();

        Path origDir = null;
        Path histDir = null;
        Path histDirTmp = null;

        SubmissionDirState dirOp = SubmissionDirState.ABSENT; // 0 - not changed, 1 - moved, 2 - copied, 3 = error

        boolean trnOk = true;

        Submission sbm = null;

        EntityTransaction trn = null;

        try {
            trn = em.getTransaction();

            trn.begin();

            Query q = em.createNamedQuery(Submission.GetByAccQuery);

            q.setParameter("accNo", acc);

            try {
                sbm = (Submission) q.getSingleResult();
            } catch (NoResultException e) {
            }

            if (sbm == null) {
                gln.log(Level.ERROR, "Submission not found");
                return gln;
            }

            if (!BackendConfig.getServiceManager().getSecurityManager().mayUserDeleteSubmission(sbm, usr)) {
                gln.log(Level.ERROR, "User has no permission to delete this submission");
                return gln;
            }

            origDir = BackendConfig.getSubmissionPath(sbm);

            histDir = BackendConfig.getSubmissionHistoryPath(sbm);

            if (toHistory && Files.exists(origDir)) {
                histDirTmp = histDir.resolveSibling(histDir.getFileName() + "#tmp");

                try {
                    fileMngr.moveDirectory(origDir,
                            histDirTmp); // trying to move submission directory to the history dir

                    try {
                        Files.createSymbolicLink(origDir,
                                histDirTmp); //to provide access to the submission before the commit
                        dirOp = SubmissionDirState.LINKED;
                    } catch (Exception ex2) {
                        fileMngr.moveDirectory(histDirTmp,
                                origDir); //if we can't make a symbolic link (FAT?) let's return the directory back
                        dirOp = SubmissionDirState.HOME;
                    }
                } catch (Exception e) {
                    // If we can't move the directory we have to make a copy of it
                    dirOp = SubmissionDirState.HOME;
                }

                if (dirOp == SubmissionDirState.HOME) {
                    try {
                        Files.createDirectories(histDirTmp);
                        fileMngr.copyDirectory(origDir, histDirTmp);
                        dirOp = SubmissionDirState.COPIED;
                    } catch (Exception ex1) {
                        log.error("Can't copy directory " + origDir + " to " + histDirTmp + " : " + ex1.getMessage());
                        gln.log(Level.ERROR, "File operation error. Contact system administrator");

                        dirOp = null; // Bad. We have to break the operation
                    }

                }
            }

            if (toHistory) {
                sbm.setMTime(System.currentTimeMillis() / 1000);
                sbm.setVersion(-sbm.getVersion());
            } else {
                em.remove(sbm);
            }

        } finally {

            try {
                if (dirOp != null) {
                    trn.commit();
                } else {
                    trn.rollback();
                    trnOk = false;
                }
            } catch (Throwable t) {
                trnOk = false;

                String err = "Database transaction failed: " + t.getMessage();

                gln.log(Level.ERROR, err);

                if (trn.isActive()) {
                    trn.rollback();
                }
            }

            if (toHistory) {

                if (trnOk) {
                    if (dirOp != SubmissionDirState.ABSENT) {
                        try {
                            fileMngr.moveDirectory(histDirTmp, histDir);
                        } catch (IOException e) {
                            log.error("History directory '" + histDirTmp + "' rename failed: " + e);
                            e.printStackTrace();
                            trnOk = false;
                        }

                        if (dirOp == SubmissionDirState.LINKED) {
                            try {
                                Files.delete(origDir);
                            } catch (IOException e) {
                                log.error("Can't delete symbolic link: " + origDir + " :" + e);
                                e.printStackTrace();
                                trnOk = false;
                            }
                        } else if (dirOp == SubmissionDirState.COPIED) {
                            try {
                                fileMngr.deleteDirectory(origDir);
                            } catch (IOException e) {
                                log.error("Can't delete directory: " + origDir + " :" + e);
                                e.printStackTrace();
                                trnOk = false;
                            }
                        }
                    }

                    if (trnOk) {
                        gln.log(Level.INFO, "Transaction successful");
                    } else {
                        gln.log(Level.WARN,
                                "Transaction successful but with some problems. Please inform system administrator ");
                    }

                    trnOk = true;
                } else {
                    if (dirOp == SubmissionDirState.LINKED) {
                        try {
                            Files.delete(origDir);
                            fileMngr.moveDirectory(histDirTmp, origDir);
                        } catch (IOException e) {
                            log.error("Delete opration rollback (move dir) failed: " + e);
                            e.printStackTrace();

                            gln.log(Level.ERROR,
                                    "Severe server problem. Please inform system administrator. The database may be "
                                            + "inconsistent");
                        }

                    } else if (dirOp == SubmissionDirState.COPIED) {
                        try {
                            fileMngr.deleteDirectory(histDirTmp);
                        } catch (IOException e) {
                            log.error("Delete opration rollback (del dir) failed: " + e);
                            e.printStackTrace();
                        }
                    }
                }
            } else if (trnOk) {
                try {
                    fileMngr.deleteDirectory(origDir);
                    gln.log(Level.INFO, "Transaction successful");
                } catch (IOException e) {
                    log.error("Delete directory '" + origDir + "' opration failed: " + e);
                    e.printStackTrace();
                    gln.log(Level.WARN,
                            "Transaction successful but with some problems. Please inform system administrator ");
                }
            }

            em.close();
        }

        if (trnOk && BackendConfig.getPublicFTPPath() != null) {
            Path ftpPath = BackendConfig.getSubmissionPublicFTPPath(sbm);

            if (Files.exists(ftpPath)) {
                try {
                    fileMngr.deleteDirectory(ftpPath);
                } catch (Exception e) {
                    log.error("Can't delete public ftp directory " + ftpPath + " Error: " + e.getMessage());
                    e.printStackTrace();
                    gln.log(Level.WARN, "Public FTP directory was not deleted");
                }
            }

        }

        return gln;
    }

    @Override
    public Submission getSubmissionsByAccession(String acc) {
        EntityManager manager = BackendConfig.getServiceManager().getEntityManager();
        EntityTransaction transaction = manager.getTransaction();
        TypedQuery<Submission> query = manager.
                createNamedQuery(Submission.GetByAccQuery, Submission.class)
                .setParameter("accNo", acc);

        try {
            return query.getResultList().stream().findFirst().orElse(null);
        } finally {
            DatabaseUtil.commitIfActiveAndNotNull(transaction);
        }
    }

    @Override
    public SubmissionReport createSubmission(byte[] data, DataFormat type, String charset, Operation op, User usr,
            boolean validateOnly, boolean ignoreAbsntFiles, String domain) {
        try {
            return createSubmissionUnsafe(data, type, charset, op, usr, validateOnly, ignoreAbsntFiles, domain);
        } catch (Throwable e) {
            log.error("createSubmissionUnsafe: uncought exception " + e);

            ExceptionUtil.unroll(e).printStackTrace();

            SimpleLogNode gln = new SimpleLogNode(Level.SUCCESS,
                    op.name() + " submission(s) from " + type.name() + " source", null);
            gln.log(Level.ERROR, "Internal server error");

            SubmissionReport res = new SubmissionReport();
            res.setLog(gln);
            return res;
        }
    }

    private boolean checkAccNoPfxSfx(SubmissionInfo si) {
        boolean submOk = true;

        try {
            si.setAccNoPrefix(checkAccNoPart(si.getAccNoPrefix()));
        } catch (Exception e) {
            si.getLogNode().log(Level.ERROR, "Submission accession number prefix contains invalid characters");
            submOk = false;
        }

        try {
            si.setAccNoSuffix(checkAccNoPart(si.getAccNoSuffix()));
        } catch (Exception e) {
            si.getLogNode().log(Level.ERROR, "Submission accession number prefix contains invalid characters");
            submOk = false;
        }

        Submission submission = si.getSubmission();

        if (si.getAccNoPrefix() == null && si.getAccNoSuffix() == null) {
            try {
                submission.setAccNo(checkAccNoPart(submission.getAccNo()));
            } catch (Exception e) {
                si.getLogNode().log(Level.ERROR, "Submission accession number contains invalid characters");
                submOk = false;
            }

            if (submission.getAccNo() == null) {
                si.setAccNoPrefix(BackendConfig.getDefaultSubmissionAccPrefix());
                si.setAccNoSuffix(BackendConfig.getDefaultSubmissionAccSuffix());
            }
        }

        return submOk;
    }

    private SubmissionReport createSubmissionUnsafe(byte[] data, DataFormat type, String charset, Operation op,
            User usr, boolean validateOnly, boolean ignoreFileAbs, String domain) {

        EntityManager em = BackendConfig.getServiceManager().getEntityManager();
        FileManager fileManager = BackendConfig.getServiceManager().getFileManager();
        uk.ac.ebi.biostd.webapp.server.mng.security.SecurityManager securityManager = BackendConfig.getServiceManager()
                .getSecurityManager();
        AccessionManager accessionManager = BackendConfig.getServiceManager().getAccessionManager();

        ErrorCounter ec = new ErrorCounterImpl();

        SimpleLogNode logNode = new SimpleLogNode(Level.SUCCESS,
                op.name() + " submission(s) from " + type.name() + " source", ec);

        SubmissionReport res = new SubmissionReport();
        res.setLog(logNode);

        if (op == Operation.CREATE && !securityManager.mayUserCreateSubmission(usr)) {
            logNode.log(Level.ERROR, "User has no permission to create submissions");
            return res;
        }

        logNode.log(Level.INFO, "Processing '" + type.name() + "' data. Body size: " + data.length);

        boolean submOk = true;
        boolean submComplete = false;

        PMDoc doc = null;

        Path trnPath = BackendConfig.getSubmissionsTransactionPath()
                .resolve(BackendConfig.getInstanceId() + "#" + BackendConfig.getSeqNumber());

        List<FileTransactionUnit> trans = null;

        LockedIdSet submIdLock = null;

        EntityTransaction trn = null;

        try {
            doc = parser.parseDocument(data, type, charset, new TagResolverImpl(em), logNode);

            if (doc == null) {
                return res;
            }

            if (doc.getSubmissions() == null || doc.getSubmissions().size() == 0) {
                logNode.log(Level.ERROR, "There are no submissions in the document");
                SimpleLogNode.setLevels(logNode);
                return res;
            }

            Optional<Map<String, ElementPointer>> smbIdMap = getSubmissionIdMap(doc);
            Optional<Map<String, ElementPointer>> secIdMap = getSectionIdMap(doc);

            if (smbIdMap.isPresent() && secIdMap.isPresent()) {
                submIdLock = waitForIdUnlocked(smbIdMap.get(), secIdMap.get());
            } else {
                submOk = false;
            }

            trn = em.getTransaction();

            trn.begin();

            for (SubmissionInfo si : doc.getSubmissions()) {
                Submission submission = si.getSubmission();

                if (!Strings.isNullOrEmpty(domain)) {
                    submission.addAccessTag(getDomainTag(em, domain));
                }
                submission.setOwner(usr);

                submOk = submOk && checkAccNoPfxSfx(si);

                Set<String> goingGlobSecId = null;

                long ts = System.currentTimeMillis() / 1000;

                submission.setMTime(ts);

                Submission oldSbm = null;

                if (op == Operation.UPDATE || op == Operation.CREATEUPDATE || op == Operation.OVERRIDE
                        || op == Operation.CREATEOVERRIDE) {
                    if ((si.getAccNoPrefix() != null || si.getAccNoSuffix() != null || submission.getAccNo() == null)
                            && (op == Operation.UPDATE || op == Operation.OVERRIDE)) {
                        si.getLogNode().log(Level.ERROR,
                                "Submission must have accession number for " + op.name() + " operation");
                        submOk = false;
                        continue;
                    }

                    oldSbm = getSubmissionByAcc(submission.getAccNo(), em);

                    if (oldSbm == null) {
                        if (op == Operation.UPDATE || op == Operation.OVERRIDE) {
                            si.getLogNode().log(Level.ERROR,
                                    "Submission '" + submission.getAccNo() + "' doesn't exist and can't be updated");
                            submOk = false;
                            continue;
                        } else {
                            if (!securityManager.mayUserCreateSubmission(usr)) {
                                si.getLogNode().log(Level.ERROR, "User has no permission to create submissions");
                                submOk = false;
                                continue;
                            }

                            submission.setCTime(ts);
                            submission.setVersion(1);
                        }
                    } else {
                        submission.setCTime(oldSbm.getCTime());
                        si.setOriginalSubmission(oldSbm);
                        submission.setVersion(oldSbm.getVersion() + 1);
                        submission.setSecretKey(oldSbm.getSecretKey());
                        if (!securityManager.mayUserUpdateSubmission(oldSbm, usr)) {
                            si.getLogNode().log(Level.ERROR, "Submission update is not permitted for this user");
                            submOk = false;
                            continue;
                        }

                        goingGlobSecId = new HashSet<>();

                        collectGlobalSecIds(oldSbm.getRootSection(), goingGlobSecId);
                    }

                } else {
                    submission.setCTime(ts);
                    submission.setVersion(1);
                }

                if (submission.getVersion() == 1 && si.getAccNoPrefix() == null && si.getAccNoSuffix() == null
                        && submission.getAccNo() != null) {
                    submission.setVersion(correctVersion(submission.getAccNo(), em));
                }

                try {
                    submission.normalizeAttributes();
                } catch (SubmissionAttributeException e) {
                    si.getLogNode().log(Level.ERROR, e.getMessage());
                    submOk = false;
                }

                if (submission.isRTimeSet() && submission.getRTime() * 1000 < System.currentTimeMillis()) {
                    boolean pub = false;

                    if (submission.getAccessTags() != null) {
                        for (AccessTag t : submission.getAccessTags()) {
                            if (t.getName().equals(BackendConfig.PublicTag)) {
                                pub = true;
                                break;
                            }
                        }

                    }

                    if (!pub) {
                        submission.addAccessTag(getPublicTag(em));
                    }
                }

                String rootPathAttr = submission.getRootPath();

                PathInfo rootPI = null;

                if (rootPathAttr == null) {
                    rootPathAttr = "";
                }

                try {
                    rootPI = PathInfo.getPathInfo(rootPathAttr, usr);
                } catch (InvalidPathException e) {
                    si.getLogNode().log(Level.ERROR, "Invalid root path: " + rootPathAttr);
                    submOk = false;
                }

                if (submission.getAccessTags() != null) {
                    for (AccessTag t : submission.getAccessTags()) {
                        if (t.getName().equals(BackendConfig.PublicTag)) {

                            if (submission.isRTimeSet() && submission.getRTime() * 1000 > System.currentTimeMillis()) {
                                submission.setRTime(System.currentTimeMillis() / 1000);
                            }

                            submission.setReleased(true);
                            break;
                        }
                    }
                }

                if (si.getFileOccurrences() != null) {
                    for (FileOccurrence foc : si.getFileOccurrences()) {
                        FilePointer fp = fileManager.checkFileExist(foc.getFileRef().getName(), rootPI, usr);
                        FilePointer oldFp = (fp == null && oldSbm != null) ?
                                fileManager.checkFileExist(foc.getFileRef().getName(), rootPI, usr, oldSbm) : null;

                        if (oldFp != null) {
                            foc.getLogNode().log(Level.WARN, "File reference '" + foc.getFileRef().getName()
                                    + "' can't be resolved in user directory. Using file from previous submission");
                            fp = oldFp;
                        }

                        if (fp != null) {
                            foc.setFilePointer(fp);

                            if (fp.getSize() == 0 && !fp.isDirectory()) {
                                foc.getLogNode().log(Level.WARN,
                                        "File reference: '" + foc.getFileRef().getName() + "' File size is zero");
                            }

                        } else if (ignoreFileAbs) {
                            foc.getLogNode().log(Level.WARN, "File reference '" + foc.getFileRef().getName()
                                    + "' can't be resolved. Ignoring in test mode");
                        } else {
                            foc.getLogNode().log(Level.ERROR, "File reference '" + foc.getFileRef().getName()
                                    + "' can't be resolved. Check files in the user directory");
                            submOk = false;
                        }
                    }
                }

                if (si.getAccNoPrefix() == null && si.getAccNoSuffix() == null && oldSbm == null
                        && submission.getAccNo() != null) {
                    if (!checkSubmissionIdUniq(submission.getAccNo(), em)) {
                        si.getLogNode().log(Level.ERROR, "Submission accession number '" + submission.getAccNo()
                                + "' is already taken by another submission");
                        submOk = false;
                    }
                }

                String accNoPat = Submission.getNodeAccNoPattern(si.getSubmission());

                if (accNoPat != null) {
                    accNoPat = accNoPat.trim();

                    if (accNoPat.length() == 0) {
                        accNoPat = null;
                    } else {
                        try {
                            Pattern.compile(accNoPat);
                        } catch (Exception e) {
                            accNoPat = null;
                        }
                    }

                    if (accNoPat == null) {
                        si.getLogNode().log(Level.ERROR, "Invalid value of '" + Submission.canonicAccNoPattern
                                + "' attribute. Must be valid regular expression");
                    }
                }

                List<String> pAccL = Submission.getNodeAttachTo(si.getSubmission());

                if (pAccL != null && pAccL.size() != 0) {
                    List<Submission> notMatched = new ArrayList<>();
                    boolean matched = false;

                    for (String pAcc : pAccL) {
                        Submission parent = getSubmissionByAcc(pAcc, em);

                        if (parent == null) {
                            si.getLogNode().log(Level.ERROR,
                                    "Submission attribute 'AttachTo' points to non existing submission '" + pAcc + "'");
                            submOk = false;

                            continue;
                        }

                        if (!securityManager.mayUserAttachToSubmission(parent, usr)) {
                            si.getLogNode().log(Level.ERROR, "User has no permission to attach to submission: " + pAcc);
                            submOk = false;
                            continue;
                        }

                        submOk = submOk && isEUToxRiskFileValid(si, parent);

                        AccNoMatcher.Match mtch = AccNoMatcher.match(si, parent);

                        if (mtch == Match.NO) {
                            notMatched.add(parent);
                        } else if (mtch == Match.YES) {
                            matched = true;
                        }

                        Collection<AccessTag> newSet = si.getSubmission().getAccessTags();
                        Collection<AccessTag> parentTags = parent.getAccessTags();

                        if (parentTags != null) {
                            if (newSet == null) {
                                newSet = new ArrayList<>();
                                si.getSubmission().setAccessTags(newSet);
                            }
                            for (AccessTag pTag : parentTags) {
                                boolean found = false;
                                for (AccessTag aTag : newSet) {
                                    if (pTag.getId() == aTag.getId()) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found && !pTag.getName().equals(BackendConfig.PublicTag)) {
                                    newSet.add(pTag);
                                }
                            }
                        }
                    }

                    if (!matched && notMatched.size() > 0) {
                        for (Submission nms : notMatched) {
                            si.getLogNode().log(Level.ERROR,
                                    "AccNo doesn't match to host submission (" + nms.getAccNo() + ") requirements: "
                                            + Submission.getNodeAccNoPattern(nms));
                        }

                        submOk = false;
                    }
                }

                if (si.getGlobalSections() != null) {
                    for (SectionOccurrence seco : si.getGlobalSections()) {
                        try {
                            seco.setPrefix(checkAccNoPart(seco.getPrefix()));
                        } catch (Exception e) {
                            seco.getSecLogNode()
                                    .log(Level.ERROR, "Section accession number prefix contains invalid characters");
                            submOk = false;
                        }

                        try {
                            seco.setSuffix(checkAccNoPart(seco.getSuffix()));
                        } catch (Exception e) {
                            seco.getSecLogNode()
                                    .log(Level.ERROR, "Section accession number prefix contains invalid characters");
                            submOk = false;
                        }

                        if (seco.getSuffix() == null && seco.getPrefix() == null) {
                            try {
                                seco.getSection().setAccNo(checkAccNoPart(seco.getSection().getAccNo()));
                            } catch (Exception e) {
                                seco.getSecLogNode()
                                        .log(Level.ERROR, "Section accssesion number contains invalid characters");
                                submOk = false;
                            }

                        }

                        if (seco.getPrefix() == null && seco.getSuffix() == null && seco.getSection().getAccNo() != null
                                && (goingGlobSecId == null || !goingGlobSecId.contains(seco.getSection().getAccNo()))) {
                            if (!checkSectionIdUniq(seco.getSection().getAccNo(), em)) {
                                seco.getSecLogNode().log(Level.ERROR,
                                        "Section accession number '" + seco.getSection().getAccNo()
                                                + "' is taken by another section");
                                submOk = false;
                            }
                        }
                    }
                }

            }

            if (!submOk || validateOnly) {
                SimpleLogNode.setLevels(logNode);

                if (validateOnly) {
                    submComplete = true;
                }

                return res;
            }

            int sbmNo = 0;

            for (SubmissionInfo si : doc.getSubmissions()) {
                sbmNo++;

                Submission subm = si.getSubmission();

                SubmissionMapping sMap = new SubmissionMapping();
                sMap.getSubmissionMapping().setOrigAcc(si.getAccNoOriginal());
                sMap.getSubmissionMapping().setPosition(new int[]{sbmNo});
                sMap.getSubmissionMapping().setAssignedAcc(subm.getAccNo());

                res.addSubmissionMapping(sMap);

                if (!validateOnly && (si.getAccNoPrefix() != null || si.getAccNoSuffix() != null
                        || subm.getAccNo() == null)) {
                    while (true) {
                        try {
                            String newAcc = accessionManager
                                    .getNextAccNo(si.getAccNoPrefix(), si.getAccNoSuffix(), usr);

                            if (checkGeneratedSubmissionIdUniq(newAcc, em)) {
                                subm.setAccNo(newAcc);
                                si.getLogNode().log(Level.INFO, "Submission generated accNo: " + newAcc);

                                sMap.getSubmissionMapping().setAssignedAcc(subm.getAccNo());

                                break;
                            }
                        } catch (SecurityException e) {
                            si.getLogNode().log(Level.ERROR,
                                    "User has no permission to generate accession number: " + e.getMessage());
                            submOk = false;
                            break;
                        }
                    }
                }

                if (subm.getTitle() == null) {
                    subm.setTitle(subm.getAccNo() + " " + SimpleDateFormat.getDateTimeInstance()
                            .format(new Date(subm.getCTime() * 1000)));
                }

                if (si.getGlobalSections() != null) {
                    for (SectionOccurrence seco : si.getGlobalSections()) {
                        if (!validateOnly && (seco.getPrefix() != null || seco.getSuffix() != null
                                || seco.getSection().getAccNo() == null)) {
                            String localId = seco.getLocalId();

                            while (true) {
                                String newAcc = null;
                                try {
                                    newAcc = accessionManager
                                            .getNextAccNo(seco.getPrefix(), seco.getSuffix(), usr);

                                    if (checkSectionIdUniqTotal(newAcc, em)) {
                                        seco.getSection().setAccNo(newAcc);
                                        seco.getSecLogNode().log(Level.INFO, "Section generated accNo: " + newAcc);
                                        break;
                                    }
                                } catch (SecurityException e) {
                                    seco.getSecLogNode().log(Level.ERROR,
                                            "User has no permission to generate accession number: " + e.getMessage());
                                    submOk = false;
                                    break;
                                }

                            }

                            AccessionMapping secMap = new AccessionMapping();

                            secMap.setAssignedAcc(seco.getSection().getAccNo());
                            secMap.setOrigAcc(localId);

                            int[] pth = new int[seco.getPath().size() + 1];

                            int i = 0;

                            pth[i++] = sbmNo;

                            for (SectionOccurrence ptoc : seco.getPath()) {
                                pth[i++] = ptoc.getPosition();
                            }

                            secMap.setPosition(pth);

                            sMap.addSectionMapping(secMap);
                        }
                    }
                }

                if (si.getOriginalSubmission() != null) {
                    if (op == Operation.OVERRIDE || op == Operation.CREATEOVERRIDE) {
                        em.remove(si.getOriginalSubmission());
                    } else {
                        si.getOriginalSubmission().setVersion(-si.getOriginalSubmission().getVersion());
                    }
                }

                if (subm.getSecretKey() == null) {
                    subm.setSecretKey(UUID.randomUUID().toString());
                }

                em.persist(subm);
            }

            submComplete = true;

            trans = new ArrayList<>(doc.getSubmissions().size());

            if (!prepareFileTransaction(fileManager, trans, doc.getSubmissions(), trnPath, op)) {
                logNode.log(Level.ERROR, "File operation failed. Contact system administrator");
                submOk = false;
            }

        } catch (Throwable t) {
            logNode.log(Level.ERROR, "Internal server error");

            t.printStackTrace();
            log.error("Exception during submission process: " + t.getMessage());

            submOk = false;
        } finally {
            try {

                if (!submOk || !submComplete) {
                    if (trn != null && trn.isActive()) {
                        trn.rollback();
                    }

                    logNode.log(Level.ERROR, "Submit/Update operation failed. Rolling transaction back");

                    if (trans != null) {
                        rollbackFileTransaction(fileManager, trans, trnPath);
                    }

                    return res;
                } else {
                    try {
                        if (trn != null && trn.isActive()) {
                            trn.commit();
                        }

                        if (trans != null) {
                            try {
                                commitFileTransaction(fileManager, trans, trnPath, op);
                            } catch (IOException ioe) {
                                String err = "File transaction commit failed: " + ioe.getMessage();

                                logNode.log(Level.ERROR, err);
                                log.error(err);

                                ioe.printStackTrace();

                                return res;
                            }
                        }
                    } catch (Throwable t) {
                        String err = "Database transaction commit failed: " + t.getMessage();

                        logNode.log(Level.ERROR, err);
                        log.error(err);

                        t.printStackTrace();

                        if (trn != null && trn.isActive()) {
                            trn.rollback();
                        }

                        if (trans != null) {
                            rollbackFileTransaction(fileManager, trans, trnPath);
                        }

                        return res;
                    }

                    logNode.log(Level.INFO, "Database transaction successful");
                }
            } finally {
                if (em != null) {
                    em.close();
                }

                unlockIds(submIdLock);
            }
        }

        if (trans != null && BackendConfig.getPublicFTPPath() != null) {
            copyToPublicFTP(fileManager, doc.getSubmissions(), logNode);
        }

        return res;
    }

    private boolean isEUToxRiskFileValid(SubmissionInfo si, Submission parent) {
        if (!eutoxriskFileValidator.isApplicableTo(si.getSubmission(), parent.getAccNo())) {
            return true;
        }

        Collection<EUToxRiskFileValidationError> errors = eutoxriskFileValidator.validateFirst(
                si.getFileOccurrences().stream()
                        .map(FileOccurrence::getFilePointer)
                        .filter(Objects::nonNull)
                        .map(FilePointer::getFullPath)
                        .map(Path::toFile)
                        .collect(Collectors.toList()));

        errors.forEach(error -> {
            si.getLogNode().log(Level.ERROR, error.toString());
        });
        return errors.isEmpty();
    }


    private Optional<Map<String, ElementPointer>> getSubmissionIdMap(PMDoc doc) {
        Map<String, ElementPointer> idMap = new HashMap<>();

        for (SubmissionInfo si : doc.getSubmissions()) {
            if (si.getAccNoPrefix() != null || si.getAccNoSuffix() != null || si.getSubmission().getAccNo() == null) {
                continue;
            }

            ElementPointer sbmPtr = idMap.get(si.getSubmission().getAccNo());

            if (sbmPtr != null) {
                si.getLogNode().log(Level.ERROR,
                        "Accession number '" + si.getSubmission().getAccNo() + " is already taken by submission at "
                                + sbmPtr);
                return Optional.empty();
            } else {
                idMap.put(si.getSubmission().getAccNo(), si.getElementPointer());
            }
        }

        return Optional.of(idMap);
    }


    private Optional<Map<String, ElementPointer>> getSectionIdMap(PMDoc doc) {
        Map<String, ElementPointer> idMap = new HashMap<>();

        for (SubmissionInfo si : doc.getSubmissions()) {
            if (si.getGlobalSections() == null || si.getGlobalSections().size() == 0) {
                continue;
            }

            for (SectionOccurrence seco : si.getGlobalSections()) {
                if (seco.getPrefix() != null || seco.getSuffix() != null || seco.getSection().getAccNo() == null) {
                    continue;
                }

                ElementPointer secPtr = idMap.get(seco.getSection().getAccNo());

                if (secPtr != null) {
                    seco.getSecLogNode().log(Level.ERROR,
                            "Accession number '" + seco.getSection().getAccNo() + " is already taken by section at "
                                    + secPtr);
                    return Optional.empty();
                } else {
                    idMap.put(seco.getSection().getAccNo(), seco.getElementPointer());
                }
            }
        }

        return Optional.of(idMap);
    }


    private LockedIdSet waitForIdUnlocked(Map<String, ElementPointer> sbmIdMap, Map<String, ElementPointer> secIdMap)
            throws InterruptedException {
        LockedIdSet lckSet = new LockedIdSet();

        lckSet.setSubmissionMap(sbmIdMap);
        lckSet.setSectionMap(secIdMap);

        if (lckSet.empty()) {
            return null;
        }

        synchronized (lockedSmbIds) {
            while (true) {

                boolean needWait = false;

                if (sbmIdMap != null) {
                    for (String s : sbmIdMap.keySet()) {
                        LockInfo li = lockedSmbIds.get(s);

                        if (li != null) {
                            if (li.waiters == null) {
                                li.waiters = new HashSet<>();
                            }

                            li.waiters.add(Thread.currentThread().getName());

                            needWait = true;
                            break;
                        }
                    }
                }

                if (secIdMap != null && !needWait) {
                    for (String s : secIdMap.keySet()) {
                        if (lockedSecIds.contains(s)) {
                            needWait = true;
                            break;
                        }
                    }
                }

                if (!needWait) {
                    if (sbmIdMap != null) {
                        for (String s : sbmIdMap.keySet()) {
                            LockInfo li = new LockInfo();
                            li.lockOwner = Thread.currentThread().getName();

                            lockedSmbIds.put(s, li);
                        }
                    }

                    if (secIdMap != null) {
                        lockedSecIds.addAll(secIdMap.keySet());
                    }

                    return lckSet;
                }

                lckSet.incWaitCount();

                while (true) {
                    try {
                        lockedSmbIds.wait();
                        break;
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

    }

    private void unlockIds(LockedIdSet lset) {
        if (lset == null) {
            return;
        }

        synchronized (lockedSmbIds) {
            if (lset.getSubmissionMap() != null) {
                lockedSmbIds.keySet().removeAll(lset.getSubmissionMap().keySet());
            }

            if (lset.getSectionMap() != null) {
                lockedSecIds.removeAll(lset.getSectionMap().keySet());
            }

            lockedSmbIds.notifyAll();
        }
    }

    private AccessTag getPublicTag(EntityManager em) {
        Query q = em.createNamedQuery("AccessTag.getByName");
        q.setParameter("name", BackendConfig.PublicTag);

        return (AccessTag) q.getSingleResult();
    }

    private AccessTag getDomainTag(EntityManager em, String domain) {
        Query q = em.createNamedQuery("AccessTag.getByName");
        q.setParameter("name", domain);

        return (AccessTag) q.getSingleResult();
    }

    private String checkAccNoPart(String acc) throws Exception {
        if (acc == null) {
            return null;
        }

        acc = acc.trim();

        if (acc.length() == 0) {
            return null;
        }

        if (!AccNoUtil.checkAccNoStr(acc)) {
            throw new Exception("Invalid characters");
        }

        return acc;
    }

    private void copyToPublicFTP(FileManager fileMngr, List<SubmissionInfo> list, LogNode gln) {
        for (SubmissionInfo si : list) {
            Path sourceDir = BackendConfig.getSubmissionFilesPath(si.getSubmission());
            Path targetDir = BackendConfig.getSubmissionPublicFTPPath(si.getSubmission());

            try {

                if (Files.exists(targetDir)) {
                    fileMngr.deleteDirectory(targetDir);
                }

                if (Files.exists(sourceDir) && BackendConfig.getServiceManager().getSecurityManager()
                        .mayEveryoneReadSubmission(si.getSubmission())) {
                    fileMngr.linkOrCopyDirectory(sourceDir, targetDir);

                    try {
                        if (BackendConfig.getServiceManager().getSecurityManager()
                                .mayEveryoneReadSubmission(si.getSubmission())) {
                            Files.setPosixFilePermissions(targetDir, BackendConfig.rwxrwxr_x);
                        } else {
                            Files.setPosixFilePermissions(targetDir, BackendConfig.rwxrwx___);
                        }
                    } catch (UnsupportedOperationException e) {
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                si.getLogNode()
                        .log(Level.WARN, "Submission files were not copied to public FTP directory due to error");
                log.error("Coping to FTP directory error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void rollbackFileTransaction(FileManager fileMngr, List<FileTransactionUnit> trans, Path trnPath) {
        for (FileTransactionUnit ftu : trans) {
            try {
                if (ftu.historyPathTmp != null) {
                    if (Files.isSymbolicLink(ftu.submissionPath)) {
                        Files.delete(ftu.submissionPath);
                        Files.move(ftu.historyPathTmp, ftu.submissionPath);
                    } else {
                        fileMngr.deleteDirectory(ftu.historyPathTmp);
                    }
                }

                if (ftu.submissionPathTmp != null) {
                    fileMngr.deleteDirectory(ftu.submissionPathTmp);
                }
            } catch (Exception e) {
                log.error("File operation error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            fileMngr.deleteDirectory(trnPath);
        } catch (Exception e) {
            log.error("Can't delete transaction directory: '" + trnPath + "' " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void commitFileTransaction(FileManager fileMngr, List<FileTransactionUnit> trans, Path trnPath,
            Operation op) throws IOException {
        for (FileTransactionUnit ftu : trans) {

            Path dirToDel = null;

            if (ftu.state == SubmissionDirState.COPIED) {
                if (op == Operation.OVERRIDE || op == Operation.CREATEOVERRIDE) {
                    fileMngr.deleteDirectory(ftu.historyPathTmp);
                } else {
                    Files.move(ftu.historyPathTmp, ftu.historyPath);
                }

                dirToDel = ftu.submissionPathTmp.getParent().resolve(ftu.submissionPath.getFileName() + "~");
                Files.move(ftu.submissionPath, dirToDel);
            } else if (ftu.state == SubmissionDirState.HOME) {
                if (op == Operation.OVERRIDE || op == Operation.CREATEOVERRIDE) {
                    dirToDel = ftu.submissionPath;
                } else {
                    Files.move(ftu.submissionPath, ftu.historyPath);
                }
            } else if (ftu.state == SubmissionDirState.LINKED) {
                Files.move(ftu.historyPathTmp, ftu.historyPath);
                Files.delete(ftu.submissionPath);
            }

            if (dirToDel != null) {
                try {
                    fileMngr.deleteDirectory(dirToDel);
                } catch (Exception ex3) {
                    log.error("Can't delete directory of dirsymlink: " + dirToDel + " " + ex3.getMessage());
                }
            }

            if (ftu.submissionPathTmp != null) {
                Files.move(ftu.submissionPathTmp, ftu.submissionPath);
            }

        }

        try {
            Files.delete(trnPath);
        } catch (Exception ex4) {
            log.error("Can't delete directory : " + trnPath + " " + ex4.getMessage());
        }

    }

    private boolean prepareFileTransaction(FileManager fileMngr, List<FileTransactionUnit> trans,
            Collection<SubmissionInfo> subs, Path trnPath, Operation op) {

        for (SubmissionInfo si : subs) {

            FileTransactionUnit ftu = new FileTransactionUnit();
            trans.add(ftu);

            Path origDir = BackendConfig.getSubmissionPath(si.getSubmission());
            ftu.submissionPath = origDir;

            try {
                Files.createDirectories(origDir.getParent());
            } catch (IOException e2) {
                log.error("Can't create directory: " + origDir.getParent());
                return false; // Bad. We have to break the operation
            }

            si.getSubmission().setRelPath(BackendConfig.getSubmissionRelativePath(si.getSubmission()));

            ftu.state = SubmissionDirState.ABSENT;

            if (si.getOriginalSubmission() != null) {
                Path histDir = BackendConfig.getSubmissionHistoryPath(si.getOriginalSubmission());

                if (Files.exists(histDir)) {
                    log.error("History directory already exists: " + histDir);
                    return false;
                }

                if (Files.exists(origDir)) {
                    if (op != Operation.OVERRIDE && op != Operation.CREATEOVERRIDE) {
                        ftu.historyPath = histDir;

                        Path histDirTmp = histDir.resolveSibling(histDir.getFileName() + "#tmp");

                        try {
                            fileMngr.moveDirectory(origDir,
                                    histDirTmp); // trying to move submission directory to the history dir
                            ftu.historyPathTmp = histDirTmp;

                            try {
                                Files.createSymbolicLink(origDir,
                                        histDirTmp); //to provide access to the submission before the commit
                                ftu.state = SubmissionDirState.LINKED;
                            } catch (Exception ex2) {
                                fileMngr.moveDirectory(histDirTmp,
                                        origDir); //if we can't make a symbolic link (FAT?) let's return the
                                // directory back
                                ftu.historyPathTmp = null; // Signaling that the directory was not neither moved nor
                                // copied
                                ftu.state = SubmissionDirState.HOME;
                            }
                        } catch (Exception e) {
                            // If we can't move the directory we have to make a copy of it

                            try {
                                Files.createDirectories(histDirTmp);
                                fileMngr.copyDirectory(origDir, histDirTmp);
                                ftu.historyPathTmp = histDirTmp;
                                ftu.state = SubmissionDirState.COPIED;
                            } catch (Exception ex1) {
                                log.error("Can't copy directory " + origDir + " to " + histDirTmp + " : " + ex1
                                        .getMessage());

                                return false; // Bad. We have to break the operation
                            }

                        }
                    } else {
                        ftu.historyPathTmp = null;
                        ftu.state = SubmissionDirState.HOME;
                    }
                }
            } else if (Files.exists(origDir)) {
                log.warn("Directory " + origDir + " exists unexpectedly");

                try {
                    if (Files.isDirectory(origDir)) {
                        FileUtils.deleteDirectory(origDir.toFile());
                    } else {
                        Files.delete(origDir);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("Can't remove file/directory: " + origDir);
                    return false;
                }
            }

            Path trnSbmPath = trnPath.resolve(si.getSubmission().getAccNo());

            try {
                Files.createDirectories(trnSbmPath);
                ftu.submissionPathTmp = trnSbmPath;
            } catch (IOException e1) {
                log.error("Create submission transaction dir (" + trnSbmPath + ") error. " + e1.getMessage());
                e1.printStackTrace();

                return false;
            }

            try {
                if (BackendConfig.getServiceManager().getSecurityManager()
                        .mayEveryoneReadSubmission(si.getSubmission())) {
                    Files.setPosixFilePermissions(trnSbmPath, BackendConfig.rwxrwxr_x);
                } else {
                    Files.setPosixFilePermissions(trnSbmPath, BackendConfig.rwxrwx___);
                }
            } catch (UnsupportedOperationException ex) {
            } catch (IOException e1) {
                log.error("Submission dir (" + trnSbmPath + ") set permissions error. " + e1.getMessage());
                e1.printStackTrace();

                return false;
            }

            Path sbmFilesPath = trnSbmPath.resolve(BackendConfig.SubmissionFilesDir);

            if (si.getFileOccurrences() != null) {
                Set<FileOccurrence> foSet = new HashSet<>();

                for (FileOccurrence fo : si.getFileOccurrences()) {
                    if (fo.getFilePointer() == null) // test mode. Ignoring absent files
                    {
                        continue;
                    }

                    fo.getFileRef().setSize(fo.getFilePointer().getSize());
                    fo.getFileRef().setDirectory(fo.getFilePointer().isDirectory());

                    if (foSet.contains(fo)) {
                        continue;
                    }

                    foSet.add(fo);

                    try {
                        String dstRelPath = fileMngr.linkOrCopy(sbmFilesPath, fo.getFilePointer());

                        fo.getFileRef().setPath(dstRelPath);
                        si.getLogNode().log(Level.INFO, "File '" + fo.getFileRef().getName() + "' transfer success");
                    } catch (IOException e) {
                        log.error("File " + fo.getFilePointer() + " transfer error: " + e.getMessage());
                        e.printStackTrace();
                        return false;
                    }
                }
            }

            PMDoc doc = new PMDoc();
            doc.addSubmission(si);

            try (PrintStream out = new PrintStream(
                    trnSbmPath.resolve(si.getSubmission().getAccNo() + ".xml").toFile())) {
                new PageMLFormatter(out, true).format(doc);
            } catch (Exception e) {
                si.getLogNode().log(Level.ERROR, "Can't generate XML source file");
                log.error("Can't generate XML source file: " + e.getMessage());
                e.printStackTrace();
            }

            try (PrintStream out = new PrintStream(
                    trnSbmPath.resolve(si.getSubmission().getAccNo() + ".json").toFile())) {
                new JSONFormatter(out, true).format(si.getSubmission(), out);
            } catch (Exception e) {
                si.getLogNode().log(Level.ERROR, "Can't generate JSON source file");
                log.error("Can't generate JSON source file: " + e.getMessage());
                e.printStackTrace();
            }

            try (PrintStream out = new PrintStream(
                    trnSbmPath.resolve(si.getSubmission().getAccNo() + ".pagetab.tsv").toFile())) {
                new CellFormatter(XSVCellStream.getTSVCellStream(out)).format(doc);
            } catch (Exception e) {
                si.getLogNode().log(Level.ERROR, "Can't generate Page-Tab source file");
                log.error("Can't generate Page-Tab source file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return true;
    }

    private void collectGlobalSecIds(Section sec, Set<String> globSecId) {
        if (sec.getSections() != null) {
            for (Section sbs : sec.getSections()) {
                collectGlobalSecIds(sbs, globSecId);
            }
        }
    }

    private Submission getSubmissionByAcc(String accNo, EntityManager em) {
        Query q = em.createNamedQuery(Submission.GetByAccQuery);

        q.setParameter("accNo", accNo);

        List<?> res = q.getResultList();

        if (res.size() == 0) {
            return null;
        }

        return (Submission) res.get(0);
    }


    private boolean checkSubmissionIdUniq(String accNo, EntityManager em) {
        Query q = em.createNamedQuery(Submission.GetCountByAccQuery);
        q.setParameter("accNo", accNo);

        return ((Number) q.getSingleResult()).intValue() == 0;
    }

    private int correctVersion(String accNo, EntityManager em) {
        Query q = em.createNamedQuery(Submission.GetMinVer);
        q.setParameter("accNo", accNo);

        List<?> res = q.getResultList();

        if (res.size() == 0 || res.get(0) == null) {
            return 1;
        }

        int ver = ((Number) res.get(0)).intValue();

        if (ver < 0) {
            ver = -ver;
        }

        return ver + 1;

    }

    private boolean checkGeneratedSubmissionIdUniq(String accNo, EntityManager em) {
        Query q = em.createNamedQuery(Submission.GetCountAllByAccQuery);
        q.setParameter("accNo", accNo);

        return ((Number) q.getSingleResult()).intValue() == 0;
    }


    private boolean checkSectionIdUniq(String accNo, EntityManager em) {
        Query q = em.createNamedQuery("Section.countByAccActive");
        q.setParameter("accNo", accNo);

        return ((Number) q.getSingleResult()).intValue() == 0;
    }

    private boolean checkSectionIdUniqTotal(String accNo, EntityManager em) {
        Query q = em.createNamedQuery("Section.countByAcc");
        q.setParameter("accNo", accNo);

        return ((Number) q.getSingleResult()).intValue() == 0;
    }

    @Override
    public LogNode tranklucateSubmissionById(int id, User user) {
        return new SimpleLogNode(Level.ERROR, "Tranklucating submissions by id is not implemented", null);
    }

    @Override
    public LogNode tranklucateSubmissionByAccessionPattern(String accPfx, User usr) {
        SimpleLogNode gln = new SimpleLogNode(Level.SUCCESS, "Tranklucating submissions by pattern '" + accPfx + "'",
                null);

        EntityManager em = emf.createEntityManager();

        List<String> res = null;

        try {
            TypedQuery<String> pq = em.createNamedQuery(Submission.GetAccByPatQuery, String.class);

            pq.setParameter("pattern", accPfx);

            res = pq.getResultList();

            if (res.size() == 0) {
                gln.log(Level.INFO, "No matches");
                return gln;
            }

            gln.log(Level.INFO, "Found " + res.size() + " matches");

            Query q = em.createNamedQuery(Submission.GetAllByAccQuery);

            for (String acc : res) {
                tranklucateSubmissionByAccession(acc, usr, gln.branch("Tranklucating submission '" + acc + "'"), em, q);
            }

        } catch (Exception e) {
            e.printStackTrace();

            log.error("Exception: " + e.getClass() + " Message: " + e.getMessage());

            gln.log(Level.ERROR, "Internal server error");
        } finally {
            em.close();
        }

        return gln;
    }

    @Override
    public LogNode tranklucateSubmissionByAccession(String acc, User usr) {
        SimpleLogNode gln = new SimpleLogNode(Level.SUCCESS, "Tranklucating submission '" + acc + "'", null);

        EntityManager em = BackendConfig.getServiceManager().getEntityManager();

        try {
            Query q = em.createNamedQuery(Submission.GetAllByAccQuery);

            tranklucateSubmissionByAccession(acc, usr, gln, em, q);
        } catch (Exception e) {
            e.printStackTrace();

            log.error("Exception: " + e.getClass() + " Message: " + e.getMessage());

            gln.log(Level.ERROR, "Internal server error");
        }

        return gln;
    }


    private LogNode tranklucateSubmissionByAccession(String acc, User usr, LogNode gln, EntityManager em, Query q) {
        FileManager fileMngr = BackendConfig.getServiceManager().getFileManager();

        boolean trnOk = false;

        Submission mainSbm = null;

        EntityTransaction trn = em.getTransaction();

        try {

            trn.begin();

            q.setParameter("accNo", acc);

            List<Submission> res = q.getResultList();

            if (res.size() == 0) {
                gln.log(Level.ERROR, "Submission not found");
                return gln;
            }

            for (Submission s : res) {
                if (s.getVersion() > 0) {
                    mainSbm = s;
                    break;
                }
            }

            if (mainSbm != null && !BackendConfig.getServiceManager().getSecurityManager()
                    .mayUserDeleteSubmission(mainSbm, usr)) {
                gln.log(Level.ERROR, "User has no permission to delete this submission");
                return gln;
            }

            for (Submission s : res) {
                em.remove(s);
            }

            trnOk = true;

            for (Submission s : res) {
                Path dir =
                        s == mainSbm ? BackendConfig.getSubmissionPath(s) : BackendConfig.getSubmissionHistoryPath(s);

                if (Files.exists(dir)) {
                    try {
                        fileMngr.deleteDirectory(dir);
                    } catch (Exception e) {
                        log.error("Can't delete submission directory " + dir + " Error: " + e.getMessage());
                        e.printStackTrace();
                        gln.log(Level.WARN, "Submission directory was not deleted");
                    }
                }
            }

        } finally {

            try {
                if (trnOk) {
                    trn.commit();
                    gln.log(Level.INFO, "Transaction successful");
                } else {
                    trn.rollback();
                }
            } catch (Throwable t) {
                trnOk = false;

                String err = "Database transaction failed: " + t.getMessage();

                gln.log(Level.ERROR, err);

                if (trn.isActive()) {
                    trn.rollback();
                }
            }

        }

        if (trnOk && BackendConfig.getPublicFTPPath() != null && mainSbm != null) {
            Path ftpPath = BackendConfig.getSubmissionPublicFTPPath(mainSbm);

            if (Files.exists(ftpPath)) {
                try {
                    fileMngr.deleteDirectory(ftpPath);
                } catch (Exception e) {
                    log.error("Can't delete public ftp directory " + ftpPath + " Error: " + e.getMessage());
                    e.printStackTrace();
                    gln.log(Level.WARN, "Public FTP directory was not deleted");
                }
            }
        }

        return gln;
    }

    @Override
    public LogNode updateSubmissionMeta(String acc, Collection<TagRef> tgRefs, Set<String> access, long rTime,
            User usr) {
        ErrorCounter ec = new ErrorCounterImpl();
        SimpleLogNode gln = new SimpleLogNode(Level.SUCCESS, "Amending submission '" + acc + "' meta information", ec);

        EntityManager em = emf.createEntityManager();

        boolean trnOk = true;

        Submission sbm = null;

        EntityTransaction trn = null;

        boolean wasPublic = false;
        boolean nowPublic = false;

        try {
            trn = em.getTransaction();

            trn.begin();

            Query q = em.createNamedQuery(Submission.GetByAccQuery);

            q.setParameter("accNo", acc);

            try {
                sbm = (Submission) q.getSingleResult();
            } catch (NoResultException e) {
            }

            if (sbm == null) {
                gln.log(Level.ERROR, "Submission not found");
                return gln;
            }

            if (!BackendConfig.getServiceManager().getSecurityManager().mayUserUpdateSubmission(sbm, usr)) {
                gln.log(Level.ERROR, "User has no permission to amend this submission");
                return gln;
            }

            List<SubmissionTagRef> tags = Collections.emptyList();

            if (tgRefs != null && tgRefs.size() > 0) {
                tags = new ArrayList<>(tgRefs.size());

                Query tagq = em.createNamedQuery("Tag.getByName");

                for (TagRef tr : tgRefs) {
                    tagq.setParameter("tname", tr.getTagName());
                    tagq.setParameter("cname", tr.getClassiferName());

                    List<Tag> res = tagq.getResultList();

                    if (res.size() == 0) {
                        gln.log(Level.ERROR,
                                "Tag " + tr.getClassiferName() + ":" + tr.getTagName() + " can't be resolved");
                        trnOk = false;
                    } else {
                        SubmissionTagRef str = new SubmissionTagRef();

                        str.setTag(res.get(0));
                        str.setParameter(tr.getTagValue());

                        tags.add(str);
                    }
                }
            }

            List<AccessTag> accTags = Collections.emptyList();

            if (access != null && access.size() > 0) {
                accTags = new ArrayList<>(access.size());

                Query acctq = em.createNamedQuery("AccessTag.getByName");

                for (String tName : access) {
                    acctq.setParameter("name", tName);

                    List<AccessTag> res = acctq.getResultList();

                    if (res.size() == 0) {
                        gln.log(Level.ERROR, "Access tag " + tName + " can't be resolved");
                        trnOk = false;
                    } else {
                        accTags.add(res.get(0));
                    }
                }

            }

            if (!trnOk) {
                return gln;
            }

            trnOk = false;

            if (tgRefs != null) {
                if (sbm.getTagRefs() != null) {
                    for (SubmissionTagRef str : sbm.getTagRefs()) {
                        em.remove(str);
                    }
                }

                sbm.setTagRefs(tags);
                trnOk = true;
            }

            if (access != null) {
                sbm.setAccessTags(accTags);
                trnOk = true;
            }

            if (rTime >= 0) {
                sbm.setRTime(rTime / 1000);
                sbm.setReleased(false);
                trnOk = true;
            }

        } catch (Exception e) {
            e.printStackTrace();

            gln.log(Level.ERROR, "Internal server error");

            trnOk = false;
        } finally {

            try {
                if (trnOk) {
                    trn.commit();
                    gln.log(Level.INFO, "Transaction successful");
                } else {
                    trn.rollback();
                }
            } catch (Throwable t) {
                trnOk = false;

                String err = "Database transaction failed: " + t.getMessage();

                gln.log(Level.ERROR, err);

                if (trn.isActive()) {
                    trn.rollback();
                }
            }

            if (em != null && em.isOpen()) {
                em.close();
            }

        }

        if (trnOk) {
            PMDoc doc = new PMDoc();
            doc.addSubmission(new SubmissionInfo(sbm));

            Path trnSbmPath = BackendConfig.getSubmissionPath(sbm);

            try (PrintStream out = new PrintStream(trnSbmPath.resolve(sbm.getAccNo() + ".xml").toFile())) {
                new PageMLFormatter(out, true).format(doc);
            } catch (Exception e) {
                gln.log(Level.WARN, "Can't generate XML source file");
                log.error("Can't generate XML source file: " + e.getMessage());
                e.printStackTrace();
            }

            try (PrintStream out = new PrintStream(trnSbmPath.resolve(sbm.getAccNo() + ".json").toFile())) {
                new JSONFormatter(out, true).format(sbm, out);
            } catch (Exception e) {
                gln.log(Level.WARN, "Can't generate JSON source file");
                log.error("Can't generate JSON source file: " + e.getMessage());
                e.printStackTrace();
            }

            try (PrintStream out = new PrintStream(trnSbmPath.resolve(sbm.getAccNo() + ".pagetab.tsv").toFile())) {
                new CellFormatter(XSVCellStream.getTSVCellStream(out)).format(doc);
            } catch (Exception e) {
                gln.log(Level.WARN, "Can't generate Page-Tab source file");
                log.error("Can't generate Page-Tab source file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return gln;
    }


    @Override
    public LogNode changeOwnerByAccession(String sbmAcc, String owner, User usr) {
        return changeOwnerByAccession(sbmAcc, owner, usr, false);
    }


    @Override
    public LogNode changeOwnerByAccessionPattern(String sbmAcc, String owner, User usr) {
        return changeOwnerByAccession(sbmAcc, owner, usr, true);
    }

    private LogNode changeOwnerByAccession(String sbmAcc, String owner, User usr, boolean isLike) {
        ErrorCounter ec = new ErrorCounterImpl();
        SimpleLogNode gln = new SimpleLogNode(Level.SUCCESS,
                "Changing submission(s) '" + sbmAcc + "' owner to '" + owner + "'", ec);

        if (!usr.isSuperuser()) {
            gln.log(Level.ERROR, "Permission denied: only superuser can do it");
            return gln;
        }

        EntityManager em = emf.createEntityManager();

        User newOwner = BackendConfig.getServiceManager().getSecurityManager().getUserByLogin(owner);

        if (newOwner == null) {
            newOwner = BackendConfig.getServiceManager().getSecurityManager().getUserByEmail(owner);
        }

        if (newOwner == null) {
            gln.log(Level.ERROR, "Invalid new owner: " + owner);
            return gln;
        }

        EntityTransaction trn = em.getTransaction();

        try {
            trn.begin();

            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaUpdate<Submission> upd = cb.createCriteriaUpdate(Submission.class);

            Root<Submission> r = upd.from(Submission.class);

            upd.set(r.get("owner"), newOwner.getId());

            if (isLike) {
                upd.where(cb.like(r.get("accNo"), sbmAcc));
            } else {
                upd.where(cb.equal(r.get("accNo"), sbmAcc));
            }

            int cnt = em.createQuery(upd).executeUpdate();

            gln.log(Level.INFO, "Submissions updated: " + cnt);
        } finally {
            try {
                trn.commit();
                gln.log(Level.INFO, "Transaction successful");
            } catch (Throwable t) {
                String err = "Database transaction failed: " + t.getMessage();

                gln.log(Level.ERROR, err);

                if (trn.isActive()) {
                    trn.rollback();
                }
            }

            if (em != null && em.isOpen()) {
                em.close();
            }

        }

        return gln;
    }


}
