/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.ui.listdata;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListDataListener;
import org.jetbrains.annotations.NotNull;
import quickchatter.network.bluetooth.basic.BEClient;
import quickchatter.utilities.CollectionUtilities;
import quickchatter.utilities.Logger;

public class BEClientsNamesListData implements JListData<String> {
    private final @NotNull List<BEClient> data;
    private final @NotNull ArrayList<String> names = new ArrayList<>();

    public BEClientsNamesListData(@NotNull BEClientsNamesListData other) {
        this.data = CollectionUtilities.copy(other.data);
        
        for (BEClient c : this.data) {
            names.add(c.getName());
        }
    }
    
    public BEClientsNamesListData(@NotNull List<BEClient> data) {
        this.data = CollectionUtilities.copy(data);
        
        for (BEClient c : this.data) {
            names.add(c.getName());
        }
    }
    
    public BEClientsNamesListData(@NotNull JListData<BEClient> data) {
        this.data = CollectionUtilities.copy(data.getValues());
        
        for (BEClient c : this.data) {
            names.add(c.getName());
        }
    }

    @Override
    public int count() {
        return names.size();
    }
    
    @Override
    public List<String> getValues() {
        return names;
    }

    @Override
    public String getValue(int position) {
        return names.get(position);
    }
    
    @Override
    public JListData<String> copy() {
        return new BEClientsNamesListData(this);
    }

    @Override
    public int getSize() {
        return count();
    }

    @Override
    public String getElementAt(int index) {
        return getValue(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        // Logger.warning(this, "Unsupported addListDataListener operation");
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        
    }
}

