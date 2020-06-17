package quickchatter.ui.listdata;

import java.util.List;
import javax.swing.event.ListDataListener;
import org.jetbrains.annotations.NotNull;
import network.bluetooth.basic.BEClient;
import utilities.CollectionUtilities;
import utilities.Logger;

public class BEClientsListData implements JListData<BEClient> {
    private final @NotNull List<BEClient> data;

    public BEClientsListData(@NotNull List<BEClient> data) {
        this.data = CollectionUtilities.copy(data);
    }
    
    public BEClientsListData(@NotNull JListData<BEClient> data) {
        this.data = CollectionUtilities.copy(data.getValues());
    }

    @Override
    public int count() {
        return data.size();
    }
    
    @Override
    public List<BEClient> getValues() {
        return data;
    }

    @Override
    public BEClient getValue(int position) {
        return data.get(position);
    }
    
    @Override
    public JListData<BEClient> copy() {
        return new BEClientsListData(this);
    }

    @Override
    public int getSize() {
        return count();
    }

    @Override
    public BEClient getElementAt(int index) {
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
