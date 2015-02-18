/*
 * Copyright (c) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cheng Fang - Initial API and implementation
 */

package org.jberet.testapps.purgeInMemoryRepository;

import javax.batch.operations.NoSuchJobException;
import javax.batch.operations.NoSuchJobExecutionException;

import org.junit.Assert;
import org.junit.Test;

public class PurgeInMemoryRepositoryIT extends PurgeRepositoryTestBase {
    static final String purgeInMemoryRepositoryXml = "purgeInMemoryRepository.xml";

    @Test(expected = NoSuchJobExecutionException.class)
    public void restartNoSuchJobExecutionException() throws NoSuchJobExecutionException {
        jobOperator.restart(-1, null);
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void stopNoSuchJobExecutionException() throws NoSuchJobExecutionException {
        jobOperator.stop(-1);
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void abandonNoSuchJobExecutionException() throws NoSuchJobExecutionException {
        jobOperator.abandon(-1);
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void getParametersNoSuchJobExecutionException() throws NoSuchJobExecutionException {
        jobOperator.getParameters(-1);
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void getJobInstanceNoSuchJobExecutionException() throws NoSuchJobExecutionException {
        jobOperator.getJobInstance(-1);
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void getStepExecutionsNoSuchJobExecutionException() throws NoSuchJobExecutionException {
        jobOperator.getStepExecutions(-1);
    }


    @Test
    public void jobExecutionSelector() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();

        params.setProperty("jobExecutionSelector",
                "org.jberet.testapps.purgeInMemoryRepository.PurgeRepositoryTestBase$JobExecutionSelector1");
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        assertNoSuchJobExecution(prepurge1JobExecutionId);
        assertNoSuchJobExecution(prepurge2JobExecutionId);
    }

    @Test
    public void purgeJobsByNamesAll() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();
        final long prepurge3JobExecutionId = prepurge(prepurge2JobName);

        params.setProperty("purgeJobsByNames", "*");
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        assertNoSuchJobExecution(prepurge1JobExecutionId);
        assertNoSuchJobExecution(prepurge2JobExecutionId);
        assertNoSuchJobExecution(prepurge3JobExecutionId);
    }

    @Test
    public void jobExecutionsByJobNames() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();
        final long prepurge3JobExecutionId = prepurge(prepurge2JobName);

        params.setProperty("jobExecutionsByJobNames", prepurgeJobName);
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        assertNoSuchJobExecution(prepurge1JobExecutionId);
        assertNoSuchJobExecution(prepurge2JobExecutionId);
        Assert.assertNotNull(jobOperator.getJobExecution(prepurge3JobExecutionId));
    }

    @Test
    public void jobExecutionIds() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();

        //non-existent job execution id 999 will be ignored
        params.setProperty("jobExecutionIds", String.format("%s,%s,%s", prepurge1JobExecutionId, prepurge2JobExecutionId, 999));
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        assertNoSuchJobExecution(prepurge1JobExecutionId);
        assertNoSuchJobExecution(prepurge2JobExecutionId);
    }


    @Test
    public void numberOfRecentJobExecutionsToKeep() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();

        //2 job executions to keep:
        //the purge job itself
        //prepurge2
        params.setProperty("numberOfRecentJobExecutionsToKeep", "2");
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        assertNoSuchJobExecution(prepurge1JobExecutionId);
        Assert.assertNotNull(jobOperator.getJobExecution(prepurge2JobExecutionId));
    }

    @Test
    public void jobExecutionIdFrom() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();

        //purge job executions whose id >= prepurge2JobExecutionId
        params.setProperty("jobExecutionIdFrom", String.valueOf(prepurge2JobExecutionId));
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        Assert.assertNotNull(jobOperator.getJobExecution(prepurge1JobExecutionId));
        assertNoSuchJobExecution(prepurge2JobExecutionId);
    }

    @Test
    public void jobExecutionIdFromIncludeRunningOnes() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();

        //purge job executions whose id >= prepurge2JobExecutionId
        params.setProperty("jobExecutionIdFrom", String.valueOf(prepurge2JobExecutionId));
        //include running job executions
        params.setProperty("keepRunningJobExecutions", String.valueOf(false));

        startJob(purgeInMemoryRepositoryXml);
        Thread.sleep(purgeSleepMillis);
        final long purgeJobExecutionId = prepurge2JobExecutionId + 1;
        assertNoSuchJobExecution(purgeJobExecutionId);

        Assert.assertNotNull(jobOperator.getJobExecution(prepurge1JobExecutionId));
        assertNoSuchJobExecution(prepurge2JobExecutionId);
    }

    @Test
    public void jobExecutionIdTo() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();

        //purge job executions whose id <= prepurge2JobExecutionId
        params.setProperty("jobExecutionIdTo", String.valueOf(prepurge2JobExecutionId));
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        assertNoSuchJobExecution(prepurge1JobExecutionId);
        assertNoSuchJobExecution(prepurge2JobExecutionId);
    }

    @Test
    public void jobExecutionIdFromTo() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();
        final long prepurge3JobExecutionId = prepurge();

        //purge job executions whose id between prepurge2JobExecutionId and prepurge3JobExecutionId
        params.setProperty("jobExecutionIdFrom", String.valueOf(prepurge2JobExecutionId));
        params.setProperty("jobExecutionIdTo", String.valueOf(prepurge3JobExecutionId));
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        Assert.assertNotNull(jobOperator.getJobExecution(prepurge1JobExecutionId));
        assertNoSuchJobExecution(prepurge2JobExecutionId);
        assertNoSuchJobExecution(prepurge3JobExecutionId);
    }

    @Test
    public void withinPastMinutes() throws Exception {
        final long prepurge1JobExecutionId = prepurge();
        final long prepurge2JobExecutionId = prepurge();

        //purge job executions which ended within past 5 minutes
        params.setProperty("withinPastMinutes", "5");
        //keepRunningJobExecutions batch property defaults to true, the following sets it to its default value
        params.setProperty("keepRunningJobExecutions", String.valueOf(true));
        startAndVerifyPurgeJob(purgeInMemoryRepositoryXml);

        assertNoSuchJobExecution(prepurge1JobExecutionId);
        assertNoSuchJobExecution(prepurge2JobExecutionId);
    }
}
