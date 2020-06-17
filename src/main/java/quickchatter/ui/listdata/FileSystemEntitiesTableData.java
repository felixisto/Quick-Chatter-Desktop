/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.listdata;

import java.util.List;
import javax.swing.event.TableModelListener;
import org.jetbrains.annotations.NotNull;
import filesystem.model.EntityInfo;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;
import utilities.CollectionUtilities;
import utilities.Logger;

public class FileSystemEntitiesTableData implements JTableData<FileSystemEntityViewModel> {
    private final int _maxRowCount;
    private final int _maxColumnCount;
    private final @NotNull List<FileSystemEntityViewModel> _data;
    
    private final int _currentRowCount;
    
    public FileSystemEntitiesTableData(int maxRowCount, int maxColumnCount, @NotNull List<FileSystemEntityViewModel> data) {
        _maxRowCount = maxRowCount;
        _maxColumnCount = maxColumnCount > 0 ? maxColumnCount : 1;
        _data = CollectionUtilities.copy(data);
        
        int rows = (int)Math.ceil((double)_data.size() / (double)_maxColumnCount);
        _currentRowCount = rows > _maxRowCount ? _maxRowCount : rows;
    }
    
    public static int indexOf(int row, int column, int maxColumnCount) {
        return column + (maxColumnCount * row);
    }
    
    public int indexOf(int row, int column) {
        return indexOf(row, column, _maxColumnCount);
    }
    
    @Override
    public List<FileSystemEntityViewModel> getValues() {
        return _data;
    }

    @Override
    public int count() {
        return _data.size();
    }

    @Override
    public FileSystemEntityViewModel getValue(int index) {
        return _data.get(index);
    }

    @Override
    public int getRowCount() {
        return _currentRowCount;
    }

    @Override
    public int getColumnCount() {
        return _maxColumnCount;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return EntityInfo.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int index = indexOf(rowIndex, columnIndex);
        
        if (index >= _data.size()) {
            return null;
        }
        
        return _data.get(index);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Logger.warning(this, "Unsupported set value operation");
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        // Logger.warning(this, "Unsupported addTableModelListener operation");
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        
    }

    @Override
    public JTableData<FileSystemEntityViewModel> copy() {
        return new FileSystemEntitiesTableData(_maxRowCount, _maxColumnCount, _data);
    }
}
