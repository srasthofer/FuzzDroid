package de.tu_darmstadt.sse.sharedclasses.tracing;

import android.os.Parcel;
import android.os.Parcelable;


public class TimingBombTraceItem extends TraceItem {
	
	
	private static final long serialVersionUID = 1837171677027458456L;

	public static final Parcelable.Creator<TimingBombTraceItem> CREATOR = new Parcelable.Creator<TimingBombTraceItem>() {

		@Override
		public TimingBombTraceItem createFromParcel(Parcel parcel) {
			TimingBombTraceItem ti = new TimingBombTraceItem();
			ti.readFromParcel(parcel);
			return ti;
		}

		@Override
		public TimingBombTraceItem[] newArray(int size) {
			return new TimingBombTraceItem[size];
		}

	};

	private long originalValue;
	private long newValue;
	
	
	private TimingBombTraceItem() {
		
	}
	
	
	public TimingBombTraceItem(long originalValue, long newValue) {
		this.originalValue = originalValue;
		this.newValue = newValue;
	}
	
	
	public long getOriginalValue() {
		return this.originalValue;
	}
	
	
	public long getNewValue() {
		return this.newValue;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		super.writeToParcel(parcel, arg1);
		parcel.writeLong(this.originalValue);
		parcel.writeLong(this.newValue);
	}
	
	@Override
	protected void readFromParcel(Parcel parcel) {
		super.readFromParcel(parcel);
		this.originalValue = parcel.readLong();
		this.newValue = parcel.readLong();
	}

}
