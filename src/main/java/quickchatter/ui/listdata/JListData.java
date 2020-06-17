package quickchatter.ui.listdata;

import java.util.List;
import javax.swing.ListModel;
import org.jetbrains.annotations.NotNull;
import utilities.Copyable;

public interface JListData <T> extends ListModel<T>, Copyable<JListData <T>> {
    @NotNull List<T> getValues();
    int count();
    @NotNull T getValue(int index);
}
