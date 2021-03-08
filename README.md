# Scarpet UHC
Scarpet UHC is a cool little scarpet app that let you play simple and fun UHCs with your friends. This app was **not** designed for big servers but as a simpler alternative to sometimes complex plugins, in an environnement that allows modding and more.

## Installing Scarpet UHC
Running this app will of course require you have carpet installed on your server, which you can download [here](https://github.com/gnembon/fabric-carpet), along with all of its installation instructions.

To install the app just drag it into the `scripts` folder you the world that you want to use. The file should be at `/world/scripts/scarpet_uhc.sc`. If you plan to use the world generator app, add `world_generator.sc` in the same folder.
If you are familiar with the carpet mod, none of this should be new.

# Playing UHC

Upon logging into your world with the app installed, you should be teleported to a hub. If this does not happen, make sure that the app is loaded with `/script load scarpet_uhc` . At this point, the app is waiting for you to start the game, but before doing so you may want to edit the settings.

## Settings
All of the settings presented in this section can be modified 2 ways : 
+ Using the `/scarpet_uhc settings change <category> <setting> <value>` command
+ By changing them directly in `settings.json`, and then updating them in game using `/scarpet_uhc settings reload`

You can see all of the settings at any time using `/scarpet_uhc settings list <category>` or simply reset them using `/scarpet_uhc settings reset` .

## Settings list
Settings are of two types : `bool` or `int`. Note that all settings are saved in the form of numbers, which means that editings a boolean setting through the command will require you tu pu either 0 or 1 (for false or true, nothing new here). There also is a `float` setting among them all, for obvious reasons that you will see later.

+ ### Gamerules
In this category you will find all of the gamerule related settings. These are here mostly to avoid you 10 minutes of typing commands in chat. There still is some special ones in the middle.

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| allow_nether | bool | true  | Allows you to close the nether from the start
| annonce_advancements | bool | true | Just like the vanilla one
| daylight_cycle | bool | false | Just like the vanilla one
| day_time | int | 6000 | Set the time of day if daylight_cycle is false
| fire_tick | bool | false | Just like the vanilla one
| daylight_cycle | bool | false | Just like the vanilla one, keep false to avoid lag
| insomnia | bool | false | Just like the vanilla one, you don't want phantoms in your UHC
| natural_regeneration | bool | false | Just like the vanilla one, here just in case
| patrols | bool | false | Just like the vanilla one (patrolSpawning)
| player_drop_gapple | bool | true | Whether players drops golden apple on death or not
| raids | bool | false | Just like the vanilla one
| wandering_traders | bool | false | Just like the vanilla one
| weather_cycle | bool | false | Just like the vanilla one

+ ### Timers
Do I really have to explain what the category is for ? Note that all timers are in ticks, where 1 second = 20 ticks, do some quick math to obtain the durations you want.

Setting a timer to `0` will make it execute on start, and to `-1` will disable it completely.

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| pvp | int | 12000 | When to enable PvP, 10 minutes by default
| border | int | 108000 | When the border should start moving, 90 minutes by default
| nether_closing | int | -1 | When the nether should close
| final_heal | int | -1 | When the final heal should occure

+ ### Border
Here you have all of the border size and speed settings. Note that sizes are a radius, so a radius of 750 would mean that players are free from 750x, 750z, to -750x, -750z.

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| start | int | 750 | The size of the border at the start of the game
| speed | float | 0.5 | The speed at which the border should move, in blocks/sec
| end | int | 150 | The size at which the border should stop moving

+ ### Teams

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| friendly_fire | bool | true | Disabling this means that you are bad at the game
| show_nametag | bool | true | Sadly you can't nametag your friends 'Mike Oxlong'
| start_radius | int | 500 | The distance from 0 0 teams should have at the start of the game
| start_distance | int | 300 | The distance teams should have between them at the start of the game
| use_distance | bool | false | `start_radius` and `start_distance` are incompatible, so this allows you to choose which to use. This settings should be removed at some point since it is really impractical.

+ ### Other

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| start_invul_time | int | 60 | The length in seconds of the invulnerability given at the start of the game, setting this to 0 is not a good idea since players can spawn on trees
| final_heal_amount | int | 10 | How much players should be healed by the final heal in half hearts (10 = 5 hearts), negative values should work...
| solo_mode | bool | false | **Not implemented**
| ghost_players | bool | true | **Not implemented**
| kill_ghosts_after | int | -1 | **Not implemented**
