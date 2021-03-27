package integrationtests.it.tests;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.codehaus.jackson.JsonNode;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.ReplicationClient;
import com.adobe.cq.testing.junit.rules.CQAuthorPublishClassRule;
import com.adobe.cq.testing.junit.rules.Page;

/**
 * This test runs the ootb ActivatePage workflow on author and tests,
 * if the page reaches the publish instance.
 *
 */
public class ActivatePageIT {
    
    private static final Logger LOG = LoggerFactory.getLogger(ActivatePageIT.class);

    private static final String AUDIT_LOG_REPLICATION_ROOT = "/var/audit/com.day.cq.replication";
    
    @ClassRule
    public static CQAuthorPublishClassRule cqBaseClassRule = new CQAuthorPublishClassRule();
    
    private static CQClient adminAuthor;
    private static CQClient adminPublish;
    private static ReplicationClient replicationClient;
    
    @Rule
    public Page page = new RootPage(cqBaseClassRule.authorRule,"/content");


    @BeforeClass
    public static void setup() throws ClientException {
        adminAuthor = cqBaseClassRule.authorRule.getAdminClient(CQClient.class);
        adminPublish = cqBaseClassRule.publishRule.getAdminClient(CQClient.class);
        replicationClient = adminAuthor.adaptTo(ReplicationClient.class);
    }
    
    @AfterClass
    public static void afterClass() {
        closeQuietly(adminAuthor);
        closeQuietly(adminPublish);
        closeQuietly(replicationClient);
    }
    
    @Test
    public void replicatePage() throws ClientException, TimeoutException, InterruptedException {
        
        String pagePath = page.getPath();
        
        // validate that the page is not present on publish
        adminPublish.doGet(pagePath, 403,404);
        
        
        LOG.info("Activate path: {}", pagePath);
        replicationClient.activate(pagePath, 200);
        adminPublish.waitExists(pagePath, 10000, 1000);
        LOG.info("page {} exists on publish", pagePath);
        
        // validate that there is an audit entry for this activation
        List<JsonNode> auditEntries = getAuditEntries(adminAuthor, pagePath);
        assertEquals(1, auditEntries.size());
        assertEquals("Activate", auditEntries.get(0).get("cq:type").getValueAsText());
        
        LOG.info("Deactivate page {} again", pagePath);
        replicationClient.deactivate(pagePath, 200);
        adminPublish.doGet(pagePath, 403,404);
        auditEntries = getAuditEntries(adminAuthor,pagePath);
        assertEquals(2, auditEntries.size());
        assertTrue(auditEntries.stream().anyMatch(node -> {
            return node.get("cq:type").getValueAsText().equals("Deactivate");
        }));
        
    }
    
    static List<JsonNode> getAuditEntries (CQClient client, String contentPath) throws ClientException {
        List<JsonNode> result = new ArrayList<>();
        String auditPath = AUDIT_LOG_REPLICATION_ROOT + contentPath;
        JsonNode auditEntries = adminAuthor.doGetJson(auditPath, -1, 200);
        auditEntries.getElements().forEachRemaining(node -> {
            if (node.isContainerNode()) {
                result.add(node);
            }
        });
        return result;
    }
    
    
    
    public class RootPage extends Page {
        
        String parentPath;
        
        public RootPage(Instance rule, String parentPath) {
            super(rule);
            this.parentPath = parentPath;
        }
        
        @Override
        protected String initialParentPath() {
            return parentPath;
        }
    }
    
}
