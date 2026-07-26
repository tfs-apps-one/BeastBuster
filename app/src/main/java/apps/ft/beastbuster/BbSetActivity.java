package apps.ft.beastbuster;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Created by FURUKAWA on 2016/12/03.
 */
public class BbSetActivity extends PreferenceActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new BbSetFragment()).commit();

        // TODO:Edge-to-Edge（Android 15/16でシステムバーとの被りを防止）
        applyEdgeToEdgeInsets();
    }

    private void applyEdgeToEdgeInsets() {
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
    }

}
