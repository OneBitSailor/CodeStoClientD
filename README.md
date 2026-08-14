## CodeSto Desktop Client

A Kotlin Multiplatform desktop app for quickly sending code snippets to the CodeSto Android app over your local Wi-Fi network. Paste, format, and send — the snippet shows up instantly in CodeSto's searchable library on your phone.

<img width="1912" height="1078" alt="CodeStoClientDesktop" src="https://github.com/user-attachments/assets/4160d4fc-bd14-49b9-aa47-a063d4ff46ec" />


## Features
🔌 Live connection status — auto-pings your phone as you type its address, so you know before you hit send whether it's reachable
✨ Auto-format — bracket-depth re-indenting for snippets that lose formatting on paste
🔤 Adjustable font size & bold for comfortable editing of large snippets
📤 Animated send flow — clear sending → sent/error states, no guessing whether it went through
⚡ Built on Ktor client, so it's fast and dependency-light
## Requirements
JDK 17+
CodeSto Android app installed and running on your phone, with its embedded server started
Desktop and phone connected to the same Wi-Fi network.

## Getting started
1. Clone and run
bash
git clone https://github.com/<your-username>/codesto-desktop-client.git
cd codesto-desktop-client
./gradlew run
2. Start the server on your phone

Open CodeSto on your phone, go to the connection screen, and tap Start server. Once it turns green, note the IP and port shown (e.g. 192.168.1.42:8080).

3. Connect

In the desktop app, paste that address into the Phone address field. Watch the status badge:

Badge	Meaning
⚪ Idle	No address entered yet
🟠 Checking…	Pinging the phone
🟢 Connected	Phone reachable, ready to send
🔴 Not reachable	See Troubleshooting
4. Send a snippet

Fill in a file name, paste your code, optionally hit Format to clean up indentation, then Send to phone. It'll appear immediately in CodeSto's recent list.

## Troubleshooting

Badge stuck on "Not reachable"

Double-check the IP — it can change if the phone reconnects to Wi-Fi. Confirm it matches what's currently shown on the phone.
Make sure both devices show the same Wi-Fi network name.
Some routers isolate devices from each other even on the same network (look for "AP isolation" / "client isolation" in your router settings) — disable it if present.
Confirm the server is actually running on the phone (green status card, not red).

Send fails after connecting successfully

The phone's server may have stopped between the ping and the send — reconnect and retry.
Check the error message shown next to the Send button; it surfaces the phone's actual HTTP response when available.

## Tech stack
Kotlin Multiplatform / Compose Desktop
Ktor Client (CIO engine)
kotlinx.serialization
License
