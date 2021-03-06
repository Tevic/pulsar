/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.functions.runtime;

import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RuntimeUtilsTest {

    @Test
    public void testActions() throws InterruptedException {

        // Test for success
        Supplier<RuntimeUtils.Actions.ActionResult> supplier1 = mock(Supplier.class);
        when(supplier1.get()).thenReturn(RuntimeUtils.Actions.ActionResult.builder().success(true).build());

        Supplier<RuntimeUtils.Actions.ActionResult> supplier2 = mock(Supplier.class);
        when(supplier2.get()).thenReturn(RuntimeUtils.Actions.ActionResult.builder().success(true).build());

        Runnable onFail = mock(Runnable.class);
        Runnable onSucess = mock(Runnable.class);

        RuntimeUtils.Actions.Action action1 = spy(
                RuntimeUtils.Actions.Action.builder()
                        .actionName("action1")
                        .numRetries(10)
                        .sleepBetweenInvocationsMs(100)
                        .supplier(supplier1)
                        .continueOn(true)
                        .onFail(onFail)
                        .onSuccess(onSucess)
                        .build());

        RuntimeUtils.Actions.Action action2 = spy(
                RuntimeUtils.Actions.Action.builder()
                        .actionName("action2")
                        .numRetries(20)
                        .sleepBetweenInvocationsMs(200)
                        .supplier(supplier2)
                        .build());

        RuntimeUtils.Actions actions = RuntimeUtils.Actions.newBuilder()
                .addAction(action1)
                .addAction(action2);
        actions.run();

        Assert.assertEquals(actions.numActions(), 2);
        verify(supplier1, times(1)).get();
        verify(onFail, times(0)).run();
        verify(onSucess, times(1)).run();
        verify(supplier2, times(1)).get();

        // test only run 1 action

        supplier1 = mock(Supplier.class);
        when(supplier1.get()).thenReturn(RuntimeUtils.Actions.ActionResult.builder().success(true).build());

        supplier2 = mock(Supplier.class);
        when(supplier2.get()).thenReturn(RuntimeUtils.Actions.ActionResult.builder().success(true).build());

        onFail = mock(Runnable.class);
        onSucess = mock(Runnable.class);

        action1 = spy(
                RuntimeUtils.Actions.Action.builder()
                        .actionName("action1")
                        .numRetries(10)
                        .sleepBetweenInvocationsMs(100)
                        .supplier(supplier1)
                        .continueOn(false)
                        .onFail(onFail)
                        .onSuccess(onSucess)
                        .build());

        action2 = spy(
                RuntimeUtils.Actions.Action.builder()
                        .actionName("action2")
                        .numRetries(20)
                        .sleepBetweenInvocationsMs(200)
                        .supplier(supplier2)
                        .onFail(onFail)
                        .onSuccess(onSucess)
                        .build());

        actions = RuntimeUtils.Actions.newBuilder()
                .addAction(action1)
                .addAction(action2);
        actions.run();

        Assert.assertEquals(actions.numActions(), 2);
        verify(supplier1, times(1)).get();
        verify(onFail, times(0)).run();
        verify(onSucess, times(1)).run();
        verify(supplier2, times(0)).get();

        // test retry

        supplier1 = mock(Supplier.class);
        when(supplier1.get()).thenReturn(RuntimeUtils.Actions.ActionResult.builder().success(false).build());

        supplier2 = mock(Supplier.class);
        when(supplier2.get()).thenReturn(RuntimeUtils.Actions.ActionResult.builder().success(true).build());

        onFail = mock(Runnable.class);
        onSucess = mock(Runnable.class);

        action1 = spy(
                RuntimeUtils.Actions.Action.builder()
                        .actionName("action1")
                        .numRetries(10)
                        .sleepBetweenInvocationsMs(10)
                        .supplier(supplier1)
                        .continueOn(false)
                        .onFail(onFail)
                        .onSuccess(onSucess)
                        .build());

        action2 = spy(
                RuntimeUtils.Actions.Action.builder()
                        .actionName("action2")
                        .numRetries(20)
                        .sleepBetweenInvocationsMs(200)
                        .supplier(supplier2)
                        .build());

        actions = RuntimeUtils.Actions.newBuilder()
                .addAction(action1)
                .addAction(action2);
        actions.run();

        Assert.assertEquals(actions.numActions(), 2);
        verify(supplier1, times(10)).get();
        verify(onFail, times(1)).run();
        verify(onSucess, times(0)).run();
        verify(supplier2, times(1)).get();

    }
}
