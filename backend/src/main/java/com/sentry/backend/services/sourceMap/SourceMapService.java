package com.sentry.backend.services.sourceMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.sentry.backend.common.CurrentLocale;
import com.sentry.backend.dto.request.EventRequest;
import com.sentry.backend.dto.request.FrameRequest;
import com.sentry.backend.processing.SourceMapDecoder;
import com.sentry.backend.processing.message.FilePaths;
import com.sentry.backend.repository.SourceMapRepository;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service 
public class SourceMapService implements ISourceMapService {


    private final MessageSource messageSource;
    private final SourceMapRepository sourceMapRepository;
    private final JsonMapper mapper;

    private final Map<String, Optional<SourceMapDecoder>> cache = Collections.synchronizedMap(new LinkedHashMap<String, Optional<SourceMapDecoder>>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Optional<SourceMapDecoder>> eldEntry) {
                return size() > 65;
            }
    });


    public SourceMapService(MessageSource messageSource, SourceMapRepository sourceMapRepository, JsonMapper mapper) {
        this.messageSource = messageSource;
        this.sourceMapRepository = sourceMapRepository;
        this.mapper = mapper;
    }


    @Override
    public void validate(String json) {
        parse(json);
    }

    @Override
    public void invalidate(UUID projectId, String release, String file) {
        cache.remove(key(projectId, release, file));
    }

    @Override
    public EventRequest apply(UUID projectId, EventRequest event) {
        
        if (event.release() == null || !"javascript".equals(event.platform())) {
            return event;
        }

     List<FrameRequest> decoded = new ArrayList<>();
        for (FrameRequest frame : event.allFrames()) {
            decoded.add(decode(projectId, event.release(), frame));
        }
        return new EventRequest(event.type(), event.message(), event.platform(), event.release(), event.environment(), event.url(), event.timestamp(), decoded, event.tags(), event.breadcrumbs());
    }

    private FrameRequest decode(UUID projectId, String release, FrameRequest frame) {
        
        if (frame.file() == null || frame.line() == null || frame.column() == null) {
            return frame;
        }

        String base = FilePaths.baseName(frame.file());
        
        if (base.isEmpty()) {
            return frame;
        }

        Optional<SourceMapDecoder> decoder = load(projectId, release, base);
        
        if (decoder.isEmpty()) {
            return frame;
        }
        
        return decoder.get().lookup(frame.line(), frame.column()).map(originalPosition -> new FrameRequest(originalPosition.name() != null ? originalPosition.name() : frame.function(), originalPosition.source(), originalPosition.line(), originalPosition.col(), null)).orElse(frame);
    }

    private Optional<SourceMapDecoder> load(UUID projectId, String release, String file) {
        return cache.computeIfAbsent(key(projectId, release, file), k -> sourceMapRepository.findByProjectIdAndAppReleaseAndFile(projectId, release, file).<SourceMapDecoder>flatMap(sm -> {
                    try {
                        return Optional.of(parse(sm.getContent()));
                    } catch (RuntimeException e) {
                        log.warn("Source map illisible pour {} ({})", file, e.getClass().getSimpleName());
                        return Optional.empty();
                    }
                }));
    }

    private SourceMapDecoder parse(String json) {
        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (JacksonException e) {
            throw new IllegalArgumentException(messageSource.getMessage("sourcemap.json.invalid", null, CurrentLocale.getCurrentLocale()), e);
        }
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException(messageSource.getMessage("sourcemap.notSourceMap", null, CurrentLocale.getCurrentLocale()));
        }
        JsonNode mappings = root.get("mappings");
        JsonNode sourceList = root.get("sources");
        if (mappings == null || !mappings.isString() || sourceList == null || !sourceList.isArray()) {
            throw new IllegalArgumentException(messageSource.getMessage("sourcemap.notSourceMap", null, CurrentLocale.getCurrentLocale()));
        }
        JsonNode version = root.get("version");
        if (version != null && !(version.isInt() && version.intValue() == 3)) {
            throw new IllegalArgumentException(messageSource.getMessage("sourcemap.version.unsupported", null, CurrentLocale.getCurrentLocale()));
        }
        String sourceRoot = text(root.get("sourceRoot"));
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < sourceList.size(); i++) {
            sources.add(cleanSource(sourceRoot + text(sourceList.get(i))));
        }
        List<String> names = new ArrayList<>();
        JsonNode nameList = root.get("names");
        if (nameList != null && nameList.isArray()) {
            for (int i = 0; i < nameList.size(); i++) {
                names.add(text(nameList.get(i)));
            }
        }
        return new SourceMapDecoder(messageSource, mappings.asString(), sources, names);
    }

    private static String text(JsonNode node) {
        return node != null && node.isString() ? node.asString() : "";
    }

    static String cleanSource(String s) {
        String res = s.replaceFirst("^webpack:///?", "").replaceFirst("^[a-z]+://", "");
        res = res.replaceAll("^(\\./|\\.\\./)+", "");
        int q = res.indexOf('?');
        if (q >= 0) {
            res = res.substring(0, q);
        }
        return res.length() > 300 ? res.substring(0, 300) : res;
    }

    private static String key(UUID projectId, String release, String file) {
        return projectId + "|" + release + "|" + file;
    }
}
