package apps.ft.beastbuster;

import android.os.Bundle;
import android.preference.PreferenceFragment;
/**
 * Created by FURUKAWA on 2016/12/03.
 */
public class BbSetFragment  extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
