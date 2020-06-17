/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SafeMutableArray <T> implements SafeArray<T> {
    private final @NotNull Object lock = new Object();
    private final @NotNull ArrayList<T> _value;

    public SafeMutableArray() {
        _value = new ArrayList<>();
    }

    public SafeMutableArray(@Nullable List<T> values) {
        _value = values != null ? new ArrayList<>(values) : new ArrayList<T>();
    }

    @Override
    public @NotNull List<T> copyData() {
        synchronized (lock) {
            return new ArrayList<>(_value);
        }
    }

    // # Query

    @Override
    public int size() {
        synchronized (lock) {
            return _value.size();
        }
    }

    @Override
    public T get(int pos) {
        synchronized (lock) {
            return _value.get(pos);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return _value.isEmpty();
        }
    }

    @Override
    public boolean contains(@NotNull T value) {
        synchronized (lock) {
            return _value.contains(value);
        }
    }

    @Override
    public int indexOf(@NotNull T value) {
        synchronized (lock) {
            return _value.indexOf(value);
        }
    }

    // # Operations

    public void add(@NotNull T value) {
        synchronized (lock) {
            _value.add(value);
        }
    }

    public void addAll(@NotNull Collection<T> value) {
        synchronized (lock) {
            _value.addAll(value);
        }
    }

    public void insert(@NotNull T value, int pos) {
        synchronized (lock) {
            _value.add(pos, value);
        }
    }

    public void removeAt(int index) {
        synchronized (lock) {
            _value.remove(index);
        }
    }

    public void remove(@NotNull T value) {
        synchronized (lock) {
            _value.remove(value);
        }
    }

    public void remove(@NotNull Function<T, Boolean> filter) {
        synchronized (lock) {
            ArrayList<T> copiedValues = new ArrayList<>(_value);

            for (T element: copiedValues) {
                if (filter.apply(element)) {
                    _value.remove(element);
                }
            }
        }
    }

    public void removeAll() {
        synchronized (lock) {
            _value.clear();
        }
    }

    // # Other

    @Override
    public @NotNull ArrayList<T> filter(@NotNull Function<T, Boolean> filter) {
        ArrayList<T> filteredValues = new ArrayList<>();

        synchronized (lock) {
            for (T element: _value) {
                if (filter.apply(element)) {
                    filteredValues.add(element);
                }
            }
        }

        return filteredValues;
    }

    @Override
    public void perform(@NotNull Callback<T> callback) {
        ArrayList<T> valueCopied;

        synchronized (lock) {
            valueCopied = new ArrayList<>(_value);
        }

        for (T element: valueCopied) {
            callback.perform(element);
        }
    }
}

