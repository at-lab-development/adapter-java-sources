package com.epam.jira.junit;

import com.epam.jira.entity.Issue;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.FrameworkMethod;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class SkipAnalyzer {

    private final Map<String, Issue> failedMethods = new HashMap<>();
    private final Map<String, List<Issue>> failedGroups = new HashMap<>();
    private final List<String> failedConfigs = new ArrayList<>();

    void addFailedConfig(FrameworkMethod method) {
        Annotation[] annotations = method.getMethod().getDeclaredAnnotations();
        String annotationInfo = annotationsToString(annotations);
        failedConfigs.add(JunitUtils.getFullMethodName(method) + annotationInfo);
    }

    void addFailedResult(Failure result, Issue issue) {

        failedMethods.put(result.getMessage(), issue);

    }

    String getLastFailedConfig() {
        return failedConfigs.isEmpty() ? null : failedConfigs.get(failedConfigs.size() - 1);
    }

    private String annotationsToString(Annotation[] annotations) {
        if (annotations == null || annotations.length == 0) return "";
        return Arrays.stream(annotations).map(a -> a.annotationType().getSimpleName()).collect(
                Collectors.joining(", @", " annotated with @", ""));
    }
}
