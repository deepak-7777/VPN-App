# 🔐 VPN Android App

A **production-ready WireGuard-based VPN Android application** built using modern Android architecture and best practices.

This app provides a secure, fast, and user-friendly VPN experience with real-time connection state handling, server selection, and tunnel management using the official WireGuard backend.

---

## 🚀 Features

* 🔒 VPN connection using **WireGuard protocol**
* 🌍 Multiple server selection (country + city based)
* ⚡ Fast connect / disconnect functionality
* 📡 Real-time connection status (Connected / Connecting / Disconnected)
* ⏱️ Session timer tracking
* 🌐 Current IP display
* 📶 Server ping indicator with dynamic UI colors
* 🎨 Modern and responsive UI
* 🔄 Live state updates using ViewModel + LiveData
* 🔔 Foreground VPN service with notification support
* 🛡️ Proper VPN permission handling

---

## 🧱 Tech Stack

**Language**

* Kotlin

**Architecture**

* MVVM (Model-View-ViewModel)

**Android Components**

* Fragment
* ViewModel
* LiveData
* Navigation Component
* Data Binding / View Binding

**Networking**

* Retrofit

**VPN Core**

* Official WireGuard Android library (`GoBackend`)

---

## 📂 Project Structure

```bash
com.securevpn.app
│
├── ui
│   ├── home
│   │   ├── HomeFragment.kt
│   │   ├── HomeViewModel.kt
│   │
│   ├── servers
│   ├── settings
│
├── data
│   ├── model
│   │   ├── ServerItem.kt
│   │   ├── WireGuardConfigDto.kt
│   │
│   ├── repository
│   │   ├── VpnRepository.kt
│
├── vpn
│   ├── WireGuardManager.kt
│
├── service
│   ├── SecureVpnService.kt
│
├── network
│   ├── RetrofitClient.kt
│
├── utils
│   ├── Constants.kt
│   ├── Extensions.kt
```

---

## 🔌 VPN Flow

1. User selects a server
2. User taps **Connect**
3. App requests VPN permission
4. Backend provides WireGuard config
5. App builds `Config` object
6. Tunnel is started using:

```kotlin
GoBackend.setState(tunnel, Tunnel.State.UP, config)
```

7. Connection state updates in UI

---

## 📡 Connection States

| State        | Description                 |
| ------------ | --------------------------- |
| DISCONNECTED | VPN is not active           |
| CONNECTING   | Tunnel is being established |
| CONNECTED    | VPN is active and secured   |

---

## 🧩 Key Components

### 🔹 SecureVpnService

* Extends `VpnService`
* Handles tunnel lifecycle
* Runs as foreground service

### 🔹 HomeViewModel

* Manages connection state
* Handles connect / disconnect logic
* Observes backend responses

### 🔹 WireGuardManager

* Validates config
* Manages tunnel state cache

### 🔹 VpnRepository

* Communicates with backend APIs
* Fetches WireGuard configuration

---

## ⚙️ Permissions

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

VPN permission handled via:

```kotlin
VpnService.prepare(context)
```

---

## 🖼️ UI Highlights

* Animated connect button
* Pulse effect during connecting
* Dynamic status banner (red / yellow / green)
* Clean card-based server UI
* Real-time stats (download / upload placeholder)

---

## 🔐 Security Notes

* No sensitive data is stored locally
* VPN keys are provided securely from backend
* No traffic logs are collected
* Follows privacy-first architecture

---

## 🧪 Current Status

✔ UI completed
✔ VPN service integrated
✔ WireGuard backend integration ready
✔ Server selection working
✔ Connection state handling implemented

🚧 Backend provisioning integration in progress

---

## 📌 Future Improvements

* 🔄 Real-time traffic stats (download/upload)
* 🌐 Multi-server load balancing
* 🔐 Authentication system
* 💳 Premium subscription support
* 📊 Usage analytics dashboard
* 🌍 Auto server selection (best latency)

---

## 👨‍💻 Developer

**Deepak Kumar**

Android & Backend Developer

---

## 📄 License

This project is for educational and development purposes.
Production deployment requires proper security, infrastructure, and compliance handling.

---

## ⭐ Support

If you like this project, consider giving it a ⭐ on GitHub!
