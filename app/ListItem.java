import android.app.LauncherActivity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ListItem {
    public String value;
    public boolean hasBackground = false;



    public ListItem(String value, boolean background) {
        this.value = value;
        this.hasBackground = background;
    }

    private ListItem getDefaultItem(String value) {
        return new ListItem(value, false);
    }



}

