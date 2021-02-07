__config()-> {
    'scope' -> 'global',
    'stay_loaded' -> 'true',
    'command_permission' -> 'ops',
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

// # Starting fuctions
load_status() -> (
    default_status = {
        'game' -> 'pending',
        'time' -> 0,
        'border' -> 'static',
        'border_size' -> 750,
        'total_teams' -> 0,
        'total_players' -> 0,
    };

    loaded_status = load_app_data();
    if(!loaded_status, 
        global_status = default_status;
        logger('info', '[Scarpet UHC] No app data found, loading defaults...');
        ,
        global_status = parse_nbt(loaded_status);
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
            'mode' -> 'teams',
            'day_light_cycle' -> false,
            // 'day_time' -> 6000,
            'allow_nether' -> true,
            'player_drop_gapple' -> true,
            'natural_regeneration' -> false,
            'final_heal_amount' -> 0,
            // 'cut_clean' -> false,
            'death_by_creeper' -> true,
            // 'death_message' -> true,
        },
        'teams' -> {
            'friendly_fire' -> true,
            'show_nametag' -> true,
            'see_invisible' -> true,
            'collision' -> true,
        },
    };

    loaded_settings = read_file('settings', 'json');
    if(!loaded_settings, 
        global_settings = default_settings;
        logger('info', '[Scarpet UHC] No settings found, loading defaults...');
        ,
        global_settings = loaded_settings;
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
        team_add(entity_id(_));
        team_add(entity_id(_), entity_id(_));

        if(global_players:_:'team' == 'spectator',
            team_property(entity_id(_), 'color', 'gray');
            team_property(entity_id(_), 'prefix', '[Spec] ');
            ,
            team_property(entity_id(_), 'color', global_players:_:'team');
            team_property(entity_id(_), 'prefix', '🗡 ');
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

    save_players();
);

__on_start();
