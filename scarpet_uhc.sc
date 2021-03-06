// Scarpet_UHC v0.1.0-dev by l_eg
global_players = {};
global_settings = {};
global_status = {};

__config()-> {
    'scope' -> 'global',
    'stay_loaded' -> 'true',
    'command_permission' -> 'ops',
    'commands' -> {
        '' -> ['comm_root'],

        'team join <players> <team>' -> ['comm_team_join'],
        'team leave <players>' -> ['comm_team_join', 'spectator'],
        'team empty <team>' -> ['comm_team_empty'],
        'team randomize <teamSize>' -> ['comm_team_randomize', true],
        'team randomize <teamSize> <force>' -> ['comm_team_randomize'],
        'team swap <teamA> <teamB>' -> ['comm_team_swap'],
        'team wool <teams>' -> ['comm_team_wool'],

        'settings change <category> <setting> <settingValue>' -> ['comm_settings_change'],
        'settings reset' -> ['comm_settings_reset'],
        'settings reload' -> ['comm_settings_reload'],
        'settings list' -> ['comm_settings_list', 'all'],
        'settings list <category>' -> ['comm_settings_list'],
        
        'generate_world' -> ['comm_generate_world'],

        'game top <players>' -> ['comm_game_top'],
        'game start' -> ['comm_game_start'],
        'game heal <players>' -> ['comm_game_heal', 20],
        'game heal <players> <amount>' -> ['comm_game_heal'],
        'game revive <players>' -> ['comm_game_revive', 10, null],
        'game revive <players> <health>' -> ['comm_game_revive', null],
        'game revive <players> <health> <surfacelocation>' -> ['comm_game_revive'],
        'game trigger <event>' -> ['comm_game_trigger'],
        'game pause' -> ['comm_game_pause'],
    },
    'arguments' -> {
        'team' -> {'type' -> 'term', 'options' -> ['aqua', 'black', 'blue', 'dark_aqua', 'dark_blue', 'dark_gray', 'dark_green', 
                   'dark_purple', 'dark_red', 'gold', 'gray', 'green', 'light_purple', 'red', 'yellow', 'white',]},
        'teamA' -> {'type' -> 'term', 'options' -> ['aqua', 'black', 'blue', 'dark_aqua', 'dark_blue', 'dark_gray', 'dark_green', 
                   'dark_purple', 'dark_red', 'gold', 'gray', 'green', 'light_purple', 'red', 'yellow', 'white',]},
        'teamB' -> {'type' -> 'term', 'options' -> ['aqua', 'black', 'blue', 'dark_aqua', 'dark_blue', 'dark_gray', 'dark_green', 
                   'dark_purple', 'dark_red', 'gold', 'gray', 'green', 'light_purple', 'red', 'yellow', 'white',]},
        'teams' -> {'type' -> 'text', 'suggest' -> ['aqua', 'blue', 'dark_aqua', 'dark_blue', 'dark_green', 
                   'dark_purple', 'dark_red', 'gold', 'green', 'light_purple', 'red', 'yellow', 'white', 'all']},
        'teamSize' -> {'type' -> 'int', 'min' -> 1, 'suggest' -> [3]},
        'force' -> {'type' -> 'bool'},

        'category' -> {'type' -> 'term', 'suggester' -> _(args) -> (keys(global_settings))},
        'setting' -> {'type' -> 'term', 'suggester' -> _(args) -> (keys(global_settings:(args:'category')))},
        'settingValue' -> {'type' -> 'term', 'suggester' -> _(args) -> [global_settings:(args:'category'):(args:'setting')]},
    
        'amount' -> {'type' -> 'int', 'min' -> -20, 'max' -> 20, 'suggest' -> [10]},
        'health' -> {'type' -> 'int', 'min' -> 1, 'max' -> 20, 'suggest' -> [10]},
        'event' -> {'type' -> 'term', 'suggester' -> _(args) -> (keys(global_settings:'timers'))},
    },
};

__on_start() -> (
    load_status();
    load_settings();
    load_players();
    if(global_status:'game'=='pending', generate_hub());
    if(global_status:'game'=='started', game_pause());
    bossbar('scarpet_uhc:info_bar');
    scoreboard_add('health', 'health');
    scoreboard_display('list', 'health');
);

__on_close() -> (
    save_status();
    save_settings();
    save_players();
);

_reset() -> (
    global_status:'game' = 'pending';
    generate_hub();
    for(player('all'), modify(_, 'location', 0, 203, 0, 0, 0); modify(_, 'gamemode', 'adventure'));
);

// # Loading and saving
load_status() -> (
    default_status = {
        'game' -> 'pending',
        'time' -> 0,
        'border' -> 'static',
        'border_size' -> 750,
        'total_teams' -> 0,
        'total_players' -> 0,
        'pvp' -> false,
        'nether' -> true,
    };

    loaded_status = load_app_data();
    if(!loaded_status, 
        global_status = default_status;
        logger('info', '[Scarpet UHC] No app data found, loading defaults...');
        ,
        global_status = default_status + parse_nbt(loaded_status);
        logger('info', '[Scarpet UHC] Found pre-existing app data, loading...');
    );

    logger('debug', 'Current status : ' + global_status);
);

save_status() -> (
    logger('info', '[Scarpet UHC] Saving current status...');
    store_app_data(nbt(global_status));
);

