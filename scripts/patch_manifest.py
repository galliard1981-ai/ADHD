"""Προσθέτει στο AndroidManifest.xml τα δικαιώματα μικροφώνου και την ορατότητα της υπηρεσίας αναγνώρισης ομιλίας."""
import re, pathlib
p = pathlib.Path('android/app/src/main/AndroidManifest.xml')
s = p.read_text(encoding='utf8')
perms = ['android.permission.RECORD_AUDIO', 'android.permission.INTERNET', 'android.permission.VIBRATE', 'android.permission.WAKE_LOCK']
for perm in perms:
    if perm not in s:
        s = s.replace('</manifest>', f'    <uses-permission android:name="{perm}" />\n</manifest>')
if 'android.speech.RecognitionService' not in s:
    s = s.replace('</manifest>', '    <queries>\n        <intent>\n            <action android:name="android.speech.RecognitionService" />\n        </intent>\n        <intent>\n            <action android:name="android.intent.action.TTS_SERVICE" />\n        </intent>\n    </queries>\n</manifest>')
p.write_text(s, encoding='utf8')
print(s)
