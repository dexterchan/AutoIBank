package io.exp.analysis.match.beam.pipeline.check;

import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.bson.Document;

@Slf4j
public class CheckSecurityDocument implements SerializableFunction<Iterable<Document>, Void> {
    @Override
    public Void apply(Iterable<Document> input) {
        input.forEach(
                document->{
                    log.debug(document.toJson());
                }
        );
        return null;
    }
}
