__config() -> {'scope' -> 'global', 'stay_loaded'->'false',
	'allow_command_conflicts' -> 'true',
	'arguments' -> {
		'function' -> {'type'->'term', 'options' -> ['pause', 'start', 'help'], 'suggest' -> ['pause', 'start', 'help']},
		'cornerA' -> {'type'->'surfacelocation'},
		'cornerB' -> {'type'->'surfacelocation'},
		'chunkPerSec' -> {'type' -> 'int', 'min' -> 1, 'suggest' -> [20]},
		'saveFrequency' -> {'type'->'int', 'min'-> 20, 'suggest' -> [1200]},
		'inDimension' -> {'type'->'term', 'suggest' -> ['overworld', 'the_nether', 'the_end', '___', '__']}
	},
	'commands' -> {
		'' -> _() -> print('Use /world_generator help to see all the commands'),
		'help <function>' -> ['wg_help'],
		'info' -> ['wg_info'],
		'pause' -> ['wg_pause'],
		'cancel' -> _() -> print('Are you sure you want to cancel ? Use /world_generator cancel confirm to confirm'),
		'cancel confirm' -> ['wg_cancel'],
		'start' -> ['wg_help', 'start'],
		'start <cornerA> <cornerB>' -> ['wg_start', 'overworld', 20, 1200],
		'start <cornerA> <cornerB> <inDimension>' -> ['wg_start', 20, 1200],
		'start <cornerA> <cornerB> <inDimension> <chunkPerSec> <saveFrequency>' -> ['wg_start'],
	}
};

global_task = {};
global_step_counter = 0;
global_save_timer = 0;

__on_start() -> (
	loaded_task = load_app_data();
	if(!loaded_task, 
		global_task = {
			'state' -> 'waiting',
			'cornerA' -> [0, 0],
			'cornerB' -> [0, 0],
			'current_pos' -> [0, 0, 0],
			'dimension' -> [],
			'chunk_per_tick' -> 1,
			'save_interval' -> 1200,
			'step' -> 0,
			'step_total' -> 0,
		},
		global_task = parse_nbt(loaded_task);
		if(global_task:'state' == 'generating', logger('info', '[World Generator] Running task found. Continuing'););
	);
);
__on_start();

__on_close() -> (
	store_app_data(nbt(global_task));
);

wg_help(subject) -> (
	print('It is comming (sent '+subject+']')
);

wg_info() -> (
	if(global_task:'state' == 'generating' || global_task:'state' == 'paused',
		print('Current task :');
		print(' - State : ' + global_task:'state');
		print(' - 1st corner : ' + global_task:'cornerA');
		print(' - 2nd corner : ' + global_task:'cornerB');
		print(' - Current generator position : ' + global_task:'current_pos');
		print(' - Dimensions to generate : ' + global_task:'dimension');
		print(' - Chunks to generate per tick : ' + global_task:'chunk_per_tick');
		print(' - Save interval : ' + global_task:'save_interval');
		print(' - Current step : ' + global_task:'step');
		print(' - Total steps : ' + global_task:'step_total');
		,
		print(format('r There is no running generation task'));
	);
);

wg_start(corner1, corner2, dim, chunkps, save_every) -> (
	if(global_task:'state' == 'generating' || global_task:'state' == 'paused', 
		print(format('r There is already a running generation task'));
		return();
	);

	[x1, x2] = sort(corner1:0 - (corner1:0 % 16), corner2:0 - (corner2:0 % 16));
	[y1, y2] = sort(corner1:1 - (corner1:1 % 16), corner2:1 - (corner2:1 % 16));
	
	if(dim == '__',
		dim = ['overworld', 'the_nether']
	);
	if(dim == '___',
		dim = ['overworld', 'the_nether', 'the_end']
	);
	if(type(dim) == 'string',
		dim = [dim]
	);
	
	global_task = {
		'state' -> 'generating',
		'cornerA' -> [x1, y1],
		'cornerB' -> [x2, y2],
		'current_pos' -> [x1, y1, 0],
		'dimension' -> dim,
		'chunk_per_tick' -> chunkps / 20,
		'save_interval' -> save_every,
		'step' -> 0,
		'step_total' -> abs((x1/16-x2/16)*(y1/16-y2/16)) * length(dim),
	};
	
	print(format(
		' World generation started for ', ' '+global_task:'dimension',
		'  going from ', ' '+global_task:'cornerA', '  to ', ' '+global_task:'cornerB',
		'  (', ' '+global_task:'step_total', '  chunks)'
	));
	logger(
		'[World Generator] '+
		'World generation started for '+ global_task:'dimension'+
		' going from '+ global_task:'cornerA'+ ' to '+ global_task:'cornerB'+
		' ('+ global_task:'step_total'+ ' chunks)'
	);
);

