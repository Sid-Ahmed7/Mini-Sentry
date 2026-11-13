package com.sentry.backend.dto.request;

import java.util.List;
import java.util.Map;


public record EventRequest(String type, String message, String platform, String release, String environment, String url, Long timestamp, List<FrameRequest> frames, Map<String, String> tags, List<BreadcrumbRequest> breadcrumbs){

    public List<FrameRequest> allFrames() {
        return frames == null ? List.of() : frames;
    }

    public Map<String, String> allTags() {
        return tags == null ? Map.of() : tags;
    }

    public List<BreadcrumbRequest> allBreadcrumb() {
        return breadcrumbs == null ? List.of() : breadcrumbs;
    }
    
}