load_settings() -> (
    default_settings = {
        'border' -> {
            'start' -> 750,
            'end' -> 150,
            'speed' -> 0.5,
        },
        'timers' -> {
            'border' -> 20*60*90, // aka 90 minutes
            'nether_closing' -> -1,
            'final_heal' -> -1,
            'pvp' -> 20*60*10, // aka 10 minutes
        },
        'gamerules' -> {
            'daylight_cycle' -> false,
            'day_time' -> 6000,
            'weather_cycle' -> false,
            'allow_nether' -> true,
            'player_drop_gapple' -> true,
            'natural_regeneration' -> false,
            'insomnia' -> false,
            'wandering_traders' -> false,
            'patrols' -> false,
            'raids' -> false,
            'announce_advancements' -> true,
            'fire_tick' -> true,
        },
        'other' -> {
            'solo_mode' -> false,
            'ghost_players' -> true,
            'kill_ghosts_after' -> -1,
            // 'death_by_creeper' -> true, // Does not work due to a bug in scarpet
            'final_heal_amount' -> 10,
            'start_invul_time' -> 60,
            // 'cut_clean' -> false,
            'enchanted_gapple' -> false,
            'suspicious_stew' -> false,
            'teammate_direction' -> false,
        },
        'teams' -> {
            'friendly_fire' -> true,
            'show_nametag' -> true,
            'start_radius' -> 500,
            'start_distance' -> 0,
            'use_distance' -> 0,
        },
    };

    loaded_settings = read_file('settings', 'json');
    if(!loaded_settings, 
        global_settings = default_settings;
        logger('info', '[Scarpet UHC] No settings found, loading defaults...');
        ,
        for(default_settings, global_settings:_ = default_settings:_ + loaded_settings:_);
        logger('info', '[Scarpet UHC] Found settings.json, loading...');
    );

    logger('debug', 'Current settings : ' + global_settings);
);

save_settings() -> (
    logger('info', '[Scarpet UHC] Saving current settings...');
    write_file('settings', 'json', global_settings);
);

load_players() -> (
    loaded_players = read_file('players', 'json');
    if(loaded_players, 
        global_players = loaded_players;
        logger('info', '[Scarpet UHC] Found players.json, loading...');
    );

    logger('debug', 'Current settings : ' + global_settings);
);

save_players() -> (
    logger('info', '[Scarpet UHC] Saving players...');
    write_file('players', 'json', global_players);
);

// # Commands
comm_root() -> (
    print(format('d Scarpet UHC ', ' by ', 'e l_eg'));
    print(format(' Collaborator : ', 'bn BlackH57'));
    print(format(' Version ', 'm 0.1.0-dev'));
    print(format('  -> ', 'ut github.com/l-eg116/Scarpet_UHC/', '?https://github.com/l-eg116/Scarpet_UHC/'));
);

comm_team_join(players, team) -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    if(global_settings:'other':'solo_mode',
        print(format('r You are in solo mode'));
        return();
    );

    count = for(players,
        team_join(player(_), team);
    );
    print(str('Added %d player%s to team %s.', count, if(count == 1, '', 's'), team));
);

comm_team_empty(team) -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    if(global_settings:'other':'solo_mode',
        print(format('r You are in solo mode'));
        return();
    );

    count = for(keys(global_players),
        if(global_players:_:'team' == team,
            global_players:_:'team' = 'spectator';
        );
    );
    update_teams();

    print(str('Removed %d player%s from team %s.', count, if(count == 1, '', 's'), team));
);

comm_team_randomize(team_size, force) -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    if(global_settings:'other':'solo_mode',
        print(format('r You are in solo mode'));
        return();
    );
    
    if(force, 
        for(keys(global_players),
            global_players:_:'team' = 'spectator'
        );
    );

    team_list = [
        'blue', 'red', 'green', 'yellow', 'aqua', 'light_purple', 'gold', 'dark_aqua', 'dark_blue', 'dark_green','dark_purple', 'dark_red', 'black', 'gray', 'dark_gray', 'white',
    ];
    randomized_players = filter(sort_key(player('all'), rand(1)), global_players:(_~'uuid'):'team' == 'spectator');
    if(!randomized_players, return());
    i = 0;

    for(team_list,
        team = _;
        while(team_size(team) < team_size, team_size,
            team_join(randomized_players:i, team);
            i += 1;

            if(i >= length(randomized_players), break());
        );

        if(i >= length(randomized_players), break());
    );

    print('Teams randomized');
);

comm_team_swap(team1, team2) -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    if(global_settings:'other':'solo_mode',
        print(format('r You are in solo mode'));
        return();
    );
    count = for(keys(global_players),
        if(global_players:_:'team' == team1, global_players:_:'team' = team2,
            global_players:_:'team' == team2, global_players:_:'team' = team1,
        );
    );
    update_teams();

    print(str('Swaped %d player%s from team %s to team %s', count, if(count == 1, '', 's'), team1, team2));
);

comm_team_wool(teams) -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );

    team_to_color = {
        'aqua' -> 'light_blue',
        'black' -> 'black',
        'blue' -> 'blue',
        'dark_aqua' -> 'cyan',
        'dark_blue' -> 'purple',
        'dark_gray' -> 'gray',
        'dark_green' -> 'green',
        'dark_purple' -> 'magenta',
        'dark_red' -> 'brown',
        'gold' -> 'orange',
        'gray' -> 'light_gray',
        'green' -> 'lime',
        'light_purple' -> 'pink',
        'red' -> 'red',
        'yellow' -> 'yellow',
        'white' -> 'white',
    };

    if(teams == 'all', teams = join(' ', keys(team_to_color)));

    for(split(' ', teams), color = team_to_color:_;
        if(color == null, continue());

        volume([-10, 200, -10], [9, 200, 9], block = block(_x, _y, _z);
            if(
                str(block) == color + '_stained_glass', set(block, color + '_wool'),
                str(block) == color + '_wool', set(block, color + '_stained_glass'),
            );
        );
    );
    
    print('Floor updated');
);

comm_settings_reset() -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    
    global_settings = {};
    save_settings();
    load_settings();
    save_settings();
    print('All settings were reset and saved');
);

