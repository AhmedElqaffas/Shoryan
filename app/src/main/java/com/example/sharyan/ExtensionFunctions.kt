package com.example.sharyan

import android.widget.EditText

/**
 * Trims the white spaces at the start and end of EditText text,
 * and reduces any sequence of white spaces in the middle of the string to one space character only.
 *
 * Examples:
 *
 *     " The King " will be returned as "The King"
 *     " The       King " will be returned as "The King"
 *
 * @return a string containing no leading/trailing white spaces,
 * and no more than one consecutive white space in the middle
 */
fun EditText.getStringWithoutAdditionalSpaces() = this.text.toString().trim().replace(Regex("\\s+")," ")
