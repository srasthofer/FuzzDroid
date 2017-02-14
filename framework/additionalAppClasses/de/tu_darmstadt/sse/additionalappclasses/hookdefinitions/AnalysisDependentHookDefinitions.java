package de.tu_darmstadt.sse.additionalappclasses.hookdefinitions;

import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.sse.additionalappclasses.hooking.HookInfo;
import de.tu_darmstadt.sse.additionalappclasses.hooking.MethodHookInfo;

public class AnalysisDependentHookDefinitions implements Hook{

	@Override
	public Set<HookInfo> initializeHooks() {
		Set<HookInfo> allAnalysisDependentHooks = new HashSet<HookInfo>();
		allAnalysisDependentHooks.addAll(telephonyManagerHooks());	
//		allAnalysisDependentHooks.addAll(stringOperationHooks());
//		allAnalysisDependentHooks.addAll(applicationInfoHooks());
		allAnalysisDependentHooks.addAll(smsMessageHooks());
		allAnalysisDependentHooks.addAll(sharedPreferencesHooks());
//		allAnalysisDependentHooks.addAll(networkRelatedHooks());
		allAnalysisDependentHooks.addAll(integrityRelatedHooks());
		allAnalysisDependentHooks.addAll(fileRelatedHooks());
		allAnalysisDependentHooks.addAll(reflectionHooks());
		allAnalysisDependentHooks.addAll(deviceAdminHooks());
//		allAnalysisDependentHooks.addAll(intentBasedHooks());
		allAnalysisDependentHooks.addAll(xmlBasedHooks());
		allAnalysisDependentHooks.addAll(foregroundActivityCheckHooks());
		return allAnalysisDependentHooks;
	}	

	
	private Set<HookInfo> telephonyManagerHooks() {
		Set<HookInfo> tmHoocks = new HashSet<HookInfo>();
		
		MethodHookInfo deviceId = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getDeviceId()>");
        deviceId.analysisDependentHookAfter();
        MethodHookInfo countryIso = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getNetworkCountryIso()>");
        countryIso.analysisDependentHookAfter();
        MethodHookInfo networkOperator = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getNetworkOperator()>");
        networkOperator.analysisDependentHookAfter();
        MethodHookInfo simCountryIso = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getSimCountryIso()>");
        simCountryIso.analysisDependentHookAfter();
        MethodHookInfo simSerial = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>");
        simSerial.analysisDependentHookAfter();
        MethodHookInfo voiceMail = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getVoiceMailNumber()>");
        voiceMail.analysisDependentHookAfter();
        MethodHookInfo phoneType = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getPhoneType()>");
        phoneType.analysisDependentHookAfter();
        MethodHookInfo networkType = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getNetworkType()>");
        phoneType.analysisDependentHookAfter();
        MethodHookInfo simOperator = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getSimOperator()>");
        simOperator.analysisDependentHookAfter();
        MethodHookInfo simOperatorName = new MethodHookInfo("<android.telephony.TelephonyManager: java.lang.String getSimOperatorName()>");
        simOperatorName.analysisDependentHookAfter();

        tmHoocks.add(deviceId);
        tmHoocks.add(countryIso);
        tmHoocks.add(networkOperator);
        tmHoocks.add(simCountryIso);
        tmHoocks.add(simSerial);
        tmHoocks.add(voiceMail);
        tmHoocks.add(phoneType);        
        tmHoocks.add(networkType);        
        tmHoocks.add(simOperator);        
        tmHoocks.add(simOperatorName);        
        
        return tmHoocks;
	}
	
	private Set<HookInfo> intentBasedHooks() {
		Set<HookInfo> intentBasedHooks = new HashSet<HookInfo>();
		
		MethodHookInfo stringExtra = new MethodHookInfo("<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>");
		stringExtra.analysisDependentHookAfter();
        
        intentBasedHooks.add(stringExtra);
        return intentBasedHooks;
	}
	