comm_settings_reload() -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    
    load_settings();
    print('All settings were reloaded from settings.json');
);

comm_settings_list(categories) -> (
    categories = if(global_settings~categories, [categories], keys(global_settings));

    print('Current settings : ');
    for(sort(categories), category = _;
        print(str(' ¤ %s :', title(category)));
        for(sort(keys(global_settings:category)), 
            print(str('   - %s : %d', replace(title(_), '_', ' '), global_settings:category:_));
        );
    );
);

comm_settings_change(category, setting, value) -> (
    if(global_status:'game' != 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    
    if(
        value == 'true', value = true,
        value == 'false', value = false,
        value = number(value)
    );
    if(global_settings~category && global_settings:category~setting && type(value) == type(global_settings:category:setting),
        global_settings:category:setting = value;
        save_settings();
        print(str('Changed setting %s in "%s" to %s', setting, category, value));
        if(category == 'other' && setting == 'solo_mode',
            if(value,
                for(player('all'), team_join(_, str(_))), 
                for(player('all'), team_join(_, 'spectator'))
            );
        );
        ,
        print(format('r The setting that you tried to change does not exist, or you tried to put in a wrong value'));
    );
);

comm_generate_world() -> (
    border = global_settings:'border':'start';
    command = str('/world_generator start %d %d %d %d __ 20 1200', -border, -border, border, border);
    print(format(' Use world_generator app to pregenerate your world. Here is a cool premade command : ', 'i ' + command, '^ Click to execute !', '?' + command));
);

comm_game_start() -> (
    if(global_status:'game' == 'pending',
        game_start();
        ,
        print(format('r Game already started'));
    )
);

comm_game_top(players) -> (
    if(global_status:'game' == 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    
    for(players, 
        player = player(_);

        if(player~'dimension' != 'overworld',
            in_dimension(player~'dimension', set(player~'pos', 'nether_portal'));
            modify(player, 'portal_timer', 99999);
            // schedule(1, 'comm_game_top', player);
            ,
            x = floor(player~'x') + 0.5;
            z = floor(player~'z') + 0.5;
            y = in_dimension('overworld', top('motion', [x, 0, z]) + 1);
            modify(player, 'pos', [x, y, z]);
        );
        modify(player, 'effect', 'resistance', 20*10, 255, false, true);
    );
);

comm_game_heal(players, amount) -> (
    if(global_status:'game' == 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    
    count = for(players, 
        modify(player(_), 'health', player(_)~'health' + amount)
    );
    print(str('%d player%s healed', count, if(count == 1, ' was', 's were')));
);

comm_game_revive(players, health, location) -> (
    if(global_status:'game' == 'pending',
        print(format('r You cannot use this command now'));
        return();
    );
    
    count = for(players, 
        if(!global_players:(player(_)~'uuid'):'alive' && global_players:(player(_)~'uuid'):'online' && !global_players:(player(_)~'uuid'):'team' != 'spectator',
            revive(_, health, location);
            print(player('all'), str('%s has been revived !', _));
            sound('minecraft:entity.zombie_villager.converted', [0, 0, 0], 99999, 1, 'player');
        );
    );
    print(str('%d player%s revived', count, if(count == 1, ' was', 's were')));
);

comm_game_trigger(event) -> (
    if(global_status:'game' != 'started',
        print(format('r You cannot use this command now'));
        return();
    );

    for(keys(global_settings:'timers'),
        if(_ == event,
            if(global_settings:'timers':_ < global_status:'time' && global_settings:'timers':_ != -1,
                print(format('r This event already happened'));
                return();
            ,
                global_settings:'timers':_ = global_status:'time';
                print('Event triggered');
                return();
            );
        );
    );

    print(format('r This event does not exist'));
);

comm_game_pause() -> (
    if(global_status:'game' == 'started',
        game_pause();
    , global_status:'game' == 'paused',
        game_unpause();
    ,
        print(format('r You cannot use this command now'));
        return();
    );
);

// # Hub generation
shiny_floor()->(
    blocks = ['orange','magenta','light_blue','yellow','lime','pink','cyan','purple','blue','brown','green','red','white'] + '_stained_glass';

    return(blocks:floor(rand(length(blocks))))
);

generate_hub() -> (
    volume([-11, 200, -11], [10, 205, 10],
        edge_count = 0;
        if(_x == -11 || _x == 10, edge_count += 1);
        if(_y == 200 || _y == 205, edge_count += 1);
        if(_z == -11 || _z == 10, edge_count += 1);

        if(
            edge_count == 0, set(_x, _y, _z, 'air'),
            edge_count == 1 && _y == 200, set(_x, _y, _z, shiny_floor()),
            edge_count == 1 && _y != 200, set(_x, _y, _z, 'barrier'),
            edge_count == 2, set(_x, _y, _z, 'glass'),
            edge_count == 3, set(_x, _y, _z, 'bedrock'),
        );
    );

    run('setworldspawn 0 200 0'); // TODO : Maybe add a settings for that ?
);

// # Personal teams management
update_teams(...players) -> (
    if(!players, players = keys(global_players));

    for(players,
        if(!entity_id(_), continue());

        team_name = slice(global_players:_:'team', 0, 3) + '_' + slice(_, 0, 8);
        uuid = _;

        filter(team_list(), _~('_' + slice(uuid, 0, 8)) && team_remove(_));
        team_add(team_name);
        team_leave(team_name);
        team_add(team_name, entity_id(_));

        if(global_settings:'teams':'show_nametag',
            team_property(team_name, 'nametagVisibility', 'always');
        ,
            team_property(team_name, 'nametagVisibility', 'never');
        );

        if(global_players:_:'team' == 'spectator',
            team_property(team_name, 'color', 'gray');
            team_property(team_name, 'prefix', '<O> ');
            team_property(team_name, 'suffix', '');
            ,
            if(global_players:_:'alive',
                if(global_players:_:'online',
                    team_property(team_name, 'color', 'white');
                    team_property(team_name, 'prefix', add_format_color(' 🗡 ', global_players:_:'team'));
                    team_property(team_name, 'suffix', '');
                    ,
                    team_property(team_name, 'color', 'white');
                    team_property(team_name, 'prefix', add_format_color('o 🗡 ', global_players:_:'team'));
                    team_property(team_name, 'suffix', '');
                );
                ,
                team_property(team_name, 'color', 'gray');
                team_property(team_name, 'prefix', add_format_color(' 🗡 ', global_players:_:'team'));
                team_property(team_name, 'suffix', format('n  ☠'));
            );
        );
    );
);

add_format_color(text, color) -> (
    if(
        color == 'aqua', return(format('c'+text)),
        color == 'black', return(format('k'+text)),
        color == 'blue', return(format('t'+text)),
        color == 'dark_aqua', return(format('q'+text)),
        color == 'dark_blue', return(format('v'+text)),
        color == 'dark_gray', return(format('f'+text)),
        color == 'dark_green', return(format('e'+text)),
        color == 'dark_purple', return(format('p'+text)),
        color == 'dark_red', return(format('n'+text)),
        color == 'gold', return(format('d'+text)),
        color == 'gray', return(format('g'+text)),
        color == 'green', return(format('l'+text)),
        color == 'light_purple', return(format('m'+text)),
        color == 'red', return(format('r'+text)),
        color == 'yellow', return(format('y'+text)),
        color == 'white', return(format('w'+text)),
        return(format(text))
    );
);

team_join(player, team) -> (
    global_players:(player~'uuid'):'team' = team;
    update_teams(player~'uuid');
);

team_size(team) -> (
    for(keys(global_players),
        global_players:_:'team' == team
    );
);

// # Counting and listing
team_listing(online, alive) -> (
    team_list = {};
    for(keys(global_players),
        if((global_players:_:'online' || !online) && (global_players:_:'alive' || !alive),
            team_list += global_players:_:'team'
        )
    );
    delete(team_list:'spectator');

    return(keys(team_list));
);

team_count(online, alive) -> (
    return(length(team_listing(online, alive)));
);

player_listing(team, online, alive) -> (
    filter(keys(global_players),
        (global_players:_:'team' != 'spectator' && 
         if(team != '', global_players:_:'team' == team, true) && 
         (global_players:_:'alive' || !alive) && 
        (global_players:_:'online' || !online))
    );
);

player_count(team, online, alive) -> (
    return(length(player_listing(team, online, alive)));
);

player_distance(player1, player2) -> (
    return(sqrt((player1~'x' - player2~'x')^2 + (player1~'y' - player2~'y')^2 + (player1~'z' - player2~'z')^2));
);

direction(viewer, target) -> (
	viewer_angle = round(viewer~'yaw');
	if(viewer_angle < 0, viewer_angle += 360);
	relative_x = target~'x' - viewer~'x';
	relative_z = target~'z' - viewer~'z';
	relative_angle = atan(relative_x/relative_z);
	if(relative_x >= 0 && relative_z >= 0, relative_angle = 360 - relative_angle);
	if(relative_x < 0 && relative_z >= 0, relative_angle = 0 - relative_angle);
	if(relative_x < 0 && relative_z < 0, relative_angle = 180 - relative_angle);
	if(relative_x >= 0 && relative_z < 0, relative_angle = 180 - relative_angle);
	relative_angle += -viewer_angle;
	if(relative_angle < 0, relative_angle += 360);
	return(relative_angle)
);

direction_arrow(viewer, target) -> (
	direction = direction(viewer, target);
	if(337.5 <= direction || direction < 22.5, return('↑'));
	if(22.5 <= direction && direction < 67.5, return('⬈'));
	if(67.5 <= direction && direction < 112.5, return('→'));
	if(112.5 <= direction && direction < 157.5, return('⬊'));
	if(157.5 <= direction && direction < 202.5, return('↓'));
	if(202.5 <= direction && direction < 247.5, return('⬋'));
	if(247.5 <= direction && direction < 292.5, return('←'));
	if(292.5 <= direction && direction < 337.5, return('⬉'));
);

show_directions(player) -> (
    message = format('kb  | ');

    for(player_listing(global_players:(player~'uuid'):'team', true, true), teammate = entity_id(_);
        if(player == teammate, continue());

        if(player~'dimension' == teammate~'dimension',
            message += format('w '+teammate);
            if(
                (dist = player_distance(player, teammate)) < 8, message += format('c  '+direction_arrow(player, teammate)),
                dist < 24, message += format('e  '+direction_arrow(player, teammate)),
                dist < 40, message += format('y  '+direction_arrow(player, teammate)),
                dist < 56, message += format('d  '+direction_arrow(player, teammate)),
                message += format('r  '+direction_arrow(player, teammate)),
            );
            message += format('kb  | ');
        );
    );

    display_title(player, 'actionbar', message);
);

// # Game start functions
game_start() -> (
    global_status:'game' = 'starting';

    volume([-11, 200, -11], [10, 205, 10], set(_x, _y, _z, block('air')));

    for(global_players, if(!global_players:_:'online', global_players:_:'team' = 'spectator'));

    for(player('all'), 
        global_players:(_~'uuid'):'alive' = true;

        if(global_players:(_~'uuid'):'team' == 'spectator',
            modify(_, 'gamemode', 'spectator');
            modify(_, 'location', 0, 203, 0, 0, 0);
            ,
            reset_player(_);
            modify(_, 'gamemode', 'survival');
            modify(_, 'effect', 'resistance', global_settings:'other':'start_invul_time'*20, 255, false, true);
        );
    );
    update_teams();

    update_gamerules();
    if(global_settings:'gamerules':'daylight_cycle', day_time(0), day_time(global_settings:'gamerules':'day_time'));

    if(global_settings:'teams':'use_distance', 
        global_settings:'teams':'start_radius' = spread_radius_from_distance()
    );
    spread_teams(spread_coords());

    logger('info', '[Scarpet UHC] Game started');
    global_status = {
        'game' -> 'started',
        'time' -> 0,
        'border' -> 'static',
        'border_size' -> global_settings:'border':'start',
        'total_teams' -> team_count(1, 1),
        'total_players' -> player_count('', true, true),
        'pvp' -> false,
        'nether' -> true,
    };
);

reset_player(player) -> (
    modify(player, 'effect', );
    modify(player, 'invulnerable', false);
    modify(player, 'fire', false);
    modify(player, 'health', 20);
    modify(player, 'hunger', 20);
    modify(player, 'saturation', 5);
    modify(player, 'exhaustion', 0);
    modify(player, 'absorption', 0);
    modify(player, 'xp_level', 0);
    modify(player, 'xp_progress', 0);
    modify(player, 'xp_score', 0);
    run(str('clear %s', player));
    run(str('recipe take %s *', player));
    run(str('advancement revoke %s everything', player));
);

revive(player, health, location) -> (
    player = player(player);
    uuid = player~'uuid';
    
    if(
    length(location) <= 1 && player_listing(global_players:uuid:'team', true, true),
        location = entity_id(player_listing(global_players:uuid:'team', true, true):floor(rand(99)))~'location',
    length(location) == 2,
        location = [floor(location:0) + 0.5, top('motion', [location:0, 0, location:1]), floor(location:1) + 0.5, 0, 0],
    ,
        location = [0.5, top('motion', [0, 0, 0]), 0.5, 0, 0];
    );

    modify(player, 'health', health);
    modify(player, 'location', location);
    modify(player, 'gamemode', 'survival');
    modify(player, 'effect', 'resistance', 20*10, 255, false, true);
    global_players:uuid:'alive' = true;
    update_teams(uuid);
);

update_gamerules() -> (
    run(str('gamerule doDaylightCycle %b', global_settings:'gamerules':'daylight_cycle'));
    run(str('gamerule doWeatherCycle %b', global_settings:'gamerules':'weather_cycle'));
    run(str('gamerule naturalRegeneration %b', global_settings:'gamerules':'natural_regeneration'));
    run(str('gamerule doInsomnia %b', global_settings:'gamerules':'insomnia'));
    run(str('gamerule doTraderSpawning %b', global_settings:'gamerules':'wandering_traders'));
    run(str('gamerule doPatrolSpawning %b', global_settings:'gamerules':'patrols'));
    run(str('gamerule disableRaids %b', !global_settings:'gamerules':'raids'));
    run(str('gamerule announceAdvancements %b', global_settings:'gamerules':'announce_advancements'));
    run(str('gamerule doFireTick %b', global_settings:'gamerules':'fire_tick'));
    null;
);

game_end() -> (
    logger('info', '[Scarpet UHC] Game ended');
    global_status:'game' = 'ended';

    winner = if(has(team_listing(false, true):0), team_listing(false, true):0, null);

    for(player('all'),
        if(global_players:(_~'uuid'):'team' == winner,
            display_title(_, 'title', format('d You won !'), 10, 180, 10)
        ,
            display_title(_, 'title', format('n You lost !'), 10, 180, 10)
        );
    );
    if(global_settings:'other':'solo_mode',
        display_title(player('all'), 'subtitle', format(' '+winner+' won !'), 10, 180, 10);
    ,
        display_title(player('all'), 'subtitle', format(' Team ')+add_format_color(replace(' '+winner, '_', ' '), winner)+format('  won !'), 10, 180, 10);
    );
    sound('minecraft:ui.toast.challenge_complete', [0, 0, 0], 99999, 1, 'master');
);

game_pause() -> (
    logger('info', '[Scarpet UHC] Game paused');
    global_status:'game' = 'paused';

    for(player_listing('', 1, 1),
        modify(entity_id(_), 'gamemode', 'spectator');
        schedule(1, _(player, y) -> modify(player, 'y', y), entity_id(_), entity_id(_)~'y');
    );
    run('gamerule doDaylightCycle false');

    display_title(player('all'), 'title', format('f Game paused'));
    sound('minecraft:block.beacon.power_select', [0, 0, 0], 99999, 1, 'master');
);

game_unpause() -> (
    logger('info', '[Scarpet UHC] Game unpaused');
    global_status:'game' = 'started';

    for(player_listing('', 1, 1),
        modify(entity_id(_), 'gamemode', 'survival');
        modify(entity_id(_), 'effect', 'resistance', 20*5, 255, false, true);
    );
    update_gamerules();

    display_title(player('all'), 'title', format('d Game unpaused'));
    sound('minecraft:block.beacon.activate', [0, 0, 0], 99999, 1, 'master');
);

// ## Team spreading
spread_radius_from_distance() -> (
    distance = global_settings:'teams':'start_distance';
    angle = deg((2*pi) / team_count(1, 1)) ;
    spread_radius = distance / (sqrt(2*(1 - cos(angle))));
    return(round(spread_radius));
);

spread_coords() -> (
    angle = deg((2 * pi) / team_count(1, 1));
    random_factor = rand(360);
    coord = [];

    loop(team_count(1, 1),
        x = cos(_ * angle + random_factor) * global_settings:'teams':'start_radius';
        z = sin(_ * angle + random_factor) * global_settings:'teams':'start_radius';
        y = top('motion', [round(x), 0, round(z)]) + 1;
        coord += [round(x), y, round(z)];
    );

    return(coord);
);

spread_teams(coords) -> (
    coords = sort_key(coords, rand(1));

    if(!coords,
        print(format('r Tried to spread teams but no set of coords were given. Are you sure there are teams to spread ?'));
        logger('warn', 'Tried to spread teams but no set of coords were given. Are you sure there are teams to spread ?');
        return();
    );

    for(team_listing(1, 1), i = _i;
        for(player_listing(_, true, true),
            modify(entity_id(_), 'location', 0, 203, 0, 0, 0);
            modify(entity_id(_), 'pos', coords:i + [0.5, 0.5, 0.5]);
        );
    );
);

// # Bossbar
update_bossbar() -> (
    bossbar('scarpet_uhc:info_bar', 'color', 'red');
    bossbar('scarpet_uhc:info_bar', 'visible', true);

    if(global_settings:'other':'solo_mode',
        if(
            global_status:'game' == 'pending',
                bossbar('scarpet_uhc:info_bar', 'style', 'progress');
                bossbar('scarpet_uhc:info_bar', 'value', 0);
                bossbar('scarpet_uhc:info_bar', 'max', 100);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format('d Scarpet UHC', '  - ', 'br '+player_count('', 1, 0), 'gi /', 'gi '+length(player('all')), 'n  players', )
                );
            , global_status:'game' == 'started',
                bossbar('scarpet_uhc:info_bar', 'style', 'notched_20');
                bossbar('scarpet_uhc:info_bar', 'value', global_status:'time'%24000);
                bossbar('scarpet_uhc:info_bar', 'max', 24000);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format(
                    ' Day ', str('e %d', global_status:'time'/24000 + 1), '  - ',
                    'bd '+player_count('', 0, 1), 'r /'+global_status:'total_players', '  alive - ',
                    ' Border : ', 'c '+round(global_status:'border_size'),
                    )
                );
            , global_status:'game' == 'paused',
                bossbar('scarpet_uhc:info_bar', 'style', 'notched_20');
                bossbar('scarpet_uhc:info_bar', 'value', global_status:'time'%24000);
                bossbar('scarpet_uhc:info_bar', 'max', 24000);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format(
                    ' Day ', str('e %d', global_status:'time'/24000 + 1), '  - ',
                    'bd '+player_count('', 1, 1), 'r /'+player_count('', 0, 1), '  online - ',
                    'f Game paused',
                    )
                );
            , global_status:'game' == 'ended',
                bossbar('scarpet_uhc:info_bar', 'style', 'progress');
                bossbar('scarpet_uhc:info_bar', 'value', 100);
                bossbar('scarpet_uhc:info_bar', 'max', 100);
                winner = if(has(team_listing(false, true):0), team_listing(false, true):0, null);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format(' '+winner+' won !')
                );
        );
    ,
        if(
            global_status:'game' == 'pending',
                bossbar('scarpet_uhc:info_bar', 'style', 'progress');
                bossbar('scarpet_uhc:info_bar', 'value', 0);
                bossbar('scarpet_uhc:info_bar', 'max', 100);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format('d Scarpet UHC', '  - ', 'br '+player_count('', 1, 0), 'gi /', 'gi '+length(player('all')), 'n  players',
                    ' , ', 'mb '+team_count(1, 0), 'p  teams')
                );
            , global_status:'game' == 'started',
                bossbar('scarpet_uhc:info_bar', 'style', 'notched_20');
                bossbar('scarpet_uhc:info_bar', 'value', global_status:'time'%24000);
                bossbar('scarpet_uhc:info_bar', 'max', 24000);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format(
                    ' Day ', str('e %d', global_status:'time'/24000 + 1), '  - ',
                    'bd '+player_count('', 0, 1), 'r /'+global_status:'total_players', '  alive - ',
                    'mb '+team_count(0, 1), 'p /'+global_status:'total_teams', '  teams - ',
                    ' Border : ', 'c '+round(global_status:'border_size'),
                    )
                );
            , global_status:'game' == 'paused',
                bossbar('scarpet_uhc:info_bar', 'style', 'notched_20');
                bossbar('scarpet_uhc:info_bar', 'value', global_status:'time'%24000);
                bossbar('scarpet_uhc:info_bar', 'max', 24000);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format(
                    ' Day ', str('e %d', global_status:'time'/24000 + 1), '  - ',
                    'bd '+player_count('', 1, 1), 'r /'+player_count('', 0, 1), '  online - ',
                    'f Game paused',
                    )
                );
            , global_status:'game' == 'ended',
                bossbar('scarpet_uhc:info_bar', 'style', 'progress');
                bossbar('scarpet_uhc:info_bar', 'value', 100);
                bossbar('scarpet_uhc:info_bar', 'max', 100);
                winner = if(has(team_listing(false, true):0), team_listing(false, true):0, null);
                bossbar('scarpet_uhc:info_bar', 'name', 
                    format(' Team ')+add_format_color(replace(' '+winner, '_', ' '), winner)+format('  won !')
                );
        );
    );
);

