__config()-> {
    'scope' -> 'global',
    'stay_loaded' -> 'true',
    'command_permission' -> 'ops',
};

global_players = {};
global_settings = {};
global_status = {};

load_status() -> (
    defaut_status = {
        '' -> '',
    }; // TODO

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
        '' -> '',
    }; // TODO

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
