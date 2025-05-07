# 📞 TrueCaller Clone Android App

A powerful **TrueCaller Clone** Android application that detects incoming/outgoing calls, blocks spam/unwanted numbers, shows caller info in a floating popup, and notifies users of blocked or spam calls. Built using Android core components like Services, BroadcastReceivers, and CallScreeningService.

---

## 🚀 Features

- Detect **incoming and outgoing calls**
- **Block spam or unwanted calls** based on settings
- Show **caller information in a floating window**
- Use Android's **CallScreeningService** for handling calls
- Store user preferences like block settings using `SharedPreferences`
- Notify users about **blocked or spam calls**
- Works even in the **background**

---

## 📲 Call Flow Overview

### For Incoming Calls:

1. App receives call event via `CallScreeningService`.
2. Checks user preferences:
   - Reject All → Call blocked.
   - Reject Unknown → Block if contact is not saved.
3. If spam filtering is enabled:
   - Use `CallsControlHelper` to verify spam.
   - If spam → Block + notify using `NotificationHelper`.
   - If not spam → Show floating popup using `PopupService`.

### For Outgoing Calls:

- If contact is unknown and setting is ON → Show floating window.

---

## 🛡 Permissions Used

| Permission | Purpose |
|-----------|---------|
| `READ_CALL_LOG` | To access call details |
| `CALL_SCREENING` | To screen incoming calls |
| `SYSTEM_ALERT_WINDOW` | To show floating caller info |
| `READ_CONTACTS` | To identify saved contacts |
| `FOREGROUND_SERVICE` | To run background call monitoring |
| `INTERNET` | For spam check and network calls |

---

## 📐 Architecture & Flow

### App Flow:

1. App launches, requests required permissions.
2. Background services start to monitor calls.
3. BroadcastReceivers detect call state changes.
4. Caller info is retrieved and shown in popup.
5. Spam data is fetched via helper classes.
6. User can block, save, or ignore the number.

### 🔧 Class Structure:

| Type | Role |
|------|------|
| `Activities` | Handle UI and permission management |
| `Services` | Background logic for monitoring & popups |
| `BroadcastReceivers` | Detect call events |
| `Helper Classes` | Contact lookup, spam detection, popups |
| `Utils` | SharedPreferences, formatting |

---

## 🛠 Technologies Used

- Android SDK (Java/Kotlin)
- CallScreeningService
- Services (Foreground & Background)
- BroadcastReceivers
- SharedPreferences
- Android Permissions
- SYSTEM_ALERT_WINDOW
- Dependency Injection (if used)

---

## 🔮 Future Scope

- ✅ Spam Call Reporting
- ☁️ Cloud Backup of call logs
- 📊 Analytics Dashboard for user insights

---

## 📸 Screenshots



<img src="https://github.com/user-attachments/assets/3eb3fa01-c08c-49bf-8a73-dfc29b2282af" width="100"/> <img src="https://github.com/user-attachments/assets/64155d0f-c566-4e23-b095-cbfc0710d924" width="100"/>
<img src="https://github.com/user-attachments/assets/81299adf-258a-4cf2-824c-6ab1f6e7e6c6" width="100"/> <img src="https://github.com/user-attachments/assets/9c5c9427-7986-46b9-9a8b-a8a1603ae90c" width="100"/>
 <img src="https://github.com/user-attachments/assets/a209eae7-88eb-41e0-9abb-10992efb62de" width="100"/><img src="https://github.com/user-attachments/assets/47facec9-e078-499b-bda9-ff7c88c2885a" width="100"/> <img src="https://github.com/user-attachments/assets/6e320c84-e9b7-44ae-a15c-d2cf79059e33" width="100"/>

| Incoming Call Screen | Caller Info Popup | Settings Page |
|----------------------|-------------------|----------------|


---

## 🎯 Purpose

This app is built to **learn and demonstrate** how a call identification application like **Truecaller** functions internally. It focuses on call detection, spam filtering, background processing, permission handling, and displaying user-friendly popups using Android native tools.

---

## 📦 Installation

1. Clone the repo:
   ```bash
   git clone https://github.com/yourusername/truecaller-clone-android.git


---

## 📥 Download APK

👉 **[Download the Latest Version (.APK)](https://github.com/jester-sys/TrueCallerApp/packages)**

> Click the link above to download and install the latest release of the app directly to your Android phone. Make sure you allow installation from unknown sources in your device settings.

---