update_border() -> (
    if( global_status:'border' == 'moving' && global_status:'border_size' - global_settings:'border':'speed' <= global_settings:'border':'end', 
            global_status:'border' = 'static';
            global_status:'border_size' = global_settings:'border':'end';

            run(str('worldborder set %f 1', 2*global_settings:'border':'end'));
            event_border_end();
        ,
        global_status:'border' == 'moving',
            global_status:'border_size' += -global_settings:'border':'speed';
            run(str('worldborder add %f 1', -2*global_settings:'border':'speed'));
        ,
        global_status:'border' == 'static',
            run(str('worldborder set %f', 2*global_status:'border_size'));
    );
);

// # UHC events
event_new_day() -> (
    display_title(player('all'), 'title', format(' Day ', str('e %d', global_status:'time'/24000 + 1)), 10, 75, 5);
    for(player('all'),
        display_title(_, 'subtitle', format(' You made ', 'r '+statistic(_, 'killed', 'player'), '  kill'+if(statistic(_, 'killed', 'player') == 1, '', 's')));
    );

    sound('minecraft:block.bell.use', [0, 0, 0], 99999, 1, 'master');
);

event_start() -> (
    print(player('all'), format('db Game started !'));
    print(player('all'), format('  - PvP : ', str('r %d min', global_settings:'timers':'pvp'/1200)));
    print(player('all'), format('  - Border : ', str('p %d min', global_settings:'timers':'border'/1200)));
    if(global_settings:'timers':'nether_closing'>0, 
        print(player('all'), format('  - Nether closing : ', str('e %d min', global_settings:'timers':'nether_closing'/1200))));
    if(global_settings:'timers':'final_heal'>0, 
        print(player('all'), format('  - Final heal : ', str('d %d min', global_settings:'timers':'final_heal'/1200))));
    
    print(player('all'), 'Good luck !');
);

