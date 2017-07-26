package Utilities;

import java.io.File;

public class Constants {
	public final static String MOVIES_INFOS = "./movieInfos";
	
	public final static String SCRIPT = "script";
	public final static String SUBTITLE = "subtitle";
	
	public final static String STEP1_FOLDER = "." + File.separator + "Download";
	public final static String STEP1_SCRIPTS_FOLDER = STEP1_FOLDER + File.separator + "scripts";
	public final static String STEP1_SUBTITLES_FOLDER = STEP1_FOLDER + File.separator + "subtitles";
	
	public final static String STEP2_FOLDER = "." + File.separator + "Sanitize";
	public final static String STEP2_SCRIPTS_FOLDER = STEP2_FOLDER + File.separator + "scripts";
	public final static String STEP2_SUBTITLES_FOLDER = STEP2_FOLDER + File.separator + "subtitles";
	
	public final static String STEP3_FOLDER = "." + File.separator + "Annotate";
	public final static String STEP3_SCRIPTS_FOLDER = STEP3_FOLDER + File.separator + "scripts";
	public final static String STEP3_SUBTITLES_FOLDER = STEP3_FOLDER + File.separator + "subtitles";
	
	public final static String STEP4_FOLDER = "." + File.separator + "Sincronize";
	public final static String STEP4_SCRIPTS_FOLDER = STEP4_FOLDER + File.separator + "scripts";
	
	public final static String DETAILS_FOLDER = "." + File.separator + "additionalDetails";
	
	public final static String TOPIC_FOLDER = "." + File.separator + "topicDetails";
	
	public final static Integer UNKNOWN = 0;
	public final static Integer SCENE_BOUNDARY = 1;
	public final static Integer SCENE_DESCRIPTION = 2;
	public final static Integer CHARACTER_NAME = 3;
	public final static Integer DIALOGUE = 4;
	public final static Integer META_DATA = 5;
	public final static Integer POS = 6;
	
	public final static String[] SCENE_BOUNDARY_ARRAY = {"INT.", "EXT."};
	
	public final static String[] CHARACTER_NAME_ARRAY = {"(V.O.)", "(O.S.)", "(CONT'D)", "(V.O)", "(V.0.)", "(O.S)"};
	
	public final static String[] META_ARRAY = {"CUT TO:", "CUT BACK TO", "PAN TO", "FADE IN", "FADE TO", 
			"FADE OUT", "PULL BACK TO", "WIPE TO", "DISSOLVE TO", "SCREEN BLACK", "CONTINUED"};
	public final static String[] END_ARRAY = {"THE END", "END", "End", "end"};
	
	public final static Integer UPPERCASE = 0;
	public final static Integer NOLETTERS = 1;
	
	public final static Integer ERROR_CODE = -1;
	
	public final static String ELASTICSEARCH_SUBTITLE_TYPE = "subtitle";
	
	public final static float ACCEPTED_ERROR = 0.075f;
	
	public final static float MIN_SCORE_MATCH_PHRASE = 3f;
	public final static float MAX_SCORE_MATCH_PHRASE = 17f;
	public final static float STEP_MATCH_PHRASE = 2f;
	
	public final static float MIN_SCORE_MATCH = 2f;
	public final static float MAX_SCORE_MATCH = 9f;
	public final static float STEP_MATCH = 1f;
	
	public final static float MIN_SCORE_COMMON_MATCH = 3f;
	public final static float MAX_SCORE_COMMON_MATCH = 9f;
	public final static float STEP_COMMON_MATCH = 1f;
	
	public final static Integer MATCH_PHRASE_SEARCH = 0;
	public final static Integer MATCH_SEARCH = 1;
	public final static Integer COMMON_MATCH_SEARCH = 2;
	
	public final static String PARANTHESIS_OPEN = "{";
	public final static String PARANTHESIS_CLOSE = "}";
	
	public final static String LOG_FILENAME = "./log.txt";
	
	public final static String ERROR = "ERROR";
	public final static String EXCEPTION = "EXCEPTION";
	public final static String INFO = "INFO";
	public final static String DEBUG = "DEBUG";
	
	public final static int ELASTICSEARCH_NR_NODES = 4;
	public final static String ELASTICSEARCH_CLUSTER_NAME = "alexandru.hulea";
	
	public final static int META_DATA_NO_TYPE = 0;
	public final static int META_DATA_TRANSITION = 1;
	public final static int META_DATA_AUTHOR_DIRECTION = 2;
	
	public final static int CHARACTER_NAME_NO_TYPE = 0;
	public final static int CHARACTER_NAME_VO = 1;
	public final static int CHARACTER_NAME_OS = 2;
	public final static int CHARACTER_NAME_CONTD = 3;
	
	public final static String[] SCENE_BOUNDARIES_TIME = {"NIGHT", "DAY", "AFTERNOON", "MORNING", "SUNSET", "EVENING", "SUNRISE", "MOMENTS LATER", "LATER"};
	public final static String[] SCENE_BOUNDARIES_META_DATA = {"CONTINUOUS", "FLASHBACK", "RESUMING", "COLOUR SEQUENCE", "BLACK AND WHITE SEQUENCE", "BLACK AND WHITE"};

	public final static int NO_RESULT_FILES = 0;
	
	public final static String MURDER_FILENAME = "murder";
	public final static String CARS_FILENAME = "cars";
	
	public final static Integer HANDLE_ERRORS_NOBODY_WINS = 0;
	public final static Integer HANDLE_ERRORS_BETTER_SCORE_WINS = 1;
	// if a pair has less errors than every other pair that is in error with it, if equal nobody wins
	public final static Integer HANDLE_ERRORS_LESS_ERRORS_WINS_NOBODY_WINS = 2; 
	// if equal the better score wins
	public final static Integer HANDLE_ERRORS_LESS_ERRORS_WINS_BETTER_SCORE_WINS = 3; 
}
