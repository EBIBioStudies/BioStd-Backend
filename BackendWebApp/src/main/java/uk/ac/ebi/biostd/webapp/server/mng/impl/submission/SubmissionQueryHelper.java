package uk.ac.ebi.biostd.webapp.server.mng.impl.submission;

import static uk.ac.ebi.biostd.authz.ACR.Permit.ALLOW;
import static uk.ac.ebi.biostd.authz.SystemAction.ATTACHSUBM;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import uk.ac.ebi.biostd.authz.AccessTag;
import uk.ac.ebi.biostd.authz.User;
import uk.ac.ebi.biostd.model.Submission;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.mng.SubmissionSearchRequest;
import uk.ac.ebi.biostd.webapp.server.search.SearchMapper;
import uk.ac.ebi.biostd.webapp.server.util.DatabaseUtil;

@Slf4j
public class SubmissionQueryHelper {

    private static final String ACCESS_TAG_QUERY = "select t from AccessTag t";

    private static final String GET_ALL_HOST = "select sb from Submission sb join sb.rootSection rs where rs"
            + ".type=:type and sb.version > 0";

    private static final String GET_HOST_SUB_BY_TYPE_QUERY = "select sb from Submission sb join sb"
            + ".rootSection rs join sb.accessTags at where rs.type=:type and at.id in :allow and sb.version > 0";

    public Collection<Submission> searchSubmissions(User u, SubmissionSearchRequest ssr) throws ParseException {
        Collection<Submission> res = null;

        EntityManager entityManager = BackendConfig.getEntityManagerFactory().createEntityManager();
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        try {

            BooleanQuery.Builder qb = new BooleanQuery.Builder();

            if (ssr.getKeywords() != null) {
                QueryParser queryParser = new QueryParser(SearchMapper.titleField, new StandardAnalyzer());

                qb.add(queryParser.parse(ssr.getKeywords()), BooleanClause.Occur.MUST);
            }

            if (u.isSuperuser() || BackendConfig.getServiceManager().getSecurityManager()
                    .mayUserListAllSubmissions(u)) {
                if (ssr.getOwnerId() > 0) {
                    qb.add(NumericRangeQuery
                            .newLongRange(SearchMapper.ownerField + '.' + SearchMapper.numidField, ssr.getOwnerId(),
                                    ssr.getOwnerId(), true, true), BooleanClause.Occur.MUST);
                }
            } else {
                qb.add(NumericRangeQuery
                        .newLongRange(SearchMapper.ownerField + '.' + SearchMapper.numidField, u.getId(), u.getId(),
                                true, true), BooleanClause.Occur.MUST);
            }

            if (ssr.getOwner() != null) {
                TermQuery tq = new TermQuery(
                        new Term(SearchMapper.ownerField + '.' + SearchMapper.emailField, ssr.getOwner()));
                qb.add(tq, BooleanClause.Occur.MUST);
            }

            if (ssr.getAccNo() != null) {
                WildcardQuery tq = new WildcardQuery(new Term(SearchMapper.accNoField, ssr.getAccNo()));
                qb.add(tq, BooleanClause.Occur.MUST);
            }

            if (ssr.getFromVersion() > Integer.MIN_VALUE || ssr.getToVersion() < Integer.MAX_VALUE) {
                qb.add(NumericRangeQuery
                                .newIntRange(SearchMapper.versionField, ssr.getFromVersion(), ssr.getToVersion(),
                                        true, false),
                        BooleanClause.Occur.MUST);
            } else {
                qb.add(NumericRangeQuery.newIntRange(SearchMapper.versionField, 0, Integer.MAX_VALUE, true, false),
                        BooleanClause.Occur.MUST);
            }

            long from, to;
            String field;

            from = ssr.getFromCTime();
            to = ssr.getToCTime();
            field = SearchMapper.cTimeField;

            if (from > Long.MIN_VALUE || to < Long.MAX_VALUE) {
                qb.add(NumericRangeQuery.newLongRange(field, from, to, true, true), BooleanClause.Occur.MUST);
            }

            from = ssr.getFromMTime();
            to = ssr.getToMTime();
            field = SearchMapper.mTimeField;

            if (from > Long.MIN_VALUE || to < Long.MAX_VALUE) {
                qb.add(NumericRangeQuery.newLongRange(field, from, to, true, true), BooleanClause.Occur.MUST);
            }

            from = ssr.getFromRTime();
            to = ssr.getToRTime();
            field = SearchMapper.rTimeField;

            if (from > Long.MIN_VALUE || to < Long.MAX_VALUE) {
                qb.add(NumericRangeQuery.newLongRange(field, from, to, true, true), BooleanClause.Occur.MUST);
            }

            Sort sort = null;

            if (ssr.getSortBy() != null) {
                switch (ssr.getSortBy()) {
                    case CTime:
                        sort = new Sort(new SortField(SearchMapper.cTimeField, SortField.Type.LONG, true));
                        break;

                    case MTime:
                        sort = new Sort(new SortField(SearchMapper.mTimeField, SortField.Type.LONG, true));
                        break;

                    case RTime:
                        sort = new Sort(new SortField(SearchMapper.rTimeField, SortField.Type.LONG, true));
                        break;
                }
            }

            org.apache.lucene.search.Query q = qb.build();

            FullTextQuery query = fullTextEntityManager.createFullTextQuery(q, Submission.class);

            if (sort != null) {
                query.setSort(sort);
            }

            if (ssr.getSkip() > 0) {
                query.setFirstResult(ssr.getSkip());
            }

            if (ssr.getLimit() > 0) {
                query.setMaxResults(ssr.getLimit());
            }

            res = query.getResultList();

        } finally {
            if (fullTextEntityManager != null) {
                fullTextEntityManager.close();
            }

            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }

        return res;
    }

