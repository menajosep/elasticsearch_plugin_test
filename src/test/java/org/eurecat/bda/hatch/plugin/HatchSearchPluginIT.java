package org.eurecat.bda.hatch.plugin;

import org.assertj.core.api.Assertions;
import org.carrot2.core.LanguageCode;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkAddress;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Before;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class HatchSearchPluginIT extends ESIntegTestCase {

    protected String restBaseUrl;
    protected Client client;

    protected static final String INDEX_TEST = "test";
    protected static final String INDEX_EMPTY = "empty";

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder().put(super.nodeSettings(nodeOrdinal))
                .build();
    }

    @Override
    protected Collection<Class<? extends Plugin>> transportClientPlugins() {
        return nodePlugins();
    }

    /*@Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(HatchSearchPlugin.class);
    }*/

    @Before
    public void createTestIndex() throws Exception {
        client = client();
        if (!client.admin().indices().prepareExists(INDEX_TEST).get().isExists()) {
            String testTemplate =
                    "{" +
                            "  \"test\": {" +
                            "    \"properties\": {" +
                            "      \"url\": { \"type\": \"text\" }," +
                            "      \"title\": { \"type\": \"text\" }," +
                            "      \"content\": { \"type\": \"text\" }," +
                            "      \"lang\": { \"type\": \"text\" }," +
                            "      \"rndlang\": { \"type\": \"text\" }" +
                            "    }" +
                            "  }" +
                            "}";

            String emptyTemplate =
                    "{" +
                            "  \"empty\": {" +
                            "    \"properties\": {" +
                            "      \"url\": { \"type\": \"text\" }," +
                            "      \"title\": { \"type\": \"text\" }," +
                            "      \"content\": { \"type\": \"text\" }," +
                            "      \"lang\": { \"type\": \"text\" }," +
                            "      \"rndlang\": { \"type\": \"text\" }" +
                            "    }" +
                            "  }" +
                            "}";

            CreateIndexResponse response = client.admin().indices()
                    .prepareCreate(INDEX_TEST)
                    .addMapping("test", testTemplate, XContentType.JSON)
                    .get();
            Assertions.assertThat(response.isAcknowledged()).isTrue();

            response = client.admin().indices()
                    .prepareCreate(INDEX_EMPTY)
                    .addMapping("empty", emptyTemplate, XContentType.JSON)
                    .get();
            Assertions.assertThat(response.isAcknowledged()).isTrue();
            //InetSocketAddress endpoint = randomFrom(cluster().httpAddresses());
            //this.restBaseUrl = "http://" + NetworkAddress.format(endpoint);
            // Create content at random in the test index.
            Random rnd = random();
            LanguageCode[] languages = LanguageCode.values();
            Collections.shuffle(Arrays.asList(languages), rnd);

            BulkRequestBuilder bulk = client.prepareBulk();
            for (String[] data : SampleDocumentData.SAMPLE_DATA) {
                bulk.add(client.prepareIndex()
                        .setIndex(INDEX_TEST)
                        .setType("test")
                        .setSource(XContentFactory.jsonBuilder()
                                .startObject()
                                .field("url",     data[0])
                                .field("title",   data[1])
                                .field("content", data[2])
                                .field("lang", LanguageCode.ENGLISH.getIsoCode())
                                .field("rndlang", languages[rnd.nextInt(languages.length)].getIsoCode())
                                .endObject()));
            }

            bulk.add(client.prepareIndex()
                    .setIndex(INDEX_EMPTY)
                    .setType("empty")
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("url",     "")
                            .field("title",   "")
                            .field("content", "")
                            .endObject()));

            bulk.execute().actionGet();
            flushAndRefresh(INDEX_TEST);
            flushAndRefresh(INDEX_EMPTY);
        }
    }

    public void testDiversityRescorer() throws IOException {
        System.out.println("test");
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(20);
        searchRequest.source(searchSourceBuilder);
        ActionListener<SearchResponse> listener = new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                List<FieldMappingSpec> fieldMapping = new ArrayList<>();
                fieldMapping.add(new FieldMappingSpec("title", LogicalField.TITLE, FieldSource.SOURCE));
                float diversityFactor = 0.7f;
                DiversityRescorer rescorer = new LevenshteinDiversityRescorer(fieldMapping, diversityFactor);
                rescorer.rescore(searchResponse);
                assertNotNull(searchResponse);
            }

            @Override
            public void onFailure(Exception e) {
                fail();
            }
        };
        client.search(searchRequest, listener);
    }
}

