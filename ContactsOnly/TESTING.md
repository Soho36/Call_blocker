# Physical-phone checks

These checks are not yet run. Compilation, lint and unit tests cannot substitute for them.

Use two ordinary phones. Do not test by calling emergency services. Keep Do Not Disturb off while checking ringing, and make sure the test caller is not already blocked by your Phone app or carrier.

| Check | Expected result |
| --- | --- |
| First launch | Blocking is off; setup button is available if the role exists. |
| Decline Android setup dialog | App stays inactive; switch cannot be enabled. |
| Accept Android setup dialog | App says selected; blocking remains off until enabled. |
| Blocking on, saved contact calls | Phone rings; normally no screening entry. |
| Blocking on, unsaved number calls | No ringing; app records “Rejection requested”. Caller may reach voicemail. |
| Repeat that unsaved call | Still rejected; no repeat-caller bypass is implemented. |
| Pause for 30 minutes | Unsaved call rings; history records “Allowed”. |
| Tap resume | Next unsaved call is rejected. |
| Let 30 minutes expire with app closed | Next unsaved call is rejected. |
| Switch blocking off | Unsaved call rings. |
| Make an outgoing call | Call proceeds normally; no outgoing history entry. |
| Lock screen / swipe app from Recents | Next unsaved call should still be screened. |
| Restart and unlock once | Enabled setting persists; repeat saved/unsaved calls. |
| Select another screening app | This app no longer screens; reopening shows setup required. |
| Add the previously unsaved number to Contacts | After the contact is saved/synced, a new call rings. |
| Call with private/hidden ID | Outside this app's scope; inspect Phone app settings separately. |
| Clear activity | All local entries disappear. |
| Two SIMs / Wi-Fi calling, if used | Repeat saved and unsaved calls for each configuration. |
| Large text / landscape | Controls remain readable and reachable by scrolling. |

If a call slips through, record phone model, Android version, whether the app still holds the role, switch/pause state, whether a history entry appeared, caller-ID visibility and SIM used. A missing history entry suggests Android never delivered the call to the service; a recorded rejection with ringing suggests platform/carrier behaviour needs investigation.

Forced stopping, clearing app data, permission/role changes and manufacturer's battery restrictions can affect operation. Do not force-stop the app as a normal way of closing it.