    public Collection<Submission> getSubmissionsByOwner(User u, int offset, int limit) {
        EntityManager manager = BackendConfig.getServiceManager().getSessionManager().getEntityManager();
        EntityTransaction transaction = manager.getTransaction();

        try {

            transaction.begin();
            TypedQuery<Submission> query = manager.
                    createNamedQuery(Submission.GetByOwnerQuery, Submission.class)
                    .setParameter("uid", u.getId());

            if (offset > 0) {
                query.setFirstResult(offset);
            }

            if (limit > 0) {
                query.setMaxResults(limit);
            }

            return query.getResultList();

        } catch (Throwable t) {
            log.error("DB error query submissions for owner: " + t.getMessage());
        } finally {
            DatabaseUtil.commitIfActiveAndNotNull(transaction);
        }

        return null;
    }

    public Submission getSubmissionsByAccession(String acc) {
        EntityManager manager = BackendConfig.getServiceManager().getSessionManager().getEntityManager();
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

    public List<Submission> getHostSubmissionsByType(String type, User user) {
        EntityManager manager = BackendConfig.getServiceManager().getSessionManager().getSession()
                .getEntityManager();

        if (user.isSuperuser()) {
            return manager.createQuery(GET_ALL_HOST, Submission.class)
                    .setParameter("type", type)
                    .getResultList();
        }

        List<AccessTag> tags = manager.createQuery(ACCESS_TAG_QUERY, AccessTag.class).getResultList();
        List<Long> allowedTags = getAllowedTags(tags, user);

        return allowedTags.isEmpty() ? Collections.emptyList()
                : manager.createQuery(GET_HOST_SUB_BY_TYPE_QUERY, Submission.class)
                        .setParameter("type", type)
                        .setParameter("allow", allowedTags).getResultList();
    }

    private List<Long> getAllowedTags(List<AccessTag> tags, User user) {
        return tags.stream()
                .filter(tag -> tag.checkDelegatePermission(ATTACHSUBM, user) == ALLOW)
                .map(AccessTag::getId)
                .collect(Collectors.toList());
    }
}
