package kilanny.muslimalarm.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.arch.core.util.Function;

import kilanny.muslimalarm.R;
import kilanny.muslimalarm.data.AppDb;
import kilanny.muslimalarm.data.Barcode;
import kilanny.muslimalarm.data.BarcodeDao;
import kilanny.muslimalarm.util.Utils;

public class BarcodeListAdapter extends ArrayAdapter<Barcode>
        implements CompoundButton.OnCheckedChangeListener {

    private int mSelectedBarcodeId;
    private boolean mIsListeningForChange = true;

    public BarcodeListAdapter(@NonNull Context context) {
        super(context, R.layout.barcode_list_item);
    }

    public void setSelectedBarcode(int barcodeId) {
        this.mSelectedBarcodeId = barcodeId;
        for (int i = 0; i < getCount(); ++i) {
            Barcode barcode = getItem(i);
            barcode.selected = (barcode.getId() == barcodeId);
        }
        notifyDataSetChanged();
    }

    public int getSelectedBarcode() {
        return this.mSelectedBarcodeId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView;
        if (convertView == null)
            rowView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.barcode_list_item, parent, false);
        else
            rowView = convertView;
        final Barcode item = getItem(position);
        AppCompatTextView textView = rowView.findViewById(R.id.txtContent);
        textView.setText(item.getCode());

        final AppCompatRadioButton radioButton = rowView.findViewById(R.id.radioSelected);
        if (!item.selected && item.getId() == mSelectedBarcodeId)
            item.selected = true;
        radioButton.setChecked(item.selected);
        radioButton.setOnCheckedChangeListener(this);
        radioButton.setTag(item.getId());
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioButton.setChecked(true);
            }
        });

        rowView.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEditClick(item, view);
            }
        });
        rowView.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeleteClick(item, view);
            }
        });
        return rowView;
    }

    private void onEditClick(final Barcode item, final View view) {
        final AppCompatEditText input = new AppCompatEditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(item.getCode());
        new AlertDialog.Builder(view.getContext())
                .setTitle(view.getContext().getString(R.string.edit_barcode))
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String txt = input.getText().toString().trim();
                        if (txt.equals("")) {
                            Toast.makeText(view.getContext(), R.string.you_entered_empty,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        item.setCode(txt);
                        Utils.runInBackground(new Function<Context, Void>() {
                            @Override
                            public Void apply(Context input) {
                                AppDb.getInstance(input).barcodeDao().update(item);
                                return null;
                            }
                        }, new Function<Void, Void>() {
                            @Override
                            public Void apply(Void input) {
                                notifyDataSetChanged();
                                return null;
                            }
                        }, view.getContext());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onDeleteClick(final Barcode item, final View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle(view.getContext().getString(R.string.delete_barcode))
                .setMessage(view.getContext().getString(R.string.are_you_sure_delete_barcode))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utils.runInBackground(new Function<Context, Void>() {
                            @Override
                            public Void apply(Context input) {
                                BarcodeDao dao = AppDb.getInstance(input).barcodeDao();
                                dao.delete(item);
                                remove(item);
                                return null;
                            }
                        }, new Function<Void, Void>() {
                            @Override
                            public Void apply(Void input) {
                                notifyDataSetChanged();
                                return null;
                            }
                        }, view.getContext());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (!mIsListeningForChange || !b) return;

        int id = (int) compoundButton.getTag();
        mIsListeningForChange = false;
        for (int i = 0; i < getCount(); ++i) {
            Barcode item = getItem(i);
            if (item.selected = item.getId() == id)
                mSelectedBarcodeId = item.getId();
        }
        notifyDataSetChanged();
        mIsListeningForChange = true;
    }
}
