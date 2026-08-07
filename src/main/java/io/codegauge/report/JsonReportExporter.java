package io.codegauge.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.nio.file.Path;

/**
 * {@link ReportExporter} producing pretty-printed JSON via Jackson.
 *
 * <p>{@code core} domain records are serialized directly — Jackson infers
 * field names from record component names with no annotations needed
 * (supported natively since Jackson 2.12), so {@code core} stays free of
 * any Jackson dependency or import. The one adaptation needed is
 * {@link Path}, which Jackson would otherwise try to serialize as a bean
 * (exposing {@code getFileSystem()}, {@code getRoot()}, etc.); a
 * {@link ToStringSerializer} is registered so paths serialize as plain
 * strings instead.
 */
public final class JsonReportExporter implements ReportExporter {

    private final ObjectMapper mapper;

    public JsonReportExporter() {
        SimpleModule pathModule = new SimpleModule();
        pathModule.addSerializer(Path.class, ToStringSerializer.instance);

        this.mapper = new ObjectMapper()
                .registerModule(pathModule)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String export(RepositoryReport report) {
        try {
            return mapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new ReportExportException("Failed to serialize report as JSON", e);
        }
    }
}