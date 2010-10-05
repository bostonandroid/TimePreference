package org.bostonandroid.timepreferencetest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.bostonandroid.timepreference.TimePreference;

import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

public class TimePreferenceActivityTest extends
    ActivityInstrumentationTestCase2<TimePreferenceActivity> {
  
  public TimePreferenceActivityTest() {
    super("org.bostonandroid.timepreferencetest", TimePreferenceActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setActivityInitialTouchMode(false);
  }

  public void testDefaultDateUnset() {
    TimePreferenceActivity activity = getActivity();
    PreferenceManager preferenceManager = activity.getPreferenceManager();
    TimePreference datePreference = (TimePreference)preferenceManager.findPreference("wake_up");
    Calendar givenTime = datePreference.getTime();
    assertCalendarTimeEquals(defaultTime(), givenTime);
    String defaultSummary = (String)datePreference.getSummary();
    assertNull("expected null but got " + defaultSummary, defaultSummary);
  }
  
  public void testDefaultDateFromXml() {
    TimePreferenceActivity activity = getActivity();
    PreferenceManager preferenceManager = activity.getPreferenceManager();
    TimePreference datePreference = (TimePreference)preferenceManager.findPreference("alert_at");
    Calendar defaultDate = datePreference.getTime();
    String newDateSummary = (String)datePreference.getSummary();
    assertNotNull(newDateSummary);
    Calendar expected = new GregorianCalendar(1970,0,1,14,1);
    assertCalendarTimeEquals(expected, defaultDate);
    assertSummary(expected, newDateSummary);
  }
  
  public void testDefaultDateForBadDate() {
    TimePreferenceActivity activity = getRestoredActivity();
    PreferenceManager preferenceManager = activity.getPreferenceManager();
    TimePreference datePreference = (TimePreference)preferenceManager.findPreference("resume_at");
    Calendar defaultTime = datePreference.getTime();
    assertCalendarTimeEquals(defaultTime(), defaultTime);
  }
  
  public void testDateChanged() {
    TimePreferenceActivity activity = getRestoredActivity();
    assertNotNull(activity);
    
    activateResumeAtPreference();
    
    // increment the minute
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_CENTER);

    pressOK();
    
    Calendar newTime = getTimePreference(activity, "resume_at").getTime();
    Calendar expected = defaultTime();
    expected.add(Calendar.MINUTE, 1);
    String newDateSummary = getTimePreference(activity, "resume_at").getSummary().toString();
    
    assertCalendarTimeEquals(expected, newTime);
    assertSummary(expected, newDateSummary);
  }
  
  public void testDateCanceled() {
    TimePreferenceActivity activity = getRestoredActivity();
    assertNotNull(activity);
    
    activateResumeAtPreference();

    // increment the day
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_CENTER);
    
    pressCancel();
    
    Calendar newDate = getTimePreference(activity, "resume_at").getTime();
    Calendar expected = defaultTime();
    
    assertCalendarTimeEquals(expected, newDate);
  }
  
  public void testDateEdited() {
    TimePreferenceActivity activity = getRestoredActivity();
    assertNotNull(activity);
    
    activateResumeAtPreference();

    // change the day
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DEL,
        KeyEvent.KEYCODE_DEL,
        KeyEvent.KEYCODE_0,
        KeyEvent.KEYCODE_2);


    final TimePreference datePreference = getTimePreference(activity, "resume_at");
    activity.runOnUiThread(new Runnable() {
      public void run() {
        clickOK(datePreference);
      }
    });
    getInstrumentation().waitForIdleSync();
    
    Calendar newDate = datePreference.getTime();
    Calendar expected = defaultTime();
    expected.add(Calendar.MINUTE, 2);
    
    assertCalendarTimeEquals(expected, newDate);
  }
  
  public void testCancelThenOK() {
    TimePreferenceActivity activity = getRestoredActivity();
    assertNotNull(activity);
    
    activateResumeAtPreference();

    // increment the day
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_CENTER);
    
    pressCancel();
    activateResumeAtPreference();
    pressOK();
    activateResumeAtPreference();
    pressOK();

    Calendar newDate = getTimePreference(activity, "resume_at").getTime();
    Calendar expected = defaultTime();
    
    assertCalendarTimeEquals(expected, newDate);
  }
  
  public void testSharedPreferences() {
    TimePreferenceActivity activity = getRestoredActivity();
    assertNotNull(activity);
    
    activateAlertAtPreference();
    // increment the day
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_CENTER);
    pressOK();

    Calendar expected = defaultTime();
    expected.add(Calendar.HOUR, 14);
    expected.add(Calendar.MINUTE, 2);
    assertCalendarTimeEquals(expected, activity.getCalendarOfAlert());
  }
  
  public void testDestroyedAndPresistence() {
    TimePreferenceActivity activity = getRestoredActivity();
    assertNotNull(activity);
    
    activateAlertAtPreference();
    // increment the minute
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_CENTER);
    pressOK();
    activateResumeAtPreference(); // android:persistent=false
    // increment the minute
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_CENTER);
    pressOK();
    activity.finish();
    activity = getActivity();
 
    Calendar expected = defaultTime();
    expected.add(Calendar.HOUR, 14);
    expected.add(Calendar.MINUTE, 2);

    assertCalendarTimeEquals(expected, getTimePreference(activity, "alert_at").getTime());
