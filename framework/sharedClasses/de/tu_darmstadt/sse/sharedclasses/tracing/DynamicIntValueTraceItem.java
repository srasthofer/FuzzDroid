package de.tu_darmstadt.sse.sharedclasses.tracing;

import android.os.Parcel;
import android.os.Parcelable;


public class DynamicIntValueTraceItem extends DynamicValueTraceItem {
	
	
	private static final long serialVersionUID = -7361222929840012462L;

	private int intValue;
	
	public static final Parcelable.Creator<DynamicIntValueTraceItem> CREATOR =
			new Parcelable.Creator<DynamicIntValueTraceItem>() {

		@Override
		public DynamicIntValueTraceItem createFromParcel(Parcel parcel) {
			DynamicIntValueTraceItem ti = new DynamicIntValueTraceItem();
			ti.readFromParcel(parcel);
			return ti;
		}

		@Override
		public DynamicIntValueTraceItem[] newArray(int size) {
			return new DynamicIntValueTraceItem[size];
		}

	};
	
	
	protected DynamicIntValueTraceItem() {
		super();
	}

	
	public DynamicIntValueTraceItem(int intValue, int paramIdx,
			int lastExecutedStatement) {
		super(paramIdx, lastExecutedStatement);
		this.intValue = intValue;
	}

	
	public int getIntValue() {
		return this.intValue;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		super.writeToParcel(parcel, arg1);
		parcel.writeInt(intValue);
	}
	
	@Override
	protected void readFromParcel(Parcel parcel) {
		super.readFromParcel(parcel);
		this.intValue = parcel.readInt();
	}

}
