# GANOMplayer

Minecraft Plugin using GAN

### TODO
- [X] Socket connection
- [X] NPC
- [X] Behavior bug fix
- [X] Making Java not to close socket when training stops
- [X] GAN

### Socket Communication Format
```json
{
    "ai": [
      {
        "WSmove": 1,
        "ADmove": 0,
        "Space": 0,
        "Ctrl": 1,
        "Shift": 0,
        "DelYaw": 15.433,
        "DelPitch": 9.467,
        "Pitch": 91.45,
        "Attack": -1
      }
    ],
    "players": [
      "almost same format as above",
      "excluding Pitch"
    ]
}
```
* WSMove/ADmove: -1, 0, 1
* Space, Ctrl, Shift: 0, 1
* DelYaw, DelPitch, Pitch(input only): float [degrees/frame]
* Attack: -1, 0
<!--
### previous format

#### input
- isOnDamage [bool]
- isOnGround [bool]
- isSneaking [bool]
- isSprinting [bool]
- pitch [float]
- velocity (yaw-relative) [float, float, float]

#### output
- rotation (yaw, pitch) [float, float]
- velocity (x, y, z) [float, float, float]
- isSneaking [bool]
- isSprinting [bool]
- attackIndex [int]
-->
