package kilanny.muslimalarm.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.arch.core.util.Function;
import androidx.fragment.app.Fragment;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.adapters.BarcodeListAdapter;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.Barcode;
import kilanny.muslimalarm.util.Utils;

public class BarcodeStopConfigFragment extends Fragment {

    private static final String ARG_SELECTED_BARCODE_ID = "selectedBarcodeId";

    private BarcodeListAdapter mAdapter;
    private Integer mSelectedBarcodeId;

    private FragmentInteractionListener mListener;


    public BarcodeStopConfigFragment() {
    }

    public static BarcodeStopConfigFragment newInstance(Integer selectedBarcodeId) {
        BarcodeStopConfigFragment fragment = new BarcodeStopConfigFragment();
        Bundle bundle = new Bundle();
        if (selectedBarcodeId != null)
            bundle.putInt(ARG_SELECTED_BARCODE_ID, selectedBarcodeId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedBarcodeId = getArguments().getInt(ARG_SELECTED_BARCODE_ID);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onBarcodeAdded() {
        bind(getContext());
    }

    private void bind(Context context) {
        mAdapter.clear();
        Utils.runInBackground(new Function<Context, Barcode[]>() {
            @Override
            public Barcode[] apply(Context input) {
                return AppDb.getInstance(input).barcodeDao().getAll();
            }
        }, new Function<Barcode[], Void>() {
            @Override
            public Void apply(Barcode[] input) {
                mAdapter.addAll(input);
                mAdapter.notifyDataSetChanged();
                return null;
            }
        }, context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_barcode_stop_config, container, false);
        mAdapter = new BarcodeListAdapter(view.getContext());
        mAdapter.setSelectedBarcode(mSelectedBarcodeId);

        view.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAdapter.getSelectedBarcode() == 0) {
                    Toast.makeText(view.getContext(), R.string.please_select_barcode,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                mListener.onDismiss(mAdapter.getSelectedBarcode());
            }
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDismiss(null);
            }
        });

        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(mAdapter);
        bind(view.getContext());
        return view;
    }

    public interface FragmentInteractionListener {
        void onDismiss(Integer selectedBarcodeId);
    }
}
