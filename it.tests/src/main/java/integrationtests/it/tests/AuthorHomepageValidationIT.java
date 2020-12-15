/*
 *  Copyright 2020 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integrationtests.it.tests;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.junit.rules.CQAuthorClassRule;
import com.adobe.cq.testing.junit.rules.CQRule;

/**
 * Performs basic validation on the WKND page on author
 * 
 */
public class AuthorHomepageValidationIT {


    // use the WKND english master as test item
    private static final String HOMEPAGE = "/content/wknd/language-masters/en.html";
    
    private static final String[] ZEROBYTEFILES = new String[] {
            "/etc.clientlibs/wcm/foundation/clientlibs/main.min.js"
            };

    private static final List<String> ZEROBYTE_CLIENTLIBS = Arrays.asList(ZEROBYTEFILES);

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AuthorHomepageValidationIT.class);

    @ClassRule
    public static CQAuthorClassRule cqBaseClassRule = new CQAuthorClassRule(true);

    private static JsoupClient adminAuthor;

    @BeforeClass
    public static void beforeClass() throws ClientException {

        adminAuthor = cqBaseClassRule.authorRule.getAdminClient(CQClient.class).adaptTo(JsoupClient.class);
    }

    @AfterClass
    public static void afterClass() {
        closeQuietly(adminAuthor);
    }



    // verify that the homepage is rendered correctly and all links are working
    @Test
    public void validateHomepage() throws ClientException {
        Document validatedPage = loadPage(adminAuthor, HOMEPAGE);
        verifyLinkedResources(adminAuthor,validatedPage);

    }


    private static Document loadPage (JsoupClient client, String path) throws ClientException  {
        URI baseURI = client.getUrl();
        HttpGet get = new HttpGet(baseURI.toString() + path);
        SlingHttpResponse validationResponse = client.doRequest(get,null,200);
        assertEquals("Request to [" + get.getURI().toString() + "] does not return expected returncode 200",
                200, validationResponse.getStatusLine().getStatusCode());
        Document homepage = client.getPage(validationResponse);
        assertEquals("Unexpected page title","WKND Adventures and Travel",homepage.getElementsByTag("title").text());
        return homepage;
    }

    private static void verifyLinkedResources(JsoupClient client, Document doc) throws ClientException {

        
        List<URI> references = client.getReferencedResources(doc);
        assertTrue("document does not contain any references!", references.size() > 0);
        for (URI ref : references ) {
            if (isSameOrigin(client.getUrl(), ref)) {
                SlingHttpResponse response = client.doGet(ref.getRawPath());
                int statusCode = response.getStatusLine().getStatusCode();
                int responseSize = response.getContent().length();
                assertEquals("Unexpected status returned from [" + ref + "]", 200, statusCode);
                if (! ZEROBYTE_CLIENTLIBS.stream().allMatch(s -> s.startsWith(ref.getPath()))) {
                    assertTrue("Empty response body from [" + ref + "]", responseSize > 0);
                }
            } else {
                LOG.info("skipping linked resource from another domain: {}", ref.toString());
            }
        }
        LOG.info("validated {} linked resources", references.size());
    }

    /** Checks if two URIs have the same origin.
     *
     * @param uri1 first URI
     * @param uri2 second URI
     * @return true if two URI come from the same host, port and use the same scheme
     */
    private static boolean isSameOrigin(URI uri1, URI uri2) {
        if (!uri1.getScheme().equals(uri2.getScheme())) {
            return false;
        } else return uri1.getAuthority().equals(uri2.getAuthority());
    }


}
