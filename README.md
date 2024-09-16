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
        "WSmove": 1,  // 0 or 1
        "ADmove": 0,
        "Space": 0,
        "Ctrl": 1,
        "Shift": 0,
        "DelYaw": 15.433,  // degrees/frame
        "DelPitch": 9.467,  // degrees/frame
        "Attack": -1  // -1 or 0
      }
    ],
    "player": [
      "same format as above"
    ]
}
```
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
