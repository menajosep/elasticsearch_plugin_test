package org.eurecat.bda.hatch.plugin;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchWrapperException;
import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

/**
 * Generic exception implementing {@link ElasticsearchWrapperException}
 */
@SuppressWarnings("serial")
public class HatchSearchException extends ElasticsearchException implements ElasticsearchWrapperException {

    public HatchSearchException(Throwable cause) {
        super(cause);
    }

    public HatchSearchException(String msg, Object... args) {
        super(msg, args);
    }

    public HatchSearchException(String msg, Throwable cause, Object... args) {
        super(msg, cause, args);
    }

    public HatchSearchException(StreamInput in) throws IOException {
        super(in);
    }
}
