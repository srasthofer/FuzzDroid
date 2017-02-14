package de.tu_darmstadt.sse.sharedclasses.crashreporter;

import android.os.Parcel;
import android.os.Parcelable;
import de.tu_darmstadt.sse.sharedclasses.tracing.TraceItem;


public class CrashReportItem extends TraceItem {
	
	
	private static final long serialVersionUID = 5787737805848107595L;

	private String exceptionMessage;
	
	public static final Parcelable.Creator<CrashReportItem> CREATOR = new Parcelable.Creator<CrashReportItem>() {

		@Override
		public CrashReportItem createFromParcel(Parcel parcel) {
			CrashReportItem ci = new CrashReportItem();
			ci.readFromParcel(parcel);
			return ci;
		}

		@Override
		public CrashReportItem[] newArray(int size) {
			return new CrashReportItem[size];
		}

	};
	
	
	private CrashReportItem() {
		super();
	}
	
	
	public CrashReportItem(String exceptionMessage, int lastExecutedStatement) {
		super(lastExecutedStatement);
		this.exceptionMessage = exceptionMessage;
	}
	
	
	public String getExceptionMessage() {
		return this.exceptionMessage;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		super.writeToParcel(parcel, arg1);
		parcel.writeString(exceptionMessage);
	}
	
	@Override
	protected void readFromParcel(Parcel parcel) {
		super.readFromParcel(parcel);
		exceptionMessage = parcel.readString();		
	}
	
}
