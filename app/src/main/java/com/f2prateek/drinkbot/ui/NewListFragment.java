/*
 * Copyright (C) 2015 Square, Inc.
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
package com.f2prateek.drinkbot.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.f2prateek.drinkbot.R;
import com.f2prateek.drinkbot.TodoApp;
import com.f2prateek.drinkbot.db.TodoList;
import com.squareup.sqlbrite.SqlBrite;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static butterknife.ButterKnife.findById;

public final class NewListFragment extends DialogFragment {
  public static NewListFragment newInstance() {
    return new NewListFragment();
  }

  private final PublishSubject<String> createClicked = PublishSubject.create();

  @Inject SqlBrite db;

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    TodoApp.objectGraph(activity).inject(this);
  }

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Context context = getActivity();
    View view = LayoutInflater.from(context).inflate(R.layout.new_list, null);

    EditText name = findById(view, android.R.id.input);
    Observable<OnTextChangeEvent> nameText = WidgetObservable.text(name);

    Observable.combineLatest(createClicked, nameText,
        (ignored, event) -> event.text().toString()) //
        .subscribeOn(AndroidSchedulers.mainThread()) //
        .observeOn(Schedulers.io()).subscribe(name1 -> {
      db.insert(TodoList.TABLE, new TodoList.Builder().name(name1).build());
    });

    return new AlertDialog.Builder(context) //
        .setTitle(R.string.new_list)
        .setView(view)
        .setPositiveButton(R.string.create, (dialog, which) -> {
          createClicked.onNext("clicked");
        })
        .setNegativeButton(R.string.cancel, (dialog, which) -> {
        })
        .create();
  }
}
