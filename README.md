# GANOMplayer

Minecraft Plugin using GAN

### TODO
- [X] Socket connection
- [X] NPC
- [ ] Behavior bug fix
- [ ] Making Java not to close socket when training stops
- [ ] GAN

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