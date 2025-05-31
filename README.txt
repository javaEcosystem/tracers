1.8.9 Minecraft mod enhanced with OpenGL toolchain and a friend list system B)


[Features]

-> HUD elements that you can toggle as follows:
/fps
/ping
/entities (loaded entities count)
/coords
/biome
/dim (dimension)
/dir (facing direction)
/light or /lyt (light level)

-> Hitbox display
-> Tracers display ( - neon pink rays for rare item drops
                     - yellow rays for other item drops
                     - green rays for friends
                     - white rays for other players )

-> Toggle tracers and hitboxes using either: - keybindings that you can set in minecraft controls settings
                                             - the following commands : /ptracers /itracers /hitbox or /hb

-> Add friends to your list using : /addfriend name or /adf name
-> Remove friends from your list using : /rmfriend name or /rmf name or /removefriend name
-> List your friends using : /lsfriends or /lsf or /listfriends


[Build]

-> Use IntelliJ IDEA 2021.1
-> Use java 8 jdk
-> Set language level to 6 - @Override in interfaces
-> Run ./gradlew build
-> The .jar file will be located at ./build/libs/
