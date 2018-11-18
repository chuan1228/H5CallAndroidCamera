package com.chuan_sir.h5callandroidcamera;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chuan_sir.h5callandroidcamera.adapter.CommonAdapter;
import com.chuan_sir.h5callandroidcamera.adapter.ViewHolder;

import java.util.ArrayList;

public class CustomDiaLog extends Dialog {

	public CustomDiaLog(Context context) {
		super(context);
	}

	public CustomDiaLog(Context context, int theme) {
		super(context, theme);
	}

	public static class Builder {
		private Context context;
		private OnItemClickListener itemClickListener;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setItemClickListener(OnItemClickListener listener) {
			this.itemClickListener = listener;
			return this;
		}

		public CustomDiaLog build(ArrayList<String> menus) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final CustomDiaLog dialog = new CustomDiaLog(context, R.style.rp_common_dialog_style);
			View layout = inflater.inflate(R.layout.rp_layout_custom_dialog_menu, null);
			dialog.addContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			ListView listView = (ListView) layout.findViewById(R.id.listview_dialog);

			CommonAdapter<String> mAdapter = new CommonAdapter<String>(context, menus,
					R.layout.rp_layout_custom_dialog_menu_item) {

				@Override
				public void convert(ViewHolder holder, String str) {
					TextView tv = (TextView) holder.getView(R.id.tv_dialog_unbind_bank_card);
					tv.setText(str);
				}
			};
			listView.setAdapter(mAdapter);

			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					itemClickListener.onItemClick(parent, view, position, id);
				}
			});

			dialog.setContentView(layout);
			return dialog;
		}
	}

}