	private Set<HookInfo> xmlBasedHooks() {
		Set<HookInfo> xmlBasedHooks = new HashSet<HookInfo>();
		
		MethodHookInfo attributeValue = new MethodHookInfo("<org.xmlpull.v1.XmlPullParser: java.lang.String getAttributeValue(int)>");
		attributeValue.analysisDependentHookAfter();
		
		xmlBasedHooks.add(attributeValue);
		
		return xmlBasedHooks;		
	}
	
	
	private Set<HookInfo> deviceAdminHooks() {
		Set<HookInfo> deviceAdminHoocks = new HashSet<HookInfo>();
		
		MethodHookInfo isAdminActive = new MethodHookInfo("<android.app.admin.DevicePolicyManager: boolean isAdminActive(android.content.ComponentName)>");
        isAdminActive.analysisDependentHookAfter();
        
        deviceAdminHoocks.add(isAdminActive);
        
        return deviceAdminHoocks;		
	}
	
	
	private Set<HookInfo> stringOperationHooks() {
		Set<HookInfo> stringOpHoocks = new HashSet<HookInfo>();
		
		MethodHookInfo charAt = new MethodHookInfo("<java.lang.String: char charAt(int)>");
		charAt.analysisDependentHookAfter();
		MethodHookInfo codePointAt = new MethodHookInfo("<java.lang.String: int codePointAt(int)>");
		codePointAt.analysisDependentHookAfter();
		MethodHookInfo codePointBefore = new MethodHookInfo("<java.lang.String: int codePointBefore(int)>");
		codePointBefore.analysisDependentHookAfter();
		MethodHookInfo codePointCount = new MethodHookInfo("<java.lang.String: int codePointCount(int, int)>");
		codePointCount.analysisDependentHookAfter();
		MethodHookInfo compareTo = new MethodHookInfo("<java.lang.String: int compareTo(java.lang.String)>");
		compareTo.analysisDependentHookAfter();
		MethodHookInfo compareToIgnoreCase = new MethodHookInfo("<java.lang.String: int compareToIgnoreCase(java.lang.String)>");
		compareToIgnoreCase.analysisDependentHookAfter();
		MethodHookInfo concat = new MethodHookInfo("<java.lang.String: java.lang.String concat(java.lang.String)>");
		concat.analysisDependentHookAfter();
		MethodHookInfo containsCharSequence = new MethodHookInfo("<java.lang.String: boolean contains(java.lang.CharSequence)>");
		containsCharSequence.analysisDependentHookAfter();
		MethodHookInfo contentEqualsCharSequence = new MethodHookInfo("<java.lang.String: boolean contentEquals(java.lang.CharSequence)>");
		contentEqualsCharSequence.analysisDependentHookAfter();
		MethodHookInfo contentEqualsStringBuffer = new MethodHookInfo("<java.lang.String: boolean contentEquals(java.lang.StringBuffer)>");
		contentEqualsStringBuffer.analysisDependentHookAfter();
		MethodHookInfo copyValueOf1 = new MethodHookInfo("<java.lang.String: java.lang.String copyValueOf(char[], int, int)>");
		copyValueOf1.analysisDependentHookAfter();
		MethodHookInfo copyValueOf2 = new MethodHookInfo("<java.lang.String: java.lang.String copyValueOf(char[])>");
		copyValueOf2.analysisDependentHookAfter();
		MethodHookInfo endsWith = new MethodHookInfo("<java.lang.String: boolean endsWith(java.lang.String)>");
		endsWith.analysisDependentHookAfter();
		MethodHookInfo equals = new MethodHookInfo("<java.lang.String: boolean equals(java.lang.Object)>");
		equals.analysisDependentHookAfter();
		MethodHookInfo equalsIgnoreCase = new MethodHookInfo("<java.lang.String: boolean equalsIgnoreCase(java.lang.String)>");
		equalsIgnoreCase.analysisDependentHookAfter();
		MethodHookInfo format1 = new MethodHookInfo("<java.lang.String: java.lang.String format(java.util.Locale, java.lang.String, java.lang.Object[])>");
		format1.analysisDependentHookAfter();
		MethodHookInfo format2 = new MethodHookInfo("<java.lang.String: java.lang.String format(java.lang.String, java.lang.Object[])>");
		format2.analysisDependentHookAfter();
		MethodHookInfo getBytes1 = new MethodHookInfo("<java.lang.String: void getBytes(int, int, byte[], int)>");
		getBytes1.analysisDependentHookAfter();
		MethodHookInfo getBytes2 = new MethodHookInfo("<java.lang.String: byte[] getBytes(java.lang.String)>");
		getBytes2.analysisDependentHookAfter();
		MethodHookInfo getBytes3 = new MethodHookInfo("<java.lang.String: byte[] getBytes(java.nio.charset.Charset)>");
		getBytes3.analysisDependentHookAfter();
		MethodHookInfo getBytes4 = new MethodHookInfo("<java.lang.String: byte[] getBytes()>");
		getBytes4.analysisDependentHookAfter();
		MethodHookInfo getChars = new MethodHookInfo("<java.lang.String: void getChars(int, int, char[], int)>");
		getChars.analysisDependentHookAfter();
		MethodHookInfo hashCode = new MethodHookInfo("<java.lang.String: int hashCode()>");
		hashCode.analysisDependentHookAfter();
		MethodHookInfo indexOf1 = new MethodHookInfo("<java.lang.String: int indexOf(int)>");
		indexOf1.analysisDependentHookAfter();
		MethodHookInfo indexOf2 = new MethodHookInfo("<java.lang.String: int indexOf(int, int)>");
		indexOf2.analysisDependentHookAfter();
		MethodHookInfo indexOf3 = new MethodHookInfo("<java.lang.String: int indexOf(java.lang.String, int)>");
		indexOf3.analysisDependentHookAfter();
		MethodHookInfo indexOf4 = new MethodHookInfo("<java.lang.String: int indexOf(java.lang.String)>");
		indexOf4.analysisDependentHookAfter();
		MethodHookInfo intern = new MethodHookInfo("<java.lang.String: java.lang.String intern()>");
		intern.analysisDependentHookAfter();
		MethodHookInfo isEmpty = new MethodHookInfo("<java.lang.String: boolean isEmpty()>");
		isEmpty.analysisDependentHookAfter();
		MethodHookInfo lastIndexOf1 = new MethodHookInfo("<java.lang.String: int lastIndexOf(java.lang.String)>");
		lastIndexOf1.analysisDependentHookAfter();
		MethodHookInfo lastIndexOf2 = new MethodHookInfo("<java.lang.String: int lastIndexOf(int, int)>");
		lastIndexOf2.analysisDependentHookAfter();
		MethodHookInfo lastIndexOf3 = new MethodHookInfo("<java.lang.String: int lastIndexOf(int)>");
		lastIndexOf3.analysisDependentHookAfter();
		MethodHookInfo lastIndexOf4 = new MethodHookInfo("<java.lang.String: int lastIndexOf(java.lang.String, int)>");
		lastIndexOf4.analysisDependentHookAfter();
		MethodHookInfo length = new MethodHookInfo("<java.lang.String: int length()>");
		length.analysisDependentHookAfter();
		MethodHookInfo matches = new MethodHookInfo("<java.lang.String: boolean matches(java.lang.String)>");
		matches.analysisDependentHookAfter();
		MethodHookInfo offsetByCodePoints = new MethodHookInfo("<java.lang.String: int offsetByCodePoints(int, int)>");
		offsetByCodePoints.analysisDependentHookAfter();
		MethodHookInfo regionMatches1 = new MethodHookInfo("<java.lang.String: boolean regionMatches(boolean, int, java.lang.String, int, int)>");
		regionMatches1.analysisDependentHookAfter();
		MethodHookInfo regionMatches2 = new MethodHookInfo("<java.lang.String: boolean regionMatches(int, java.lang.String, int, int)>");
		regionMatches2.analysisDependentHookAfter();
		MethodHookInfo replace1 = new MethodHookInfo("<java.lang.String: java.lang.String replace(java.lang.CharSequence, java.lang.CharSequence)>");
		replace1.analysisDependentHookAfter();
		MethodHookInfo replace2 = new MethodHookInfo("<java.lang.String: java.lang.String replace(char, char)>");
		replace2.analysisDependentHookAfter();
		MethodHookInfo split1 = new MethodHookInfo("<java.lang.String: java.lang.String[] split(java.lang.String)>");
		split1.analysisDependentHookAfter();
		MethodHookInfo split2 = new MethodHookInfo("<java.lang.String: java.lang.String[] split(java.lang.String, int)>");
		split2.analysisDependentHookAfter();
		MethodHookInfo startsWith1 = new MethodHookInfo("<java.lang.String: boolean startsWith(java.lang.String)>");
		startsWith1.analysisDependentHookAfter();
		MethodHookInfo startsWith2 = new MethodHookInfo("<java.lang.String: boolean startsWith(java.lang.String, int)>");
		startsWith2.analysisDependentHookAfter();
		MethodHookInfo subSequence = new MethodHookInfo("<java.lang.String: java.lang.CharSequence subSequence(int, int)>");
		subSequence.analysisDependentHookAfter();
		MethodHookInfo substring1 = new MethodHookInfo("<java.lang.String: java.lang.String substring(int)>");
		substring1.analysisDependentHookAfter();
		MethodHookInfo substring2 = new MethodHookInfo("<java.lang.String: java.lang.String substring(int, int)>");
		substring2.analysisDependentHookAfter();
		MethodHookInfo toCharArray = new MethodHookInfo("<java.lang.String: char[] toCharArray()>");
		toCharArray.analysisDependentHookAfter();
		MethodHookInfo toLowerCase1 = new MethodHookInfo("<java.lang.String: java.lang.String toLowerCase(java.util.Locale)>");
		toLowerCase1.analysisDependentHookAfter();
		MethodHookInfo toLowerCase2 = new MethodHookInfo("<java.lang.String: java.lang.String toLowerCase()>");
		toLowerCase2.analysisDependentHookAfter();
		MethodHookInfo toString = new MethodHookInfo("<java.lang.String: java.lang.String toString()>");
		toString.analysisDependentHookAfter();
		MethodHookInfo toUpperCase1 = new MethodHookInfo("<java.lang.String: java.lang.String toUpperCase(java.util.Locale)>");
		toUpperCase1.analysisDependentHookAfter();
		MethodHookInfo toUpperCase2 = new MethodHookInfo("<java.lang.String: java.lang.String toUpperCase()>");
		toUpperCase2.analysisDependentHookAfter();
		MethodHookInfo trim = new MethodHookInfo("<java.lang.String: java.lang.String trim()>");
		trim.analysisDependentHookAfter();
		MethodHookInfo valueOf1 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(long)>");
		valueOf1.analysisDependentHookAfter();
		MethodHookInfo valueOf2 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(java.lang.Object)>");
		valueOf2.analysisDependentHookAfter();
		MethodHookInfo valueOf3 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(char[])>");
		valueOf3.analysisDependentHookAfter();
		MethodHookInfo valueOf4 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(double)>");
		valueOf4.analysisDependentHookAfter();
		MethodHookInfo valueOf5 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(int)>");
		valueOf5.analysisDependentHookAfter();
		MethodHookInfo valueOf6 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(float)>");
		valueOf6.analysisDependentHookAfter();
		MethodHookInfo valueOf7 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(char[], int, int)>");
		valueOf7.analysisDependentHookAfter();
		MethodHookInfo valueOf8 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(boolean)>");
		valueOf8.analysisDependentHookAfter();
		MethodHookInfo valueOf9 = new MethodHookInfo("<java.lang.String: java.lang.String valueOf(char)>");
		valueOf9.analysisDependentHookAfter();
		
//		stringOpHoocks.add(charAt);
//		stringOpHoocks.add(codePointAt);
//		stringOpHoocks.add(codePointBefore);
//		stringOpHoocks.add(codePointCount);
//		stringOpHoocks.add(compareTo);
//		stringOpHoocks.add(compareToIgnoreCase);
//		stringOpHoocks.add(concat);
//		stringOpHoocks.add(containsCharSequence);
//		stringOpHoocks.add(contentEqualsCharSequence);
//		stringOpHoocks.add(contentEqualsStringBuffer);
//		stringOpHoocks.add(copyValueOf1);
//		stringOpHoocks.add(copyValueOf2);
//		stringOpHoocks.add(endsWith);
//		stringOpHoocks.add(equals);
//		stringOpHoocks.add(equalsIgnoreCase);
//		stringOpHoocks.add(format1);
//		stringOpHoocks.add(format2);
//		stringOpHoocks.add(getBytes1);
//		stringOpHoocks.add(getBytes2);
//		stringOpHoocks.add(getBytes3);
//		stringOpHoocks.add(getBytes4);
//		stringOpHoocks.add(getChars);
//		stringOpHoocks.add(hashCode);
//		stringOpHoocks.add(indexOf1);
//		stringOpHoocks.add(indexOf2);
//		stringOpHoocks.add(indexOf3);
//		stringOpHoocks.add(indexOf4);
//		stringOpHoocks.add(intern);
//		stringOpHoocks.add(isEmpty);
//		stringOpHoocks.add(lastIndexOf1);
//		stringOpHoocks.add(lastIndexOf2);
//		stringOpHoocks.add(lastIndexOf3);
//		stringOpHoocks.add(lastIndexOf4);
//		stringOpHoocks.add(length);
//		stringOpHoocks.add(matches);
//		stringOpHoocks.add(offsetByCodePoints);
//		stringOpHoocks.add(regionMatches1);
//		stringOpHoocks.add(regionMatches2);
//		stringOpHoocks.add(replace1);
//		stringOpHoocks.add(replace2);
//		stringOpHoocks.add(split1);
//		stringOpHoocks.add(split2);
//		stringOpHoocks.add(startsWith1);
//		stringOpHoocks.add(startsWith2);
//		stringOpHoocks.add(subSequence);
//		stringOpHoocks.add(substring1);
//		stringOpHoocks.add(substring2);
//		stringOpHoocks.add(toCharArray);
//		stringOpHoocks.add(toLowerCase1);
//		stringOpHoocks.add(toLowerCase2);
//		stringOpHoocks.add(toString);
//		stringOpHoocks.add(toUpperCase1);
//		stringOpHoocks.add(toUpperCase2);
//		stringOpHoocks.add(trim);
//		stringOpHoocks.add(valueOf1);
//		stringOpHoocks.add(valueOf2);
//		stringOpHoocks.add(valueOf3);
//		stringOpHoocks.add(valueOf4);
//		stringOpHoocks.add(valueOf5);
//		stringOpHoocks.add(valueOf6);
//		stringOpHoocks.add(valueOf7);
//		stringOpHoocks.add(valueOf8);
//		stringOpHoocks.add(valueOf9);
		
		
		return stringOpHoocks;
	}
	
	
	private Set<HookInfo> applicationInfoHooks() {
		Set<HookInfo> appInfoHooks = new HashSet<HookInfo>();
				
		MethodHookInfo loadLabel = new MethodHookInfo("<android.content.pm.PackageItemInfo: java.lang.CharSequence loadLabel(android.content.pm.PackageManager)>");
		loadLabel.analysisDependentHookAfter();

		appInfoHooks.add(loadLabel);
		
		return appInfoHooks;
	}
	
	
	private Set<HookInfo> smsMessageHooks() {
		Set<HookInfo> smsMsgHooks = new HashSet<HookInfo>();

		MethodHookInfo displayOriginatingAddress1 = new MethodHookInfo("<android.telephony.SmsMessage: java.lang.String getDisplayOriginatingAddress()>");																		
		displayOriginatingAddress1.analysisDependentHookAfter();
		MethodHookInfo displayOriginatingAddress2 = new MethodHookInfo("<android.telephony.SmsMessage: java.lang.String getOriginatingAddress()>");																		
		displayOriginatingAddress2.analysisDependentHookAfter();
		MethodHookInfo displayOriginatingAddress3 = new MethodHookInfo("<android.telephony.gsm.SmsMessage: java.lang.String getOriginatingAddress()>");																		
		displayOriginatingAddress3.analysisDependentHookAfter();
		MethodHookInfo displayMessageBody1 = new MethodHookInfo("<android.telephony.SmsMessage: java.lang.String getDisplayMessageBody()>");
		displayMessageBody1.analysisDependentHookAfter();
		MethodHookInfo displayMessageBody2 = new MethodHookInfo("<android.telephony.SmsMessage: java.lang.String getMessageBody()>");
		displayMessageBody2.analysisDependentHookAfter();
		MethodHookInfo displayMessageBody3 = new MethodHookInfo("<android.telephony.gsm.SmsMessage: java.lang.String getDisplayMessageBody()>");
		displayMessageBody3.analysisDependentHookAfter();

		
		smsMsgHooks.add(displayOriginatingAddress1);
		smsMsgHooks.add(displayOriginatingAddress2);
		smsMsgHooks.add(displayOriginatingAddress3);
		smsMsgHooks.add(displayMessageBody1);
		smsMsgHooks.add(displayMessageBody2);
		smsMsgHooks.add(displayMessageBody3);
		
		return smsMsgHooks;
	}
	
	
	private Set<HookInfo> sharedPreferencesHooks() {
		Set<HookInfo> sharedPrefHooks = new HashSet<HookInfo>();
		
		MethodHookInfo getInt = new MethodHookInfo("<android.app.SharedPreferencesImpl: int getInt(java.lang.String, int)>");
		getInt.analysisDependentHookAfter();
		MethodHookInfo getBoolean = new MethodHookInfo("<android.app.SharedPreferencesImpl: boolean getBoolean(java.lang.String, boolean)>");
		getBoolean.analysisDependentHookAfter();
		MethodHookInfo getFloat = new MethodHookInfo("<android.app.SharedPreferencesImpl: float getFloat(java.lang.String, float)>");
		getFloat.analysisDependentHookAfter();
		MethodHookInfo getLong = new MethodHookInfo("<android.app.SharedPreferencesImpl: long getLong(java.lang.String, long)>");
		getLong.analysisDependentHookAfter();
		MethodHookInfo getString = new MethodHookInfo("<android.app.SharedPreferencesImpl: java.lang.String getString(java.lang.String, java.lang.String)>");
		getString.analysisDependentHookAfter();
		MethodHookInfo getStringSet = new MethodHookInfo("<android.app.SharedPreferencesImpl: java.util.Set getStringSet(java.lang.String, java.util.Set)>");
		getStringSet.analysisDependentHookAfter();
		
		sharedPrefHooks.add(getInt);
		sharedPrefHooks.add(getBoolean);
		sharedPrefHooks.add(getFloat);
		sharedPrefHooks.add(getLong);
		sharedPrefHooks.add(getString);
		sharedPrefHooks.add(getStringSet);
		
		return sharedPrefHooks;
	}

	
	private Set<HookInfo> networkRelatedHooks() {
		Set<HookInfo> networkHooks = new HashSet<HookInfo>();
				
		MethodHookInfo getByName = new MethodHookInfo("<java.net.InetAddress: java.net.InetAddress getByName(java.lang.String)>");
		getByName.analysisDependentHookAfter();
		MethodHookInfo getHostAddress = new MethodHookInfo("<java.net.InetAddress: java.lang.String getHostAddress()>");
		getHostAddress.analysisDependentHookAfter();
		MethodHookInfo urlConstructor = new MethodHookInfo("<java.net.URL: void <init>(java.lang.String)>");
		urlConstructor.analysisDependentHookBefore();
		MethodHookInfo getResponseCode = new MethodHookInfo("<java.net.HttpURLConnection: int getResponseCode()>");
		getResponseCode.analysisDependentHookAfter();
		MethodHookInfo getInputStream = new MethodHookInfo("<com.android.okhttp.internal.http.HttpURLConnectionImpl: java.io.InputStream getInputStream()>");
		getInputStream.analysisDependentHookAfter();
		
//		networkHooks.add(getByName);
//		networkHooks.add(getHostAddress);
		networkHooks.add(urlConstructor);
		networkHooks.add(getResponseCode);
		networkHooks.add(getInputStream);
		
		return networkHooks;
	}
	
