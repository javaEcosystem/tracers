1.8.9 Minecraft mod implementing tracers with a client-side friend list system B)


[Features]

-> FPS display
-> Toggle tracers using the keybindings that you can set in minecraft controls settings

-> Rays are colored as follows :
        - neon pink for rare item drops
        - yellow for other item drops (building blocks not included)
        - green for friends
        - white for other players

-> Add friends to your list using : /addfriend name or /adf name
-> Remove friends from your list using : /rmfriend name or /rmf name or /removefriend name
-> List your friends using : /lsfriends or /lsf or /listfriends


[Build]

-> Use IntelliJ IDEA 2021.1
-> Use java 8 jdk
-> Set language level to 6 - @Override in interfaces
-> Run ./gradlew build
-> The .jar file will be located at ./build/libs/
