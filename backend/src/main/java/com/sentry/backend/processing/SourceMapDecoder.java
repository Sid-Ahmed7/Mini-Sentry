package com.sentry.backend.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sentry.backend.common.CurrentLocale;
import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.processing.sourceMap.OriginalPosition;

import org.springframework.context.MessageSource;

public class SourceMapDecoder {

    private final MessageSource messageSource;
    private final List<String> sources;
    private final List<String> names;
    private final int[][][] lines;

    public SourceMapDecoder(MessageSource messageSource, String mappings, List<String> sources, List<String> names) {
        this.messageSource = messageSource;
        this.sources = sources;
        this.names = names;

        String[] lineStrings = mappings.split(";", -1);
        this.lines = new int[lineStrings.length][][];

        int sourceIndex = 0;
        int sourceLine = 0;
        int sourceCol = 0;
        int nameIndex = 0;

        for (int i = 0; i < lineStrings.length; i++) {
            String lineString = lineStrings[i];
            if (lineString.isEmpty()) {
                lines[i] = new int[0][];
                continue;
            }
            List<int[]> out = new ArrayList<>();
            int genCol = 0;
            for (String seg : lineString.split(",")) {
                if (seg.isEmpty()) {
                    continue;
                }
                int[] v = decodeVlq(seg);
                genCol += v[0];
                if (v.length >= 4) {
                    sourceIndex += v[1];
                    sourceLine += v[2];
                    sourceCol += v[3];
                    int name = -1;
                    if (v.length >= 5) {
                        nameIndex += v[4];
                        name = nameIndex;
                    }
                    out.add(new int[]{genCol, sourceIndex, sourceLine, sourceCol, name});
                }
            }
            lines[i] = out.toArray(new int[0][]);
        }
    }

    public Optional<OriginalPosition> lookup(int line, int col) {
        int lineIdx = line - 1;
        int colIdx = col - 1;
        if (lineIdx < 0 || lineIdx >= lines.length || colIdx < 0) {
            return Optional.empty();
        }
        int[][] segments = lines[lineIdx];
        int low = 0;
        int high = segments.length - 1;
        int best = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (segments[mid][0] <= colIdx) {
                best = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if (best < 0) {
            return Optional.empty();
        }
        int[] segment = segments[best];
        if (segment[1] < 0 || segment[1] >= sources.size()) {
            return Optional.empty();
        }
        String name = (segment[4] >= 0 && segment[4] < names.size()) ? names.get(segment[4]) : null;
        return Optional.of(new OriginalPosition(sources.get(segment[1]), segment[2] + 1, segment[3] + 1, name));
    }

    int[] decodeVlq(String segment) {
        List<Integer> values = new ArrayList<>(5);
        int shift = 0;
        int value = 0;
        for (int i = 0; i < segment.length(); i++) {
            int digit = CommonConstants.BASE64.indexOf(segment.charAt(i));
            if (digit < 0) {
                throw new IllegalArgumentException(messageSource.getMessage("sourcemap.vlq.invalidChar", null, CurrentLocale.getCurrentLocale()));
            }
            value |= (digit & 31) << shift;
            if ((digit & 32) != 0) {
                shift += 5;
                if (shift > 30) {
                    throw new IllegalArgumentException(messageSource.getMessage("sourcemap.vlq.tooLarge", null, CurrentLocale.getCurrentLocale()));
                }
            } else {
                int magnitude = value >>> 1;
                values.add((value & 1) == 1 ? -magnitude : magnitude);
                value = 0;
                shift = 0;
            }
        }
        int[] out = new int[values.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = values.get(i);
        }
        return out;
    }
}
