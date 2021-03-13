# Scarpet UHC
Scarpet UHC is a cool little scarpet app that lets you play simple and fun UHCs with your friends. This app was **not** designed for big servers but as a simpler alternative to sometimes complex plugins, in an environment that allows modding and more.

## Installing Scarpet UHC
Running this app will of course require you have carpet installed on your server, which you can download [here](https://github.com/gnembon/fabric-carpet), along with all of its installation instructions.

To install the app just drag it into the `scripts` folder you the world that you want to use. The file should be at `/world/scripts/scarpet_uhc.sc`. If you plan to use the world generator app, add `world_generator.sc` in the same folder.
If you are familiar with the carpet mod, none of this should be new.

## Technical notes
This app saves itself quite often, and thus will keep settings and game state between restarts. Although it is never recommended to restart your server in the middle of a game, it should be fine. This is also true in case of a server crash (this it when it would be most useful). You should also note that in case of a restart of the app/server, the game will automatically pause.

# Playing UHC

Upon logging into your world with the app installed, you should be teleported to a hub. If this does not happen, make sure that the app is loaded with `/script load scarpet_uhc` . Use `/scarpet_uhc` to see the credits and the app version. At this point, the app is waiting for you to start the game, but before doing so you may want to edit the settings.


## Settings
All the settings presented in this section can be modified 2 ways : 
+ Using the `/scarpet_uhc settings change <category> <setting> <value>` command
+ By changing them directly in `settings.json`, and then updating them in game using `/scarpet_uhc settings reload` . This file will automatically be generated on the app's first start, but you can edit the settings beforehand by having `settings.json` already at `...\scripts\scarpet_uhc.data\settings.json`.

You can see all of the settings at any time using `/scarpet_uhc settings list [<category>]` or simply reset them using `/scarpet_uhc settings reset` .

### Settings list
Settings are of two types : `bool` or `int`. Note that all settings are saved in the form of numbers, which means that editing a boolean setting through the command will require you to put either 0 or 1 (for false or true, nothing new here). There also is a `float` setting among them all, for obvious reasons that you will see later.

+ #### Gamerules
In this category you will find all the gamerule related settings. These are here mostly to avoid you 10 minutes of typing commands in chat. There still are some special ones in the middle.

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| allow_nether | bool | true  | Allows you to close the nether from the start
| annonce_advancements | bool | true | Just like the vanilla one
| daylight_cycle | bool | false | Just like the vanilla one
| day_time | int | 6000 | Set the time of day if daylight_cycle is false
| daylight_cycle | bool | false | Just like the vanilla one
| fire_tick | bool | false | Just like the vanilla one, keep false to avoid lag
| insomnia | bool | false | Just like the vanilla one, you don't want phantoms in your UHC
| natural_regeneration | bool | false | Just like the vanilla one, here just in case
| patrols | bool | false | Just like the vanilla one (patrolSpawning)
| player_drop_gapple | bool | true | Whether players drop golden apple on death or not
| raids | bool | false | Just like the vanilla one
| wandering_traders | bool | false | Just like the vanilla one
| weather_cycle | bool | false | Just like the vanilla one

+ #### Timers
Do I really have to explain what the category is for ? Note that all timers are in ticks, where 1 second = 20 ticks, do some quick math to obtain the durations you want.

Setting a timer to `0` will make it execute on start, and to `-1` will disable it completely.

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| pvp | int | 12000 | When to enable PvP, 10 minutes by default
| border | int | 108000 | When the border should start moving, 90 minutes by default
| nether_closing | int | -1 | When the nether should close
| final_heal | int | -1 | When the final heal should occur

+ #### Border
Here you have all of the border size and speed settings. Note that sizes are a radius, so a radius of 750 would mean that players are free from 750x, 750z, to -750x, -750z.

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| start | int | 750 | The size of the border at the start of the game
| speed | float | 0.5 | The speed at which the border should move, in blocks/sec
| end | int | 150 | The size at which the border should stop moving

+ #### Teams

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| friendly_fire | bool | true | Disabling this means that you are bad at the game
| show_nametag | bool | true | Sadly you can't nametag your friends 'Mike Oxlong'
| start_radius | int | 500 | The distance from [0, 0] teams should have at the start of the game
| start_distance | int | 300 | The distance teams should have between them at the start of the game
| use_distance | bool | false | `start_radius` and `start_distance` are incompatible, so this allows you to choose which to use. This setting should be removed at some point since it is really impractical.

+ #### Other

|   Setting   |  Type  | Default |Description
|:------------|:------:|:-------:|-----------
| start_invul_time | int | 60 | The length in seconds of the invulnerability given at the start of the game, setting this to 0 is not a good idea since players can spawn on trees
| final_heal_amount | int | 10 | How much players should be healed by the final heal in half hearts (10 = 5 hearts), negative values should work...
| enchanted_gapple | bool | false | If players should be allowed to have/eat enchanted golden apples. If set to false, all notch apples will be replaced by regular golden apples.
| suspicious_stew | bool | false | Regeneration using suspicious stew is disabled. Other effects are not disabled.
| solo_mode | bool | false | If true, every player will be on its own. More info in [the team section](https://github.com/l-eg116/Scarpet_UHC/blob/main/README.md#teams-1).
| ghost_players | bool | true | **Not implemented**
| kill_ghosts_after | int | -1 | **Not implemented**


## Teams
In this app all the team’s management is done through the `/scarpet_uhc team` command. There is a total of 16 teams available, and there currently is no way to customize team names. The 16 teams are the [default Minecraft teams](https://minecraft.gamepedia.com/Scoreboard#Teams) color. The app will self-adjust depending on the number of teams. A team is considered as existing as soon as there is a player inside.
### `/scarpet_uhc team join <players> <team>`
With this command you can add players to a team, amazing !
### `/scarpet_uhc team leave <players>`
Make players leave teams with this one. Note that since you can select multiple entities, using `/scarpet_uhc team leave @a` will make everyone leave their team.
### `/scarpet_uhc team empty <team>`
Empties a team, quite self-explaining.
### `/scarpet_uhc team randomize <teamSize> [<force>]`
This command randomizes teams. You can choose the size you want for your teams by setting a `<teamSize>` (int) parameter. By setting `<force>` (boolean, true by default), you can choose if players that are already in a team will be dispatched in new teams (true), or if only players that are spectators will go into random teams (false).
### `/scarpet_uhc team swap <teamA> <teamB>`
If for some reason players are not happy with the color of their team, this command lets you swap the colors of two teams. Team compositions will stay the same, this is just for aesthetic purposes.

### Wool blocks
If you want your players to choose their teams by themselves, you can place wool blocks in the hub. Clicking on a wool block will make you join the team of the same color as the block. Players can leave their team by clicking on a dark/gray/light gray stained glass block.

### Solo mode
If turned on in the settings, teams will be disabled. Every player will be in its own team. Turn on to experience true battle royale. Wool blocks still work : clicking any color will make you a player, and (light) gray/black stained glass will make you spectate. Unlike in team mode, everyone is a player by default.


## Game
Now that you have nicely setup your game, you can start it for good. To do so, simply use `/scarpet_uhc game start` . At this point all players will be teleported with their teams across the map and spectators will be put in spectator mode.

Here are a few commands that you can use while in game :
### `/scarpet_uhc game pause`
This command will pause/unpause the game.
When the game is paused, players are in spectator and cannot move, time stops, and the world border stops moving.

### `/scarpet_uhc game trigger <event>`
Prematurely triggers an event. Events can only be triggered once, full list of events [here](https://github.com/l-eg116/Scarpet_UHC/blob/main/README.md#timers).

### `/scarpet_uhc game top <players>`
Teleports a player to the surface. If the player is in the nether, a portal will be placed at their position. Useful if you have players stuck in the nether or hiding at the end of the game.

### `/scarpet_uhc game heal <players> [<amount>]`
Heals players `amount` half-hearts (20 by default). Note that you can also damage players with this command by healing a negative amount of HP.

### `/scarpet_uhc game revive <players> [<health>] [<surfacelocation>]`
Revives a player that died (the player must be online). You can revive the player with `amount` HP (10 by default). If no `surfacelocation` is specified, the player will respawn at the position of one of his teammates, and if none is alive and/or online, the default position is 0 0. `surfacelocation` does not take a y component, the y level is determined by the terrain.

***
### Bossbar
During gameplay, a bossbar will appear at the top of your screen with all kind of cool info. You will find the day you are on (1 day = 20 minutes), the number of players and teams left, as well as the border size. The time in the current day is shown in the form of the bar filling up, with one notch corresponding to 1 minute.
> I may make it so that this bossbar is customizable later.

### Events
Events will occur at the time that you set. All the events are signalled in chat and with some kind of sound effect.
You will also get a reminder of when they will happen at the start of the game, 10 minutes before the event and 30 seconds before the event.

### Ghosts (**Not implemented**)
When a player disconnects during the game and if the settings `ghost_players` is set to `true`, players will be replaced with a ghost version of themself (aka carpet player shadow). This fake player will still be able to be killed and drop its stuff.

### Game end
The game will end once there is only one team left. Players disconnecting should not end the game.
*Note : if the game was started with only one team, the game will end once there is only one player left.*



# Pregenerating your world
This should probably be on its own on another page, but it is now too late to go back (not really but I'm lazy).

If you want to avoid lag during your game, you can use ~~Alt-F4~~ the **world_generator** app that is given for free with the scarpet_uhc app. In this section I will quickly go over how it works.

## Using the premade command
If you are too lazy to read the following section, you can simply type `/scarpet_uhc generate_world`, and then click on the command provided in chat to pregenerate your world. The command given will generate both overworld and nether and will fill your border.

## Using your own settings
If you want to be the big boy that decides everything on its own and uses commands with the settings that *he* wants, this section is for you.

### `/world_generator start <cornerA> <cornerB> [<inDimension>] [<chunkPerSec>] [<saveFrequency>]`
This command will start a new world generation task. Tasks can be paused and cancelled and will keep running after a server restart. You can follow the app's progress in your server's console or with `/world_generator info`.

| Argument | Default | Description
|----------|:-------:|------------
| `cornerA` & `cornerB` | None | Pairs of x and z coordinates that indicates the area in which the terrain should generate
| `inDimension` | overworld | Dimensions that the app should pregenerate. Can be any usual or custom dimension name. Use `__` or `___` to pregenerate both overworld and nether, or all three default Minecraft dimensions.
| `chunkPerSec` | 20 | The number of chunks to generate per second. Hard limit at 999 chunks per tick (not recommended anyway).
| `saveFrequency` | 1200 | The number of chunks to generate between saves. It is recommended to save every so often to make sure that you don't overload your server.

#### Pro tip
If `chunkPerSec` is set to 20, you can speed up the generation by using the carpet command `/tick warp <ticks>`, where `ticks` is the number of steps returned by `/world_generator start`. This will also work with `chunkPerSec` other than 20 but works best with the default settings.

### `/world_generator pause|cancel`
Cancels or pauses current generation task. Note that cancelled tasks cannot be restarted, and you will have to start over if you want to generate the same area. Paused tasks can be resumed with the same command.
