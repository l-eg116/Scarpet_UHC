global_players = {};
global_settings = {};
global_status = {};

__config()-> {
    'scope' -> 'global',
    'stay_loaded' -> 'true',
    'command_permission' -> 'ops',
    'commands' -> {
        'team join <players> <team>' -> ['comm_team_join'],
        'team leave <players>' -> ['comm_team_join', 'spectator'],
        'team empty <team>' -> ['comm_team_empty'],
        'team randomize <teamSize>' -> ['comm_team_randomize', true],
        'team randomize <teamSize> <force>' -> ['comm_team_randomize'],

        'settings change <category> <setting> <settingValue>' -> ['comm_settings_change'],
        'settings reset' -> ['comm_settings_reset'],
        'settings reload' -> ['comm_settings_reload'],
        'settings list' -> ['comm_settings_list', 'all'],
        'settings list <category>' -> ['comm_settings_list'],
        
        'generate_world' -> ['comm_generate_world'],
    },
    'arguments' -> {
        'team' -> {'type' -> 'term', 'options' -> ['aqua', 'black', 'blue', 'dark_aqua', 'dark_blue', 'dark_gray', 'dark_green', 
                   'dark_purple', 'dark_red', 'gold', 'gray', 'green', 'light_purple', 'red', 'yellow', 'white',]},
        'teamSize' -> {'type' -> 'int', 'min' -> 1, 'suggest' -> [3]},
        'force' -> {'type' -> 'bool'},

        'category' -> {'type' -> 'term', 'suggester' -> _(args) -> (keys(global_settings))},
        'setting' -> {'type' -> 'term', 'suggester' -> _(args) -> (keys(global_settings:(args:'category')))},
        'settingValue' -> {'type' -> 'int', 'suggester' -> _(args) -> [global_settings:(args:'category'):(args:'setting')]},
    },
};

__on_start() -> (
    load_status();
    load_settings();
    load_players();
    generate_hub();
);

__on_close() -> (
    save_status();
    save_settings();
    save_players();
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
            'speed' -> 1,
        },
        'timer' -> {
            'border' -> 20*60*90, // aka 90 minutes
            'nether_closing' -> -1,
            'final_heal' -> -1,
            'pvp' -> 20*60*10, // aka 10 minutes
        },
        'gamerules' -> {
            'day_light_cycle' -> false,
            'day_time' -> 6000,
            'weather_cycle' -> false,
            'allow_nether' -> true,
            'player_drop_gapple' -> true,
            'natural_regeneration' -> false,
            'insomnia' -> false,
            'wandering_traders' -> false,
            'patrols' -> false,
            'raids' -> false,
        },
        'other' -> {
            'solo_mode' -> false,
            'ghost_players' -> true,
            'kill_ghosts_after' -> -1,
            'death_by_creeper' -> true,
            'final_heal_amount' -> 0,
            // 'cut_clean' -> false,
            // 'death_message' -> true,
        },
        'teams' -> {
            'friendly_fire' -> true,
            'show_nametag' -> true,
            // 'collision' -> true,
            'start_radius' -> 500,
            'start_distance' -> 0,
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
comm_team_join(players, team) -> (
    count = for(players,
        team_join(player(_), team);
    );
    print(str('Added %d player%s to team %s.', count, if(count == 1, '', 's'), team));
);

comm_team_empty(team) -> (
    count = for(keys(global_players),
        if(global_players:_:'team' == team,
            global_players:_:'team' = 'spectator';
        );
    );
    update_teams();

    print(str('Removed %d player%s from team %s.', count, if(count == 1, '', 's'), team));
);

comm_team_randomize(team_size, force) -> (
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
        );

        if(i >= length(randomized_players), break());
    );
);

comm_settings_reset() -> (
    global_settings = {};
    save_settings();
    load_settings();
    save_settings();
    print('All settings were reset and saved');
);

comm_settings_reload() -> (
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
    value = number(value);
    if(global_settings~category && global_settings:category~setting && value != null,
        global_settings:category:setting = value;
        save_settings();
        print(str('Changed setting %s in "%s" to %s', setting, category, value));
        ,
        print(format('r The setting that you tried to change does not exist, or you tried to put in a wrong value'));
    );
);

comm_generate_world() -> (
    border = global_settings:'border':'start';
    command = str('/world_generator start %d %d %d %d __ 20 1200', -border, -border, border, border);
    print(format(' Use world_generator app to pregenerate your world . Here is a cool premade command : ', 'i ' + command, '^ Click to execute !', '?' + command));
);

comm_game_top(player) -> (
    player = player(player);

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
team_listing() -> (
    team_list = {};
    for(keys(global_players),
        team_list += global_players:_:'team'
    );
    delete(team_list:'spectator');

    return(team_list);
);

team_count() -> (
    return(length(team_listing()));
);

player_listing(team, online) -> (
    filter(keys(global_players),
        (global_players:_:'team' != 'spectator' && 
         if(team != '', global_players:_:'team' == team, true) && 
         global_players:_:'alive' && 
        (global_players:_:'online' || !online))
    );
);

player_count(team, online) -> (
    return(length(player_listing(team, online)));
);

// # Game start functions
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
);

// # Team spreading
spread_radius_from_distance(distance) -> (
    angle = deg((2*pi) / team_count()) ;
    spread_radius = distance / (sqrt(2*(1 - cos(angle))));
    return(round(spread_radius));
);

spread_coords() -> (
    angle = deg((2 * pi) / team_count());
    random_factor = rand(360);
    coord = [];

    loop(team_count(),
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

    for(team_listing(), i = _i;
        for(player_listing(_, true),
            modify(entity_id(_), 'location', 0, 0, 0, 0, 0);
            modify(entity_id(_), 'pos', coords:i + [0.5, 0.5, 0.5]);
        );
    );
);

// # Joining a team
__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if(global_status:'game' == 'pending',
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

            // TODO add some kind of feedback
        );
    );
);

// # Events
__on_tick() -> (
    if(
        game_status:'game' == pending, (
            modify(player('all'), 'health', 20);
            modify(player('all'), 'hunger', 20);
        )
    );

    if(!global_settings:'gamerules':'weather_cycle', weather('clear', 20*60*10));
    if(!global_settings:'gamerules':'day_light_cycle', day_time(global_settings:'gamerules':'day_time'));
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

    global_players:(player~'uuid'):'online' = true;
    update_teams(player~'uuid');

    save_players();
    
    if(global_status:'game' == 'pending',
        modify(player, 'location', 0, 203, 0, 0, 0);
        modify(player, 'gamemode', 'adventure');
        modify(player, 'health', 20);
        modify(player, 'hunger', 20);
        modify(player, 'saturation', 20);
        modify(player, 'invulnerable', true);
    );
);

__on_player_disconnects(player, reason) -> (
    global_players:(player~'uuid'):'online' = false;
    update_teams(player~'uuid');

    save_players();
);

__on_player_attacks_entity(player, entity) -> (
    if(entity~'type' == 'player' && !global_status:'pvp',
        modify(player, 'effect', 'weakness', 1, 255, false, false);
    );
);

__on_start();
