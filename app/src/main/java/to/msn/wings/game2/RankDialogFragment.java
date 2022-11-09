package to.msn.wings.game2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class RankDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = requireActivity();
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(activity)
                .inflate(R.layout.dialog_body,null);
        ListView ranklist = layout.findViewById(R.id.rankingList);
        Bundle data = getArguments();
        ArrayList<String> turns = data.getStringArrayList("turns");
        Log.e("abc",turns.get(0));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,android.R.layout.simple_list_item_1,turns);
        ranklist.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        Dialog dialog = builder.setTitle("ランキング")
                .setView(layout)
                .create();
        return dialog;
    }
}
