/*
 * Copyright (C) 2017 volders GmbH with <3 in Berlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package berlin.volders.rxdiff;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.ref.WeakReference;

import berlin.volders.rxdiff.RxDiffUtil.Callback;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class OnCalculateDiffSubscriberTest {

    @Mock
    Adapter adapter;
    @Mock
    Callback callback;
    @Mock
    DiffUtil.Callback cb;

    TestSubscriber<OnCalculateDiffResult<Adapter, String>> subscriber;
    OnCalculateDiffSubscriber<Adapter, String> diff;

    @Before
    public void setup() {
        doReturn(cb).when(callback).diffUtilCallback(any(Adapter.class), any());
        subscriber = TestSubscriber.create();
        diff = new OnCalculateDiffSubscriber<>(subscriber, new WeakReference<>(adapter), callback, false);
    }

    @Test
    public void onNext() {
        String value = "foobar";

        diff.onNext(value);

        OnCalculateDiffResult<Adapter, String> result = subscriber.getOnNextEvents().get(0);
        assertThat(result.adapter.get(), is(diff.adapter.get()));
        assertThat(result.o, is(value));
        assertThat(result.diff, notNullValue());
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
        subscriber.assertValueCount(1);
    }

    @Test
    public void onCompleted() {
        diff.onCompleted();

        subscriber.assertNoErrors();
        subscriber.assertCompleted();
        subscriber.assertNoValues();
    }

    @Test
    public void onError() {
        Throwable e = new Throwable();

        diff.onError(e);

        subscriber.assertError(e);
        subscriber.assertNotCompleted();
        subscriber.assertNoValues();
    }
}
