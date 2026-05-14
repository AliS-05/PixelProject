# Description

PixelProject is a real-time collaborative pixel art editor built in Java and JavaFX that allows users to work on the same drawing at the same time!
It has a custom file format, undo/redo, different tools, and synchronized multi-user editing.
The applications base is a grid based canvas to create pixel art using familiar tools.
The standout feature is the real-time multiplayer collaboration that allows multiple users to connect to a host instance and draw on the same canvas simultaneously. 
All changes made are broadcasted to all clients synchronously.

## Contributors
- Ali Sefraoui (File Format, Networking Logic, Main Tools)
- Mason DeJesus (Saving / Loading Files)
- Daniel Corona (UI)
- Jaden Williams (Bug fixes, Application Testing)
- Sebastian Adell (Grid Logic)

# Key Features
- Drawing Tools: Pencil, Eraser, Fill, and Clear tools
- RGB Color Picker
- Grid Resizing: Allows for NxM grid sizing with toggleable grid lines
- Undo/Redo
- File Management: Custom .pxbmp binary format that serializes JavaFX Color objects
- Real-time Collaboration:
	- Host a session for others to join
	- Multiple concurrent connections
	- Synchronized operations, including draw, erase, undo, redo
	- Authoritative server prevents state conflicts


# Requirements
- Java 25+
- Maven
- JavaFX 21+
- Internet Access for multiplayer
- Tailscale (sign up with email, free to use)

# Using PixelProject

To simply test the core functionality

## Terminal
```
git clone https://github.com/AliS-05/PixelProject
cd PixelProject

mvn clean javafx:run
```

## IntelliJ

1. Press the run button
2. If the run button doesn't work for some reason 
- Click the maven button -> Plugins -> javafx -> javafx:run
<img width="672" height="414" alt="image" src="https://github.com/user-attachments/assets/3b0b20c5-d89f-42fd-994d-cae0fad7e0b1" />

# Multiplayer

## NOTE
You MUST have Tailscale, or a similar solution
OR two instances on the same local machine with 127.0.0.1

Once you have Tailscale set up ON two machines

### Hosting a Session
1. Launch the application and select "Host Session"
2. Done

## Joining a Session
1. Launch and click "Join Session"
2. Type in your host machine's Tailscale IP.
3. Done

Once you do that you should see updates on both machines simultaneously.

# Saving and Loading Files

To save just click the Save button and the location in which you like the file to be located on your machine

To load similarly click load and select the file you would like to load.

# Known Issues

- Resizing grid can possibly corrupt / move current canvas state
- Loading files may result in the file being shifted to the top left
- General edge cases are not checked vigorously, be careful and the app should be fine
