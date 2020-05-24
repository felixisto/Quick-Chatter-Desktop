/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public interface SafeArray <T> {
    @NotNull List<T> copyData();

    // # Query

    int size();

    T get(int pos);

    boolean isEmpty();

    boolean contains(@NotNull T value);

    public int indexOf(@NotNull T value);

    // # Other

    @NotNull ArrayList<T> filter(@NotNull Function<T, Boolean> filter);

    void perform(@NotNull Callback<T> callback);
}