event_pvp() -> (
    global_status:'pvp' = true;

    print(player('all'), format('n Pvp', '  is now enabled !'));
    sound('minecraft:entity.arrow.hit_player', [0, 0, 0], 99999, 1, 'master');
);

event_nether_closing() -> (
    global_status:'nether' = false;

    print(player('all'), format(' The nether is now', 'n  closed'));
    sound('minecraft:entity.ghast.hurt', [0, 0, 0], 99999, 1, 'master');
);

event_border_start() -> (
    global_status:'border' = 'moving';

    print(player('all'), format(' The border has started moving !'));
    sound('minecraft:entity.zombie.attack_iron_door', [0, 0, 0], 99999, 1, 'master');
);

event_border_end() -> (
    print(player('all'), format(' The border has reached its destination'));
);

event_final_heal() -> (
    print(player('all'), format(' Final heal, you were healed ', 'r '+global_settings:'other':'final_heal_amount', '  HP'));
    for(player('all'), modify(_, 'health', _~'health' + global_settings:'other':'final_heal_amount'));
    
    sound('minecraft:entity.zombie_villager.converted', [0, 0, 0], 99999, 1, 'master');
);

// # Tick
__tick_pending() -> (
    for(player('all'), 
        modify(_, 'health', 20);
        modify(_, 'hunger', 20);
    );
);

