package kilanny.muslimalarm.fragments.alarmring;


import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class ShowAlarmFragment extends Fragment {


    public ShowAlarmFragment() {
        // Required empty public constructor
    }

    public interface FragmentInteractionListener {
        void onResetSleepTimeout();

        void onDismissed(boolean isDone);
    }
}