wg_pause() -> (
	if(global_task:'state' == 'generating',
		global_task:'state' = 'paused';
		print(format(' World generation paused on step ', ' '+global_task:'step', ' /', ' '+global_task:'step_total'));
		logger(
			'[World Generator] '+
			'World generation paused on step '+global_task:'step'+'/'+global_task:'step_total'
		);
		return();
	);
	if(global_task:'state' == 'paused',
		global_task:'state' = 'generating';
		print(format(' World generation contining from step ', ' '+global_task:'step', ' /', ' '+global_task:'step_total'));
		logger(
			'[World Generator] '+
			'World generation continuing from step '+global_task:'step'+'/'+global_task:'step_total'
		);
		return();
	);
	
	print(format('r There is no running generation task'));
	logger('warn', '[World Generator] There is no running generation task');
);

wg_cancel() -> (
	if(global_task:'state' == 'generating' || global_task:'state' == 'paused',
		global_task:'state' = 'canceled';
		
		save();
		print(format('r World generation canceled on step ', 'ri '+global_task:'step', 'r /', 'rb '+global_task:'step_total'));
		logger('warn', '[World Generator] Generation task canceled on step '+global_task:'step'+'/'+global_task:'step_total');
		return();
	);
	
	print(format('r There is no running generation task'));
	logger('warn', '[World Generator] There is no running generation task');
);

wg_end() -> (
	global_task:'state' = 'waiting';
	
	save();
	print(player('*'), format(' World generation ended after ', ' '+global_task:'step', '  steps'));
	logger('warn', '[World Generator] Generation task ended');
);

__on_tick() -> (
	if(global_task:'state' == 'generating',
		global_step_counter += global_task:'chunk_per_tick';
		while(global_step_counter >= 1, 999,
			global_step_counter += -1;
			global_task:'step' += 1;;
			global_save_timer += 1;
			
			in_dimension(global_task:'dimension':(global_task:'current_pos':2), 
				block(global_task:'current_pos':0, 0, global_task:'current_pos':1);
			);
			
			global_task:'current_pos':0 += 16;
			if(global_task:'current_pos':0 >= global_task:'cornerB':0, 
				global_task:'current_pos':0 = global_task:'cornerA':0;
				global_task:'current_pos':1 += 16; 
			);
			if(global_task:'current_pos':1 >= global_task:'cornerB':1, 
				global_task:'current_pos':1 = global_task:'cornerA':1;
				global_task:'current_pos':2 += 1; 
				
				if(global_task:'current_pos':2 >= length(global_task:'dimension'), 
					wg_end();
					return();
					,
					logger('info', '[World Generator] Changing dimension...');
				);
			);
			
			if(tick_time() % 100 == 0,
				logger('info', '[World Generator] Generating... Step '+global_task:'step'+'/'+global_task:'step_total'+
				', ~'+round(global_task:'step'/global_task:'step_total'*1000)*0.1+'%');
				// print('[World Generator] Generating... Step '+global_task:'step'+'/'+global_task:'step_total'+
				// ', ~'+round(global_task:'step'/global_task:'step_total'*1000)*0.1+'%');
			);
			
			if(global_save_timer == global_task:'save_interval',
				global_save_timer = 0;
				logger('info', '[World Generator] Saving progress');
				save();
				
				global_step_counter = 0;
				break();
			);
		);
	);
);
