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

package berlin.volders.rxdiff.test;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import berlin.volders.rxdiff.RxDiffUtil;
import rx.Observer;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.subjects.ReplaySubject;

import static androidx.recyclerview.widget.RecyclerView.Adapter;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static rx.subjects.ReplaySubject.create;

public class AndroidTestAdapter<T> extends Adapter<ViewHolder> implements
        Func1<AndroidTestAdapter<T>, T>, Action2<AndroidTestAdapter<T>, T>,
        RxDiffUtil.Callback<AndroidTestAdapter<T>, T>, RxDiffUtil.Callback2<T> {

    private final ReplaySubject<T> ts = create();
    private final Func1<T, Integer> sizeOf;

    public AndroidTestAdapter(Func1<T, Integer> sizeOf) {
        this.ts.onNext(null);
        this.sizeOf = sizeOf;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new View(getApplicationContext())) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return sizeOf.call(call(this));
    }

    @Override
    public void call(AndroidTestAdapter<T> adapter, T t) {
        adapter.ts.onNext(t);
    }

    @Override
    public T call(AndroidTestAdapter<T> adapter) {
        int last = adapter.ts.size() - 1;
        return adapter.ts.skip(last).toBlocking().first();
    }

    @NonNull
    @Override
    public DiffUtil.Callback diffUtilCallback(@NonNull AndroidTestAdapter<T> adapter, @Nullable T newData) {
        return diffUtilCallback(call(adapter), newData);
    }

    @NonNull
    @Override
    public DiffUtil.Callback diffUtilCallback(@Nullable T oldData, @Nullable T newData) {
        return new AndroidTestDiffUtilCallback<>(oldData, newData, sizeOf);
    }

    public void subscribe(Observer<T> subscriber) {
        ts.skip(1).subscribe(subscriber);
    }

    public Func1<AndroidTestAdapter<T>, T> notifyOnGet() {
        return new Func1<AndroidTestAdapter<T>, T>() {
            @Override
            public T call(AndroidTestAdapter<T> adapter) {
                adapter.notifyDataSetChanged();
                return adapter.call(adapter);
            }
        };
    }
}