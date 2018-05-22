package org.eurecat.bda.hatch.plugin.rest;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;
import org.elasticsearch.rest.action.RestBuilderListener;
import org.eurecat.bda.hatch.plugin.simple.SimpleAction;
import org.eurecat.bda.hatch.plugin.simple.SimpleRequest;
import org.eurecat.bda.hatch.plugin.simple.SimpleResponse;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;

public class RestSimpleAction extends BaseRestHandler {
    @Inject
    public RestSimpleAction(Settings settings, Client client, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/_simple", this);
        controller.registerHandler(POST, "/{index}/_simple", this);
        controller.registerHandler(POST, "/_simple/{field}", this);
        controller.registerHandler(GET, "/_simple", this);
        controller.registerHandler(GET, "/{index}/_simple", this);
        controller.registerHandler(GET, "/_simple/{field}", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        final SimpleRequest simpleRequest = new SimpleRequest(Strings.splitStringByCommaToArray(request.param("index")));
        simpleRequest.setField(request.param("field"));
        return channel -> client.execute(SimpleAction.INSTANCE, simpleRequest, new RestBuilderListener<SimpleResponse>(channel){
            @Override
            public RestResponse buildResponse(SimpleResponse simpleResponse, XContentBuilder builder) throws Exception {
                try {
                    builder.startObject();
                    builder.field("ok", true);
                    builder.array("terms", simpleResponse.getSimple().toArray());
                    builder.endObject();

                } catch (Exception e) {
                    onFailure(e);
                }
                return new BytesRestResponse(OK, builder);
            }
        });
    }
}
