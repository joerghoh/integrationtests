package integrationtests.it.tests;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.adobe.cq.testing.junit.rules.CQAuthorPublishClassRule;

/**
 * Test the redirects on publish
 *
 */
public class PublishRedirectsIT {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AuthorHomepageValidationIT.class);
    
    static int[] ALL_STATUSCODES = new int[1000];
    static {
        for (int i=0; i < 1000; i++) {
            ALL_STATUSCODES[i] = i+100; 
        }
    }

    @ClassRule
    public static CQAuthorPublishClassRule cqBaseClassRule = new CQAuthorPublishClassRule();
    

    private static RequestValidationClient anonymouspublish;

    @BeforeClass
    public static void beforeClass() throws ClientException {
        URI baseUrl = cqBaseClassRule.publishRule.getAdminClient().getUrl();
        anonymouspublish = RequestValidationClient.Builder.create(baseUrl, null, null).setUserAgent("integrationtest").build();
        
    }

    @AfterClass
    public static void afterClass() {
        closeQuietly(anonymouspublish);
    }
    
    @Test
    public void testInitialRedirectAndHomepage() throws ClientProtocolException, IOException, ClientException {
        assertPermanentRedirect("/", "/content/wknd/us/en.html");
        assertStatuscode( 200, "/us/en.html");
    }
    
    @Test
    public void testBlockedUrls() throws ClientException {
        assertStatuscode(404, "/system/console");
    }
    
    @Test
    public void testInfinityJSON() throws ClientException {
        assertStatuscode(404, "/content/wknd/us/en.1.json");
        assertStatuscode(404, "/content/wknd/us/en.-1.json");
        assertStatuscode(404, "/content/wknd/us/en.infinity.json");
    }
    
    
    private void assertStatuscode (int expectedStatus, String from) throws ClientException {
        SlingHttpResponse response = anonymouspublish.doGet(from, ALL_STATUSCODES);
        assertEquals("Request to ["+from+"] did not return the expected status code: ",expectedStatus,response.getStatusLine().getStatusCode());
    }
    
    private void assertPermanentRedirect(String from, String to) throws  ClientException {
        SlingHttpResponse response = anonymouspublish.doGet(from, ALL_STATUSCODES);
        assertEquals("request to ["+from+"] did not return a permanent redirect: ", HttpServletResponse.SC_MOVED_PERMANENTLY,response.getStatusLine().getStatusCode());
        assertTrue("request to ["+from+"] did not redirect to the expected location: ", response.getLastHeader("Location").getValue().endsWith(to));
    }
    
    


    
    
    


}
