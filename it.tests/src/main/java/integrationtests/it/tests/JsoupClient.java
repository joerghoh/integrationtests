package integrationtests.it.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.adobe.cq.testing.client.CQClient;

public class JsoupClient extends CQClient {
    

    public JsoupClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public Document getPage(String path) throws ClientException {
        
        HttpGet get = new HttpGet(getUrl().toString() + path);
        SlingHttpResponse response = doRequest(get, null, 200);
        return getPage(response);
        
    }
    
    public Document getPage(SlingHttpResponse response) {
        return Jsoup.parse(response.getContent());
    }
    
    
    public List<URI> getReferencedResources (Document document) {
        
        List<URI> result = new ArrayList<>();
        result.addAll(extractResource(document,"script", "src"));
        result.addAll(extractResource(document,"img", "src"));
        result.addAll(extractResource(document,"meta", "href"));
        result.addAll(extractResource(document,"link", "href"));
        
        result.addAll(getCoreComponentImageRenditions(document));
        return result;
        
    }
    
    
    
    
    private List<URI> extractResource(Document doc, String tag, String attributeName) {
        List<URI> result = new ArrayList<>();
        Elements tags = doc.getElementsByTag(tag);
        return tags.stream()
            .map(t -> t.attr(attributeName))
            .map(s -> resolve(s))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    
    
    /**
     * Extract all image core components and their references from the page.
     * @param page the page to scan
     * @return all renditions of all image core components
     * @throws URISyntaxException
     */
    private List<URI> getCoreComponentImageRenditions (Document doc) {
        List<URI> result = new ArrayList<>();

        // detect images core components based on the CSS class name
        Elements images = doc.getElementsByClass("cmp-image");
        images.forEach(i -> {
            String src = null;
            String width = null;
            
            if (i.hasAttr("data-cmp-src") && i.attr("data-cmp-src") != null) {
                src = i.attr("data-cmp-src");
            }
            if (i.hasAttr("data-cmp-widths") && i.attr("data-cmp-widths") != null) {
                width = i.attr("data-cmp-widths");
            }
            if (src != null && width != null) {
                String[] widths = width.split(",");
                for (String w: widths) {
                    String ref = src.replace("{.width}", "."+w);
                    result.add(resolve(ref));
                }
            } else if (src != null && width == null) {
                // happens with SVG and GIFs
                String ref = src.replace("{.width}", "");
                result.add(resolve(ref));
            }
        });
        return result;
    }
    
    private URI resolve (String path) {
        try {
            if (path != null) {
                return getUrl().resolve(path);
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("Cannot resolve path {}: {}", path, e.getMessage());
        }
        return null;
    }
    
}
