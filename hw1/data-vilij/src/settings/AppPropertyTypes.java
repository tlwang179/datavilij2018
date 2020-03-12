package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    NOTIFICATIONS_TITLE,
    NOTIFICATIONS_MESSAGE,
    NOTIFICATIONS_LINES,
    ERROR_TITLE,
    ERROR_AT,
    ERROR_NAME,
    ERROR_NAME2,
    ERROR_COORDINATES,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    CHART_TITLE,
    DISPLAY_BUTTON_TEXT,
    PNG,
    AVERAGE,
    AVERAGE_LINE,
    AVERAGE_REMOVE_NODE,

    RANDOMCLASSI,
    RANDOMCLUSTERER,
    KMEANSCLUSTERER,
}
