package de.tu_darmstadt.sse.sharedclasses.tracing;

import android.os.Parcel;
import android.os.Parcelable;


public class DexFileTransferTraceItem extends TraceItem {
	private static final long serialVersionUID = -2768018408182596990L;
	
	public static final Parcelable.Creator<DexFileTransferTraceItem> CREATOR = new Parcelable.Creator<DexFileTransferTraceItem>() {

		@Override
		public DexFileTransferTraceItem createFromParcel(Parcel parcel) {
			DexFileTransferTraceItem ti = new DexFileTransferTraceItem();
			ti.readFromParcel(parcel);
			return ti;
		}

		@Override
		public DexFileTransferTraceItem[] newArray(int size) {
			return new DexFileTransferTraceItem[size];
		}

	};
	
	private String fileName;
	private byte[] dexFile;
	
	private DexFileTransferTraceItem() {
		super();
	}
	
	public DexFileTransferTraceItem(String fileName, byte[] dexFile,
			int lastExecutedStatement, int globalLastExecutedStatement) {
		super(lastExecutedStatement, globalLastExecutedStatement);
		this.fileName = fileName;
		this.dexFile = dexFile;
	}
	
	public String getFileName() {
		return fileName;
	}

	public byte[] getDexFile() {
		return dexFile;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		super.writeToParcel(parcel, arg1);
		parcel.writeString(fileName);
		
		parcel.writeInt(dexFile.length);
		parcel.writeByteArray(dexFile);
	}
	
	@Override
	protected void readFromParcel(Parcel parcel) {
		super.readFromParcel(parcel);
		this.fileName = parcel.readString();
		
		int len = parcel.readInt();
		this.dexFile = new byte[len];
		parcel.readByteArray(this.dexFile);
	}

}
