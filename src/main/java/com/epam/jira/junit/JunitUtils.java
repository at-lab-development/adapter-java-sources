package com.epam.jira.junit;

import com.epam.jira.JIRATestKey;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class JunitUtils {

    static String getFullMethodName(FrameworkMethod method) {
        String methodName = method.getMethod().getName();
        return method.getMethod().getName() + "." + methodName;
    }

    static Annotation[] getMethodGroups(FrameworkMethod methodRes) {
        Method method = methodRes.getMethod();
        Test annotation = method.getAnnotation(Test.class);
        if (annotation != null) {
            return annotation.expected().getAnnotations();
        }
        return null;
    }

    static String getJIRATestKey(Description description) {
        JIRATestKey annotation = getJIRATestKeyAnnotation(description);
        return annotation != null ? annotation.key() : null;
    }

    static JIRATestKey getJIRATestKeyAnnotation(Description method) {
        JIRATestKey annotation = method.getAnnotation(JIRATestKey.class);
        if (annotation != null && !annotation.disabled()) {
            return annotation;
        }
        return null;
    }

    static String getTimeAsString(Long time) {
        long timeDiff = time;
        String formattedResult;
        if (timeDiff < 10)
            formattedResult = timeDiff + " ms";
        else if (timeDiff < 60000)
            formattedResult = (timeDiff / 1000.0) + " s";
        else
            formattedResult = (timeDiff / 60000.0) + " min";
        return formattedResult;
    }
}
