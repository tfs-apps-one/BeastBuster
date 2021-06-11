package apps.ft.beastbuster;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by FURUKAWA on 2016/12/03.
 */
public class BbSetActivity extends PreferenceActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new BbSetFragment()).commit();
    }

}