__tick_started() -> (
    if(global_status:'time' == 0, event_start());
    if(global_status:'time'%24000 == 0, event_new_day());
    if(global_status:'time' == global_settings:'timers':'pvp', event_pvp());
    if(global_status:'time' == global_settings:'timers':'nether_closing', event_nether_closing());
    if(global_status:'time' == global_settings:'timers':'border', event_border_start());
    if(global_status:'time' == global_settings:'timers':'final_heal', event_final_heal());
    
    for(global_settings:'timers',
        if(global_settings:'timers':_ == global_status:'time' + 20*60*10,
            print(player('all'), format('d '+replace(title(_), '_', ' '), '  in 10 mn'));
            sound('minecraft:block.note_block.pling', [0, 0, 0], 99999, 1, 'master');
        ,
        global_settings:'timers':_ == global_status:'time' + 20*30,
            print(player('all'), format('d '+replace(title(_), '_', ' '), '  in 30 sec'));
            sound('minecraft:block.note_block.bell', [0, 0, 0], 99999, 1, 'master');
        );
    );

    if(global_status:'time'%20 == 0, update_border());
    if(global_status:'time' %24000 == 0 && global_settings:'gamerules':'daylight_cycle', day_time(0));

    if(global_settings:'other':'teammate_direction',
        for(player_listing('', true, true),
            if(entity_id(_)~'sneaking', show_directions(entity_id(_)));
        );    
    );

    if(
    global_status:'total_teams' == 1 && global_status:'total_players' == 1 && player_count('', 0, 1) == 0,
        game_end(),
    global_status:'total_teams' == 1 && global_status:'total_players' > 1 && player_count('', 0, 1) <= 1,
        game_end(),
    global_status:'total_teams' > 1 && global_status:'total_players' > 1 && team_count(0, 1) <= 1,
        game_end(),
    );

    global_status:'time' += 1;
);