//    assertCalendarDateEquals(defaultDate(), getDatePreference(activity, "wake_up").getTime()); // not yet possible to test
  }
  
  private Calendar defaultTime() {
    return new GregorianCalendar(1970, 0, 1, 0, 0);
  }
  
  protected void assertCalendarTimeEquals(Calendar expected, Calendar actual) {
    String msg = "Expected: " + formatter().format(expected.getTime()) + " but got: " + formatter().format(actual.getTime());
    assertEquals(msg, expected.get(Calendar.HOUR), actual.get(Calendar.HOUR));
    assertEquals(msg, expected.get(Calendar.MINUTE), actual.get(Calendar.MINUTE));
  }
  
  protected void assertSummary(Calendar expected, String actual) {
    assertEquals(summaryFormatter().format(expected.getTime()), actual);
  }

  
  private SimpleDateFormat formatter() {
    return new SimpleDateFormat("HH:mm");
  }
  
  private DateFormat summaryFormatter() {
    return DateFormat.getTimeInstance();
  }
  
  private void pressOK() {
    sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_CENTER);
  }
  
  // actually different than pressing OK
  // must run within the UI thread
  private void clickOK(TimePreference dialog) {
    dialog.onClick(null, DialogInterface.BUTTON1);
  }
  
  private void pressCancel() {
      sendKeys(KeyEvent.KEYCODE_DPAD_DOWN,
          KeyEvent.KEYCODE_DPAD_DOWN,
          KeyEvent.KEYCODE_DPAD_DOWN,
          KeyEvent.KEYCODE_DPAD_RIGHT,
          KeyEvent.KEYCODE_DPAD_CENTER);
  }
  
  private void activateAlertAtPreference() {
    sendKeys(KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_CENTER);
  }
  
  private void activateResumeAtPreference() {
    sendKeys(KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_CENTER);
  }
  
  private TimePreference getTimePreference(TimePreferenceActivity activity, String field) {
    PreferenceManager preferenceManager = activity.getPreferenceManager();
    TimePreference datePreference = (TimePreference)preferenceManager.findPreference(field);
    return datePreference;
  }
  
  private TimePreferenceActivity getRestoredActivity() {
    TimePreferenceActivity a = getActivity();
//    a.clearSharedPreferences();
    getTimePreference(a,"alert_at").setDate("14:01");
    getTimePreference(a,"wake_up").setDate("00:00");
    getTimePreference(a,"resume_at").setDate("abcde");
    return a;
  }
}
