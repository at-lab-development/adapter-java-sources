package com.epam.jira.junit;

import com.epam.jira.JIRATestKey;
import com.epam.jira.entity.Issue;
import com.epam.jira.entity.Issues;
import com.epam.jira.entity.Parameter;
import com.epam.jira.entity.TestResult;
import com.epam.jira.util.FileUtils;
import com.epam.jira.util.JiraInfoProvider;
import com.epam.jira.util.Screenshoter;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutionListener extends RunListener {

    private final List<Issue> issues = new ArrayList<>();
    private final SkipAnalyzer skipAnalyzer = new SkipAnalyzer();
    //Start and End time of the test
    private long startTime;
    private long endTime;

    @Override
    public void testRunStarted(Description description) throws Exception {
        startTime = new Date().getTime();
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);

        for (Issue issue : issues) {
            List<String> attachments = JiraInfoProvider.getIssueAttachments(issue.getIssueKey());
            List<Parameter> parameters = JiraInfoProvider.getIssueParameters(issue.getIssueKey());
            if (attachments != null) {
                if (issue.getAttachments() != null)
                    issue.getAttachments().addAll(attachments);
                else
                    issue.setAttachments(attachments);
            }
            if (parameters != null) issue.setParameters(parameters);
        }

        if (!issues.isEmpty()) {
            FileUtils.writeXmlJunit(new Issues(issues), "jira-tm-report.xml");
        }
    }

    @Override
    public void testStarted(Description description) throws Exception {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        endTime = System.currentTimeMillis() - startTime;
        String key = JunitUtils.getJIRATestKey(description);

        Optional<Issue> optional = issues.stream()
                .filter(x -> x.getIssueKey().equals(key)).findAny();
        if (!optional.isPresent()) {
            Issue issuePassed = new Issue(key, TestResult.PASSED, JunitUtils.getTimeAsString(endTime));
            issues.add(issuePassed);
        }
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        endTime = System.currentTimeMillis() - startTime;

        JIRATestKey annotation = JunitUtils.getJIRATestKeyAnnotation(failure.getDescription());

        Issue issue = null;
        List<String> attachments = new ArrayList<>();

        if (annotation != null) {
            String screenshot = null;
            String summary;

            if (Screenshoter.isInitialized() && !annotation.disableScreenshotOnFailure()) {
                screenshot = Screenshoter.takeScreenshot();
            }

            issue = new Issue(annotation.key(), TestResult.FAILED, JunitUtils.getTimeAsString(endTime));

            // Save failure message and/or trace
            Throwable throwable = failure.getException();
            if (throwable instanceof AssertionError) {
                summary = "Assertion failed: " + throwable.getMessage();
            } else {
                summary = FileUtils.save(failure.getException());
                attachments.add(getAttachmentPath(summary));
            }

            // Add screenshot path if exist
            if (screenshot != null) {
                attachments.add(FileUtils.getAttachmentsDir() + screenshot);
                summary += ".\nScreenshot attached as " + screenshot;
            }

            issue.setSummary(summary);
            if (!attachments.isEmpty())
                issue.setAttachments(attachments);
            issues.add(issue);
        }
        skipAnalyzer.addFailedResult(failure, issue);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
        String key = JunitUtils.getJIRATestKey(description);
        if (key != null) {
            Issue issue = new Issue(key, TestResult.BLOCKED);
            issues.add(issue);
        }
    }

    private String getAttachmentPath(String message) {
        Pattern pattern = Pattern.compile("stacktrace.\\d{4}-\\d{2}-\\d{2}T.*");
        Matcher matcher = pattern.matcher(message);
        return (matcher.find()) ? FileUtils.getAttachmentsDir() + matcher.group() : null;
    }
}