__tick_paused() -> (
    for(player('all'), 
        if(_~'gamemode' == 'spectator', 
            in_dimension(_~'dimension', 
                schedule(0, _(player, pos) -> modify(player, 'pos', pos), _, _~'pos')
            );
        );
        player = _;
        for(_~'effect',
            modify(player, 'effect', _:0, _:2 + 1, _:1);
        );
    );
);

// # Events
__on_tick() -> (
    update_bossbar();

    if(
        global_status:'game' == 'pending', __tick_pending(),
        global_status:'game' == 'started', __tick_started(),
        global_status:'game' == 'paused', __tick_paused(),
    );

    if(!global_settings:'gamerules':'weather_cycle', weather('clear', 20*60*10));
    if(!global_status:'nether',
        for(player('all'), 
            modify(_, 'portal_timer', 0);
            if(_~'dimension' != 'overworld', comm_game_top(_));
        );
    );
);

__on_player_connects(player) -> (
    default_player_state = {
        'team' -> 'spectator',
        'alive' -> true,
        'online' -> true,
    };

    if(!global_players~(player~'uuid'),
        global_players:(player~'uuid') = default_player_state; 
    );

    save_players();
    
    if(global_status:'game' == 'pending',
        modify(player, 'location', 0, 203, 0, 0, 0);
        modify(player, 'gamemode', 'adventure');
        modify(player, 'health', 20);
        modify(player, 'hunger', 20);
        modify(player, 'saturation', 20);
        modify(player, 'invulnerable', true);
        global_players:(player~'uuid'):'alive' = true;
        if(global_settings:'other':'solo_mode',
            team_join(player, str(player))
            ,
            if(global_players:(player~'uuid'):'team' == str(player),
                team_join(player, 'spectator')
            );
        );
    , global_status:'game' == 'started',
        if(global_players:(player~'uuid'):'alive', modify(player, 'gamemode', 'survival'));
    , global_status:'game' == 'paused',
        if(global_players:(player~'uuid'):'alive', modify(player, 'gamemode', 'spectator'));
    );

    global_players:(player~'uuid'):'online' = true;
    update_teams(player~'uuid');

    bossbar('scarpet_uhc:info_bar', 'add_player', player);
);

