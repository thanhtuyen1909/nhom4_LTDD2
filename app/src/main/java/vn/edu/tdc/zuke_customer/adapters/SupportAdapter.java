package vn.edu.tdc.zuke_customer.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.TheCustomerConslutant;

public class SupportAdapter extends ArrayAdapter<TheCustomerConslutant> {
    Activity context;
    TextView tvItemQuestion;
    int resource;

    public SupportAdapter(@NonNull Activity context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        View customView = inflater.inflate(this.resource, null);
        tvItemQuestion = customView.findViewById(R.id.tvItemQuestion);
        TheCustomerConslutant cons = getItem(position);
        tvItemQuestion.setText(cons.getQuestion() + "");
        return customView;
    }
}