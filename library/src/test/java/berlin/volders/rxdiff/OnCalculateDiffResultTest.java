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
import java.util.ConcurrentModificationException;

import berlin.volders.rxdiff.RxDiffUtil.Callback;
import rx.Producer;
import rx.functions.Actions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class OnCalculateDiffResultTest {

    @Mock
    Adapter adapter;
    @Mock
    Callback callback;
    @Mock
    Producer producer;
    @Mock
    DiffUtil.Callback cb;

    OnCalculateDiffResult<?, ?> result;

    @Before
    public void setup() {
        doReturn(cb).when(callback).diffUtilCallback(any(Adapter.class), any());
        result = new OnCalculateDiffResult<>(new WeakReference<>(adapter), null, callback, false, producer);
    }

    @Test
    public void applyDiff() {
        result.applyDiff(Actions.empty());

        verify(producer).request(anyLong());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void applyDiff_concurrently() {
        result.onChanged();

        result.applyDiff(Actions.empty());
    }

    @Test
    public void onChanged() {
        result.onChanged();

        assertThat(result.invalidated, is(true));
    }

    @Test
    public void onItemRangeChanged() {
        result.onItemRangeChanged(0, 1);

        assertThat(result.invalidated, is(true));
    }

    @Test
    public void onItemRangeChanged_payload() {
        result.onItemRangeChanged(0, 1, null);

        assertThat(result.invalidated, is(true));
    }

    @Test
    public void onItemRangeInserted() {
        result.onItemRangeInserted(0, 1);

        assertThat(result.invalidated, is(true));
    }

    @Test
    public void onItemRangeRemoved() {
        result.onItemRangeRemoved(0, 1);

        assertThat(result.invalidated, is(true));
    }

    @Test
    public void onItemRangeMoved() {
        result.onItemRangeMoved(0, 1, 2);

        assertThat(result.invalidated, is(true));
    }

    @Test
    public void nonLeaking() {
        assertThat(OnCalculateDiffResult.nonLeaking(new WeakReference<>(adapter)), is(adapter));
    }

    @Test(expected = RxDiffUtil.SubscriptionLeak.class)
    public void nonLeaking_leaking() {
        OnCalculateDiffResult.nonLeaking(new WeakReference(null));
    }

    @Test
    public void checkConcurrency() {
        result.checkConcurrency(adapter);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void checkConcurrency_changed() {
        result.onChanged();

        result.checkConcurrency(adapter);
    }
}