	private Set<HookInfo> foregroundActivityCheckHooks() {
		Set<HookInfo> foregroundHooks = new HashSet<HookInfo>();
		
		MethodHookInfo getByName = new MethodHookInfo("<android.content.ComponentName: java.lang.String getPackageName()>");
		getByName.analysisDependentHookAfter();		
		
		return foregroundHooks;
	}
	
	private Set<HookInfo> integrityRelatedHooks(){
		Set<HookInfo> integrityHooks = new HashSet<HookInfo>();
			
		
		MethodHookInfo integrity1 = new MethodHookInfo("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: android.content.pm.PackageInfo dummyWrapper_getPackageInfo(android.content.pm.PackageManager,java.lang.String,int)>");
		integrity1.analysisDependentHookAfter();
		
		integrityHooks.add(integrity1);
		
		return integrityHooks;
	}
	
	private Set<HookInfo> fileRelatedHooks() {
		Set<HookInfo> fileHooks = new HashSet<HookInfo>();
		
		MethodHookInfo contextOpenFile = new MethodHookInfo("<android.app.ContextImpl: java.io.FileInputStream openFileInput(java.lang.String)>");
		contextOpenFile.analysisDependentHookBefore();
		
		MethodHookInfo propertyString1 = new MethodHookInfo("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: java.lang.String dummyWrapper_getProperty(java.util.Properties,java.lang.String)>");
		propertyString1.analysisDependentHookAfter();
		MethodHookInfo propertyString2 = new MethodHookInfo("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: java.lang.String dummyWrapper_getProperty(java.util.Properties,java.lang.String,java.lang.String)>");
		propertyString2.analysisDependentHookAfter();
		
		fileHooks.add(contextOpenFile);
		fileHooks.add(propertyString1);
		fileHooks.add(propertyString2);
		
		return fileHooks;
	}
	
	private Set<HookInfo> reflectionHooks() {
		Set<HookInfo> reflectionHooks = new HashSet<HookInfo>();
		
		MethodHookInfo getMethodHookAfter = new MethodHookInfo("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: java.lang.reflect.Method dummyWrapper_getMethod(java.lang.Class,java.lang.String,java.lang.Class[])>");
		getMethodHookAfter.analysisDependentHookAfter();
		
		reflectionHooks.add(getMethodHookAfter);
		
		return reflectionHooks;		
	}
}
