package com.app.personas_social.utlis

import retrofit2.http.DELETE

const val APP_FOLDER = "VideoEditor"
const val PROMO_FOLDER = "PromotionalImage"
const val RECORDED_FILE = "RecordedFile_"

const val ADD_TEXT = 0 // Add text button view
const val TRIM = 2
const val SPEED = 2
const val CROP = 3
const val FILTER = 3
const val ADD_MUSIC = 1 // Add music button view
const val VOLUME = 6
const val EFFECTS = 1
const val RECORD = 2
const val CROP_MEDIA = 8
const val SPLIT = 9
const val ADD_TEXT_VIEW = 0 // View with adding text with all it's attributes
const val ADD_TEXT_VIEW_MULTIPLE = 11 // View with selected text list
const val ON_PROGRESS = 12
const val ROTATE = 13
const val MUSIC = 0 // Music selection view
const val MUSIC_MULTIPLE = 15 // Selected music list view
const val DIALOG_DISMISS = 16 // Selected music list view
const val MSG_MUSIC_ERROR = 17 // Selected music list view
const val AUDIO_RECORDER = 18 // Selected music list view
const val BACKGROUND = 19

/**
 * startActivityForResult flags
 */
const val REQUEST_VIDEO_CAPTURE = 1
const val REQUEST_IMAGE_CAPTURE = 2
const val REQUEST_CHOOSE_IMAGE = 3
const val REQUEST_ADD_VIDEO = 4
const val REQUEST_PROJECT = 114

/**
 * Permission flags
 */
const val WRITE_PERMISSION_STORAGE = 103
const val PERMISSION_STORAGE = 101
const val PERMISSION_CAMERA = 102
const val PERMISSION_READ_STORAGE = 103
const val PERMISSION_RECORD_AUDIO = 104

const val VIDEO_SEEK_DURATION = 5000 //In millisec
const val VIDEO_FULL_SCREEN = 111 //In millisec
const val VIDEO_1080 = 1080
const val VIDEO_1920 = 1920
const val NORMAL_VIDEO_SPEED = 50f //(It's 1f)

const val FILE = 1
const val FOLDER = 2

const val MUSIC_EDIT = 101
const val MUSIC_DELETE = 102

/**
 * User actions flags
 */
const val TAB_TEXT = 0
const val TAB_PRODUCT = 1
const val TAB_FORM = 2
const val GENERATE_THUMB = 444
const val PROGRESS_CANCEL_CLICK = 24
const val RESET_DATA = 25

/**
 * Data key flags
 */
const val ARR_VIDEO = "ARR_VIDEO"
const val START_DURATION = "START_DURATION"
const val IS_INTERNET = "IS_INTERNET"
const val IS_ADD_VIDEO = "IS_ADD_VIDEO"
const val PREF_STORAGE = "PREF_STORAGE"
const val BLANK_BORDER = "BlankBorder"
const val LIGHT_BORDER = "LightBorder"
const val DARK_BORDER = "DarkBorder"
const val MULTICOLOR_BORDER = "MultiColorBorder"
const val DOT_BORDER = "DotBorder"
const val RED_BORDER = "RedBorder"
const val IS_EFFECT = "Effect"
const val IS_MUSIC = "Music"
const val IS_RECORD = "Record"
var isadded = false


/*text update type*/
const val TEXT_ADD = 1
const val ACTION_COLOR = 13
const val ACTION_GRADIENT = 2
const val ACTION_PATTERN = 3
const val ACTION_STYLE = 4
const val ACTION_BORDER = 5
const val ACTION_RESIZE = 9
const val ACTION_FONT = 10
const val DRAG_TEXT = 14
const val EDIT_TEXT = 15
const val DELETE_TEXT = 16
const val MUSIC_ADD = 17
const val MUSIC_DRAG = 18
const val ACTION_TEXT_CHANGE = 12


/*actions*/
const val ADD = 11
const val ACTION_FILTER = 22
const val ACTION_FILTER_ALL = 33
const val ACTION_TRIM = 44
const val ACTION_MUSIC = 55
const val DELETE = 4
