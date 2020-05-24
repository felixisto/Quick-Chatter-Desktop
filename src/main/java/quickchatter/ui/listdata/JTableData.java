/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.listdata;

import java.util.List;
import javax.swing.table.TableModel;
import org.jetbrains.annotations.NotNull;
import quickchatter.utilities.Copyable;

public interface JTableData <T> extends TableModel, Copyable<JTableData <T>> {
    @NotNull List<T> getValues();
    int count();
    @NotNull T getValue(int index);
}
