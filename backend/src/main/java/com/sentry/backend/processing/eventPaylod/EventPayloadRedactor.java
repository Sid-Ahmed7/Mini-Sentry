package com.sentry.backend.processing.eventPaylod;

import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.dto.request.BreadcrumbRequest;
import com.sentry.backend.dto.request.EventRequest;
import com.sentry.backend.processing.SensitiveData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventPayloadRedactor {

    public static EventRequest scrub(EventRequest event) {
        return new EventRequest(
            event.type(),
            SensitiveData.scrubText(event.message()),
            event.platform(),
            event.release(),
            event.environment(),
            SensitiveData.scrubText(SensitiveData.stripQuery(event.url())),
            event.timestamp(),
            event.frames(),
            scrubTags(event.allTags()),
            scrubBreadcrumbs(event.allBreadcrumb())
        );
    }

    private static Map<String, String> scrubTags(Map<String, String> tags) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            result.put(tag.getKey(), CommonConstants.SENSITIVE_KEY.matcher(tag.getKey()).find() ? CommonConstants.REDACTED : SensitiveData.scrubText(tag.getValue()));
        }
        return result;
    }

    private static List<BreadcrumbRequest> scrubBreadcrumbs(List<BreadcrumbRequest> breadcrumbs) {
        return breadcrumbs.stream().map(breadcrumb -> new BreadcrumbRequest(breadcrumb.timestamp(), SensitiveData.scrubText(breadcrumb.message()))).toList();
    }
}

