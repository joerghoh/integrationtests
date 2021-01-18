package integrationtests.it.tests;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingClientConfig;



/**
 * A customized Test client, which is designed to validate individual aspects of a HTTP request; especially
 * it does not automatically follow redirects, also the user agent can be customized. Use the Builder inner class
 * to create an instance of this.
 *
 */

public class RequestValidationClient extends SlingClient {

    public RequestValidationClient(URI url, String user, String password) throws ClientException {
        super(url, user, password);
        // TODO Auto-generated constructor stub
    }
    
    public RequestValidationClient(CloseableHttpClient httpClient, SlingClientConfig slingClientConfig) throws ClientException {
        super(httpClient,slingClientConfig);
    }
    
    
    
    


    public static abstract class InternalBuilder<T extends RequestValidationClient> extends SlingClient.InternalBuilder<T> {
        
        protected InternalBuilder(URI url, String user, String password) {
            super(url,user,password);
        }
        
    }
    
    /**
     * The builder class, contains the relevant customizations to the http client
     *
     */
    
    public final static class Builder extends InternalBuilder<RequestValidationClient> {
        
        Collection<Header> customHeaders = new HashSet<Header>();

        protected Builder(URI url, String user, String password) {
            super(url, user, password);
        }
        
        //  static builder
        public static Builder create (URI url, String user, String password) {
            return new Builder(url,user,password);
        }

        @Override
        public RequestValidationClient build() throws ClientException {
            this.httpClientBuilder().disableRedirectHandling();
            this.httpClientBuilder().setDefaultHeaders(customHeaders);
            return new RequestValidationClient(buildHttpClient(),buildSlingClientConfig());
        }
     

        /**
         * Set a custom user agent
         * @param useragent the new useragent string
         * @return the builder
         */
        public Builder setUserAgent (String useragent) {
            this.httpClientBuilder().setUserAgent(useragent);
            return this;
        }
        
        /**
         * set the default host header
         * @param host the value of the host header
         * @return the builder
         */
        public Builder setHostHeader(String host) {
            Header h = new BasicHeader(HttpHeaders.HOST, host);
            customHeaders.add(h);
            return this;
            
        }
        
    }
    
    
    
}
