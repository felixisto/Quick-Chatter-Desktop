/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.view.other;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jetbrains.annotations.NotNull;
import quickchatter.ui.view.FileSystemEntitiesTableCell;
import quickchatter.ui.viewmodel.FileSystemEntityViewModel;

public class FileSystemEntitiesTableRenderer extends DefaultTableCellRenderer {
    private final @NotNull ImageIcon _folderIcon;
    private final @NotNull ImageIcon _fileIcon;
    
    private final FileSystemEntitiesTableCell container;
    private boolean initialized = false;
    
    public FileSystemEntitiesTableRenderer(@NotNull ImageIcon folderIcon, @NotNull ImageIcon fileIcon) {
        _folderIcon = folderIcon;
        _fileIcon = fileIcon;
        
        container = new FileSystemEntitiesTableCell();
        container.setVisible(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (!initialized) {
            init(table);
        }
        
        if (value == null) {
            resetToDefaultLook(table);
            return container;
        }
        
        if (!isSelected) {
            container.setBackground(table.getBackground());
            container.setForeground(table.getForeground());
        } else {
            container.setBackground(table.getSelectionBackground());
            container.setForeground(table.getSelectionForeground());
        }
        
        drawComponent(table, value);
        
        return container;
    }
    
    public void drawComponent(JTable table, Object value) {
        if (!(value instanceof FileSystemEntityViewModel)) {
            resetToDefaultLook(table);
            return;
        }
        
        FileSystemEntityViewModel info = (FileSystemEntityViewModel)value;
        
        container.nameLabel.setText(info.name);
        container.sizeLabel.setText(info.size);
        
        if (info.isDirectory) {
            container.iconLabel.setIcon(_folderIcon);
        } else {
            container.iconLabel.setIcon(_fileIcon);
        }
    }
    
    public void resetToDefaultLook(JTable table) {
        container.iconLabel.setIcon(null);
        container.nameLabel.setText("");
        container.sizeLabel.setText("");
        
        container.setBackground(table.getBackground());
        container.setForeground(table.getForeground());
    }
    
    private void init(JTable table) {
        initialized = true;
        
        table.setRowHeight((int)container.getPreferredSize().getHeight());
    }
}