__on_player_disconnects(player, reason) -> (
    if(global_status:'game' == 'started' && global_settings:'other':'ghost_players' 
    && global_players:(player~'uuid'):'alive' && global_players:(player~'uuid'):'team' != 'spectator'
    && player~'player_type' != 'fake' && player~'player_type' != 'shadow', 
        run(str('player %s shadow', player));
    );

    global_players:(player~'uuid'):'online' = false;
    update_teams(player~'uuid');

    save_players();
);

__on_player_attacks_entity(player, entity) -> (
    if(entity~'type' == 'player' && !global_status:'pvp',
        modify(player, 'effect', 'weakness', 1, 255, false, false);
    ,
    entity~'type' == 'player' && !global_settings:'teams':'friendly_fire'
        && global_players:(player~'uuid'):'team' == global_players:(entity~'uuid'):'team',
        modify(player, 'effect', 'weakness', 1, 255, false, false);
    );
);

__on_player_dies(player) -> (
    if(global_status:'game' == 'started',
        modify(player, 'health', 20);
        sound('minecraft:entity.wither.spawn', [0, 0, 0], 99999, 1, 'master');

        if(global_players:(player~'uuid'):'alive',
            run(str('give %s player_head{SkullOwner:"%s"}', player, player));

            if(global_settings:'gamerules':'player_drop_gapple',
                run(str('give %s golden_apple', player));
            );
        );

        schedule(0, _(player) -> (
            modify(player, 'gamemode', 'spectator');
            global_players:(player~'uuid'):'alive' = false;
            update_teams(player~'uuid');
        ), player);
    );
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if(global_status:'game' == 'pending' && !global_settings:'other':'solo_mode',
        block_to_team = {
            'light_gray_stained_glass' -> 'spectator',
            'gray_stained_glass' -> 'spectator',
            'black_stained_glass' -> 'spectator',

            'light_blue_wool' -> 'aqua',
            'black_wool' -> 'black',
            'blue_wool' -> 'blue',
            'cyan_wool' -> 'dark_aqua',
            'purple_wool' -> 'dark_blue',
            'gray_wool' -> 'dark_gray',
            'green_wool' -> 'dark_green',
            'magenta_wool' -> 'dark_purple',
            'brown_wool' -> 'dark_red',
            'orange_wool' -> 'gold',
            'light_gray_wool' -> 'gray',
            'lime_wool' -> 'green',
            'pink_wool' -> 'light_purple',
            'red_wool' -> 'red',
            'yellow_wool' -> 'yellow',
            'white_wool' -> 'white',
        };

        if(block_to_team~str(block),
            team_join(player, block_to_team:str(block));

            if(block_to_team:str(block) == 'spectator',
                display_title(player, 'actionbar', format('f You are now a spectator', ));
                ,
                team_name = replace(block_to_team:str(block), '_', ' ');
                display_title(player, 'actionbar', add_format_color(' You joined team '+team_name, block_to_team:str(block)));
            );
        );
    , global_status:'game' == 'pending' && global_settings:'other':'solo_mode',
        if(str(block)~'wool',
            team_join(player, str(player));
            display_title(player, 'actionbar', format(' You are now playing', ));
        , [null, 'light_gray_stained_glass', 'gray_stained_glass', 'black_stained_glass']~str(block),
            team_join(player, 'spectator');
            display_title(player, 'actionbar', format('f You are now a spectator', ));
        );
    );

    if(!global_settings:'other':'enchanted_gapple' && inventory_size(block),
        while( (slot = inventory_find(block, 'enchanted_golden_apple', slot)) != null, inventory_size(block),
            inventory_set(block, slot, inventory_get(block, slot):1, 'golden_apple');
        );
    );
);

__on_player_uses_item(player, item_tuple, hand) -> (
    if(!global_settings:'other':'enchanted_gapple' && item_tuple:0 == 'enchanted_golden_apple',
        while( (slot = inventory_find(player, 'enchanted_golden_apple', slot)) != null, 41,
            inventory_set(player, slot, inventory_get(player, slot):1, 'golden_apple');
        );
    );
    
    if(!global_settings:'other':'suspicious_stew' && item_tuple:0 == 'suspicious_stew' && parse_nbt(item_tuple:2):'Effects':0 == {'EffectId'->10, 'EffectDuration'->160},
        while( (slot = inventory_find(player, 'suspicious_stew', slot)) != null, 41,
            if(parse_nbt(inventory_get(player, slot):2):'Effects':0 == {'EffectId'->10, 'EffectDuration'->160},
                inventory_set(player, slot, inventory_get(player, slot):1, 'suspicious_stew');
            );
        );
        display_title(player, 'actionbar', format('r Suspicious stew regeneration has been disabled'));
    );
);

__on_player_starts_sneaking(player) -> (
    if(global_settings:'other':'teammate_direction' &&
    global_status:'game' == 'started' &&
    global_players:(player~'uuid'):'alive' &&
    global_players:(player~'uuid'):'team' != 'spectator',

        show_directions(player);
    
    );
);
