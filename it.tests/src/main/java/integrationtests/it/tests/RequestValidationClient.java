package integrationtests.it.tests;

import java.net.URI;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingClientConfig;



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
    
    public final static class Builder extends InternalBuilder<RequestValidationClient> {

        protected Builder(URI url, String user, String password) {
            super(url, user, password);
        }

        @Override
        public RequestValidationClient build() throws ClientException {
            this.httpClientBuilder().disableRedirectHandling();
            return new RequestValidationClient(buildHttpClient(),buildSlingClientConfig());
        }
     
        public static Builder create (URI url, String user, String password) {
            return new Builder(url,user,password);
        }
        
        public Builder setUserAgent (String useragent) {
            this.httpClientBuilder().setUserAgent(useragent);
            return this;
        }
        
        
    }
    
    
    
}
