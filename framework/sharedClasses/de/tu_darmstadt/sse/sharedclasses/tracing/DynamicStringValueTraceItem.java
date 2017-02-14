package de.tu_darmstadt.sse.sharedclasses.tracing;

import android.os.Parcel;
import android.os.Parcelable;


public class DynamicStringValueTraceItem extends DynamicValueTraceItem {
	
	private String stringValue;
	
	
	private static final long serialVersionUID = 7558624604497311423L;

	public static final Parcelable.Creator<DynamicStringValueTraceItem> CREATOR =
			new Parcelable.Creator<DynamicStringValueTraceItem>() {

		@Override
		public DynamicStringValueTraceItem createFromParcel(Parcel parcel) {
			DynamicStringValueTraceItem ti = new DynamicStringValueTraceItem();
			ti.readFromParcel(parcel);
			return ti;
		}

		@Override
		public DynamicStringValueTraceItem[] newArray(int size) {
			return new DynamicStringValueTraceItem[size];
		}

	};
	
	
	protected DynamicStringValueTraceItem() {
		super();
	}

	
	public DynamicStringValueTraceItem(String stringValue, int paramIdx,
			int lastExecutedStatement) {
		super(paramIdx, lastExecutedStatement);
		this.stringValue = stringValue;
	}

	
	public String getStringValue() {
		return this.stringValue;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		super.writeToParcel(parcel, arg1);
		parcel.writeString(stringValue);
	}
	
	@Override
	protected void readFromParcel(Parcel parcel) {
		super.readFromParcel(parcel);
		this.stringValue = parcel.readString();
	}

}
