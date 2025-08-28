/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen0539;

import java.util.HashMap;
import java.util.Map;

public enum DF2301QCommand {
  SILENCE(0, "Silence"),
  WAKE_UP_WORDS_FOR_LEARNING(1, "Wake Up Words For Learning"),
  HELLO_ROBOT(2, "Hello Robot"),
  CUSTOM_COMMAND_1(5, "Custom Command 1"),
  CUSTOM_COMMAND_2(6, "Custom Command 2"),
  CUSTOM_COMMAND_3(7, "Custom Command 3"),
  CUSTOM_COMMAND_4(8, "Custom Command 4"),
  CUSTOM_COMMAND_5(9, "Custom Command 5"),
  CUSTOM_COMMAND_6(10, "Custom Command 6"),
  CUSTOM_COMMAND_7(11, "Custom Command 7"),
  CUSTOM_COMMAND_8(12, "Custom Command 8"),
  CUSTOM_COMMAND_9(13, "Custom Command 9"),
  CUSTOM_COMMAND_10(14, "Custom Command 10"),
  CUSTOM_COMMAND_11(15, "Custom Command 11"),
  CUSTOM_COMMAND_12(16, "Custom Command 12"),
  CUSTOM_COMMAND_13(17, "Custom Command 13"),
  CUSTOM_COMMAND_14(18, "Custom Command 14"),
  CUSTOM_COMMAND_15(19, "Custom Command 15"),
  CUSTOM_COMMAND_16(20, "Custom Command 16"),
  CUSTOM_COMMAND_17(21, "Custom Command 17"),
  GO_FORWARD(22, "Go Forward"),
  RETREAT(23, "Retreat"),
  PARK_A_CAR(24, "Park A Car"),
  TURN_LEFT_NINETY_DEGREES(25, "Turn Left Ninety Degrees"),
  TURN_LEFT_FORTY_FIVE_DEGREES(26, "Turn Left Forty Five Degrees"),
  TURN_LEFT_THIRTY_DEGREES(27, "Turn Left Thirty Degrees"),
  TURN_RIGHT_FORTY_FIVE_DEGREES(29, "Turn Right Forty Five Degrees"),
  TURN_RIGHT_THIRTY_DEGREES(30, "Turn Right Thirty Degrees"),
  SHIFT_DOWN_A_GEAR(31, "Shift Down A Gear"),
  LINE_TRACKING_MODE(32, "Line Tracking Mode"),
  LIGHT_TRACKING_MODE(33, "Light Tracking Mode"),
  BLUETOOTH_MODE(34, "Bluetooth Mode"),
  OBSTACLE_AVOIDANCE_MODE(35, "Obstacle Avoidance Mode"),
  FACE_RECOGNITION(36, "Face Recognition"),
  OBJECT_TRACKING(37, "Object Tracking"),
  OBJECT_RECOGNITION(38, "Object Recognition"),
  LINE_TRACKING(39, "Line Tracking"),
  COLOR_RECOGNITION(40, "Color Recognition"),
  TAG_RECOGNITION(41, "Tag Recognition"),
  OBJECT_SORTING(42, "Object Sorting"),
  QR_CODE_RECOGNITION(43, "QR Code Recognition"),
  GENERAL_SETTINGS(44, "General Settings"),
  CLEAR_SCREEN(45, "Clear Screen"),
  LEARN_ONCE(46, "Learn Once"),
  FORGET(47, "Forget"),
  LOAD_MODEL(48, "Load Model"),
  SAVE_MODEL(49, "Save Model"),
  TAKE_PHOTOS_AND_SAVE_THEM(50, "Take Photos And Save Them"),
  SAVE_AND_RETURN(51, "Save And Return"),
  DISPLAY_NUMBER_ZERO(52, "Display Number Zero"),
  DISPLAY_NUMBER_ONE(53, "Display Number One"),
  DISPLAY_NUMBER_TWO(54, "Display Number Two"),
  DISPLAY_NUMBER_THREE(55, "Display Number Three"),
  DISPLAY_NUMBER_FOUR(56, "Display Number Four"),
  DISPLAY_NUMBER_FIVE(57, "Display Number Five"),
  DISPLAY_NUMBER_SIX(58, "Display Number Six"),
  DISPLAY_NUMBER_SEVEN(59, "Display Number Seven"),
  DISPLAY_NUMBER_EIGHT(60, "Display Number Eight"),
  DISPLAY_NUMBER_NINE(61, "Display Number Nine"),
  DISPLAY_SMILEY_FACE(62, "Display Smiley Face"),
  DISPLAY_CRYING_FACE(63, "Display Crying Face"),
  DISPLAY_HEART(64, "Display Heart"),
  TURN_OFF_DOT_MATRIX(65, "Turn Off Dot Matrix"),
  READ_CURRENT_POSTURE(66, "Read Current Posture"),
  READ_AMBIENT_LIGHT(67, "Read Ambient Light"),
  READ_COMPASS(68, "Read Compass"),
  READ_TEMPERATURE(69, "Read Temperature"),
  READ_ACCELERATION(70, "Read Acceleration"),
  READING_SOUND_INTENSITY(71, "Reading Sound Intensity"),
  CALIBRATE_ELECTRONIC_GYROSCOPE(72, "Calibrate Electronic Gyroscope"),
  TURN_ON_THE_CAMERA(73, "Turn On The Camera"),
  TURN_OFF_THE_CAMERA(74, "Turn Off The Camera"),
  TURN_ON_THE_FAN(75, "Turn On The Fan"),
  TURN_OFF_THE_FAN(76, "Turn Off The Fan"),
  TURN_FAN_SPEED_TO_GEAR_ONE(77, "Turn Fan Speed To Gear One"),
  TURN_FAN_SPEED_TO_GEAR_TWO(78, "Turn Fan Speed To Gear Two"),
  TURN_FAN_SPEED_TO_GEAR_THREE(79, "Turn Fan Speed To Gear Three"),
  START_OSCILLATING(80, "Start Oscillating"),
  STOP_OSCILLATING(81, "Stop Oscillating"),
  RESET(82, "Reset"),
  SET_SERVO_TO_TEN_DEGREES(83, "Set Servo To Ten Degrees"),
  SET_SERVO_TO_THIRTY_DEGREES(84, "Set Servo To Thirty Degrees"),
  SET_SERVO_TO_FORTY_FIVE_DEGREES(85, "Set Servo To Forty Five Degrees"),
  SET_SERVO_TO_SIXTY_DEGREES(86, "Set Servo To Sixty Degrees"),
  SET_SERVO_TO_NINETY_DEGREES(87, "Set Servo To Ninety Degrees"),
  TURN_ON_THE_BUZZER(88, "Turn On The Buzzer"),
  TURN_OFF_THE_BUZZER(89, "Turn Off The Buzzer"),
  TURN_ON_THE_SPEAKER(90, "Turn On The Speaker"),
  TURN_OFF_THE_SPEAKER(91, "Turn Off The Speaker"),
  PLAY_MUSIC(92, "Play Music"),
  STOP_PLAYING(93, "Stop Playing"),
  THE_LAST_TRACK(94, "The Last Track"),
  THE_NEXT_TRACK(95, "The Next Track"),
  REPEAT_THIS_TRACK(96, "Repeat This Track"),
  VOLUME_UP(97, "Volume Up"),
  VOLUME_DOWN(98, "Volume Down"),
  CHANGE_VOLUME_TO_MAXIMUM(99, "Change Volume To Maximum"),
  CHANGE_VOLUME_TO_MINIMUM(100, "Change Volume To Minimum"),
  CHANGE_VOLUME_TO_MEDIUM(101, "Change Volume To Medium"),
  PLAY_POEM(102, "Play Poem"),
  TURN_ON_THE_LIGHT(103, "Turn On The Light"),
  TURN_OFF_THE_LIGHT(104, "Turn Off The Light"),
  BRIGHTEN_THE_LIGHT(105, "Brighten The Light"),
  DIM_THE_LIGHT(106, "Dim The Light"),
  ADJUST_BRIGHTNESS_TO_MAXIMUM(107, "Adjust Brightness To Maximum"),
  ADJUST_BRIGHTNESS_TO_MINIMUM(108, "Adjust Brightness To Minimum"),
  INCREASE_COLOR_TEMPERATURE(109, "Increase Color Temperature"),
  DECREASE_COLOR_TEMPERATURE(110, "Decrease Color Temperature"),
  ADJUST_COLOR_TEMPERATURE_TO_MAXIMUM(111, "Adjust Color Temperature To Maximum"),
  ADJUST_COLOR_TEMPERATURE_TO_MINIMUM(112, "Adjust Color Temperature To Minimum"),
  DAYLIGHT_MODE(113, "Daylight Mode"),
  MOONLIGHT_MODE(114, "Moonlight Mode"),
  COLOR_MODE(115, "Color Mode"),
  SET_TO_RED(116, "Set To Red"),
  SET_TO_ORANGE(117, "Set To Orange"),
  SET_TO_YELLOW(118, "Set To Yellow"),
  SET_TO_GREEN(119, "Set To Green"),
  SET_TO_CYAN(120, "Set To Cyan"),
  SET_TO_BLUE(121, "Set To Blue"),
  SET_TO_PURPLE(122, "Set To Purple"),
  SET_TO_WHITE(123, "Set To White"),
  TURN_ON_AC(124, "Turn On AC"),
  TURN_OFF_AC(125, "Turn Off AC"),
  INCREASE_TEMPERATURE(126, "Increase Temperature"),
  DECREASE_TEMPERATURE(127, "Decrease Temperature"),
  COOL_MODE(128, "Cool Mode"),
  HEAT_MODE(129, "Heat Mode"),
  AUTO_MODE(130, "Auto Mode"),
  DRY_MODE(131, "Dry Mode"),
  FAN_MODE(132, "Fan Mode"),
  ENABLE_BLOWING_UP_DOWN(133, "Enable Blowing Up Down"),
  DISABLE_BLOWING_UP_DOWN(134, "Disable Blowing Up Down"),
  ENABLE_BLOWING_RIGHT_LEFT(135, "Enable Blowing Right Left"),
  DISABLE_BLOWING_RIGHT_LEFT(136, "Disable Blowing Right Left"),
  OPEN_THE_WINDOW(137, "Open The Window"),
  CLOSE_THE_WINDOW(138, "Close The Window"),
  OPEN_CURTAIN(139, "Open Curtain"),
  CLOSE_CURTAIN(140, "Close Curtain"),
  OPEN_THE_DOOR(141, "Open The Door"),
  CLOSE_THE_DOOR(142, "Close The Door"),
  LEARNING_WAKE_WORD(200, "Learning Wake Word"),
  LEARNING_COMMAND_WORD(201, "Learning Command Word"),
  RE_LEARN(202, "Re-Learn"),
  EXIT_LEARNING(203, "Exit Learning"),
  I_WANT_TO_DELETE(204, "I Want To Delete"),
  DELETE_WAKE_WORD(205, "Delete Wake Word"),
  DELETE_COMMAND_WORD(206, "Delete Command Word"),
  EXIT_DELETING(207, "Exit Deleting"),
  DELETE_ALL(208, "Delete All");

  private final int id;
  private final String description;

  DF2301QCommand(int id, String description) {
    this.id = id;
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  private static final Map<Integer, DF2301QCommand> BY_ID = new HashMap<>();

  static {
    for (DF2301QCommand c : values()) BY_ID.put(c.id, c);
  }

  public static DF2301QCommand fromId(int id) {
    return BY_ID.get(id);
  }
}