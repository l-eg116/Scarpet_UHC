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
    },
    'arguments' -> {
        'team' -> {'type' -> 'term', 'options' -> ['aqua', 'black', 'blue', 'dark_aqua', 'dark_blue', 'dark_gray', 'dark_green', 
                   'dark_purple', 'dark_red', 'gold', 'gray', 'green', 'light_purple', 'red', 'yellow', 'white',]},
        'teamSize' -> {'type' -> 'int', 'min' -> 1, 'suggest' -> [3]},
        'force' -> {'type' -> 'bool'},
    },
};

global_players = {};
global_settings = {};
global_status = {};

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
        },
        'gamerules' -> {
            'day_light_cycle' -> false,
            // 'day_time' -> 6000,
            'allow_nether' -> true,
            'player_drop_gapple' -> true,
            'natural_regeneration' -> false,
            'final_heal_amount' -> 0,
            'insomnia' -> false,
            'wandering_traders' -> false,
            'patrols' -> false,
            'raids' -> false,
        },
        'settings' -> {
            'mode' -> 'teams',
            'ghost_players' -> true,
            'kill_ghosts_after' -> -1,
            'death_by_creeper' -> true,
            // 'cut_clean' -> false,
            // 'death_message' -> true,
        },
        'teams' -> {
            'friendly_fire' -> true,
            'show_nametag' -> true,
            // 'see_invisible' -> true,
            'collision' -> true,
        },
    };

    loaded_settings = read_file('settings', 'json');
    if(!loaded_settings, 
        global_settings = default_settings;
        logger('info', '[Scarpet UHC] No settings found, loading defaults...');
        ,
        global_settings = default_settings + loaded_settings;
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

        team_add(entity_id(_));
        team_leave(entity_id(_));
        team_add(entity_id(_), entity_id(_));

        if(global_players:_:'team' == 'spectator',
            team_property(entity_id(_), 'color', 'gray');
            team_property(entity_id(_), 'prefix', '<O> ');
            team_property(entity_id(_), 'suffix', '');
            ,
            if(global_players:_:'alive',
                if(global_players:_:'online',
                    team_property(entity_id(_), 'color', 'white');
                    team_property(entity_id(_), 'prefix', add_format_color(' 🗡 ', global_players:_:'team'));
                    team_property(entity_id(_), 'suffix', '');
                    ,
                    team_property(entity_id(_), 'color', 'white');
                    team_property(entity_id(_), 'prefix', add_format_color('o 🗡 ', global_players:_:'team'));
                    team_property(entity_id(_), 'suffix', '');
                );
                ,
                team_property(entity_id(_), 'color', 'gray');
                team_property(entity_id(_), 'prefix', add_format_color(' 🗡 ', global_players:_:'team'));
                team_property(entity_id(_), 'suffix', format('n  ☠'));
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
