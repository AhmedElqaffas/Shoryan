package com.example.shoryan.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.isFocused
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

object PinEntryComposableDirection{
    const val RTL = 0
    const val LTR = 1
}
/**
 * Contains the cells where the code is inserted
 * @param numberOfCells The number of character in the code
 * @param modifier: The modifier to apply to this composable
 * @param cellsMarginPercentage Space between cells divided by cell size, i.e., ratio between
 * cell size and white space between cells
 * @param cellColor The background color of the cell
 * @param activeBorderColor The color of the border around the cell, when the cell is in focus
 * @param inActiveBorderColor The color of the border around the cell, when the cell isn't in focus
 * @param activeBorderWidth The width of the border around the cell, when the cell is in focus
 * @param inActiveBorderWidth The width of the border around the cell, when the cell isn't in focus
 * @param isPassword A boolean that determines whether the characters should be treated as password
 * characters or be left as plain text
 * @param onChange A function invoked when a new code character is entered
 * @param onCodeEntered A function invoked when a new code character is entered
 * @param layoutDirection The pin entry should always be LTR, the layout direction
 * ([PinEntryComposableDirection.LTR] or [PinEntryComposableDirection.RTL]) should be specified to
 * avoid having an inverted composable in case the locale of the screen forces the composables to
 * be drawn in a RTL fashion.
 */
@Composable
fun PinEntryComposable(
    numberOfCells: Int,
    modifier: Modifier = Modifier,
    cellsMarginPercentage: Float = 0.5f,
    cellColor: Color = Color.White,
    activeBorderColor: Color = MaterialTheme.colors.primary,
    inActiveBorderColor: Color = Color.Black,
    activeBorderWidth: Dp = 5.dp,
    inActiveBorderWidth: Dp = 3.dp,
    isPassword: Boolean = false,
    onChange: (String) -> Unit,
    onCodeEntered: (String) -> Unit,
    layoutDirection: Int
){
    // Create a focusRequester for each cell to use it to give focus to a cell when needed
    val focusRequesters = List(numberOfCells) { FocusRequester() }
    /* Keep the text state of each cell in this component and pass it later to the cells so that
       each cell knows the states of the other cells
    */
    val cellsTexts = List(numberOfCells){mutableStateOf("")}
    Column{
        Row(
            modifier = modifier
        ) {
            for(i in 0 until numberOfCells){
                /* Each cell should should know the focusRequester of the cell directly after and
                   before it. Last cell doesn't have any cell after it, therefore its
                   "nextFocus" value = null. First cell doesn't have any cell before it, therefore its
                   "previousFocus" value = null
                */
                var nextFocus: FocusRequester?
                var previousFocus: FocusRequester?
                /* In a LTR layout direction, the cells are drawn from left to right, so the "next"
                   of a cell is the one following it in the focusRequesters list, and the
                   "previous" of the cell is the one preceding it in the focusRequesters list
                 */
                if(layoutDirection == PinEntryComposableDirection.LTR){
                    nextFocus = if(i < numberOfCells - 1) focusRequesters[i + 1] else null
                    previousFocus = if(i > 0) focusRequesters[i - 1] else null
                } else{
                    /* In a RTL layout direction, the cells are drawn from right to left, so the "next"
                     of a cell is the one preceding it in the focusRequesters list, and the
                    "previous" of the cell is the one following it in the focusRequesters list
                    */
                    nextFocus = if(i > 0) focusRequesters[i - 1] else null
                    previousFocus = if(i < numberOfCells - 1) focusRequesters[i + 1] else null
                }

                Cell(this, i, cellsTexts, focusRequesters[i], nextFocus, previousFocus,
                    cellColor = cellColor,
                    activeBorderColor = activeBorderColor,
                    inActiveBorderColor = inActiveBorderColor,
                    activeBorderWidth = activeBorderWidth,
                    inActiveBorderWidth = inActiveBorderWidth,
                    isPassword = isPassword,
                    onChange = onChange,
                    onCodeEntered = onCodeEntered,
                    layoutDirection = layoutDirection
                )
                // Add space between cells
                if(i != numberOfCells - 1)
                    Spacer(modifier = Modifier.weight(cellsMarginPercentage))
            }
        }
    }

}

/**
 * Each cell represents a character field in the CodeEntryComposable.
 * @param parentLayoutScope The parent row scope.
 * @param id The order of the cell.
 * @param cellsText A list containing the text state of each cell.
 * @param focusRequester Reference to the FocusRequester used to request focus to this cell.
 * @param nextFocusRequester nullable reference to the FocusRequester used to request focus to
 * the cell following this cell. It is null if this is the last cell
 * @param previousFocusRequester nullable reference to the FocusRequester used to request focus to
 * the cell preceding this cell. It is null if this is the first cell
 * @param cellColor The background color of the cell
 * @param activeBorderColor The color of the border around the cell, when the cell is in focus
 * @param inActiveBorderColor The color of the border around the cell, when the cell isn't in focus
 * @param activeBorderWidth The width of the border around the cell, when the cell is in focus
 * @param inActiveBorderWidth The width of the border around the cell, when the cell isn't in focus
 * @param isPassword A boolean that determines whether the characters should be treated as password
 * characters or be left as plain text
 * @param onChange A function invoked when a new code character is entered
 * @param onCodeEntered A function invoked when all cells are filled with characters
 * @param layoutDirection The pin entry should always be LTR, the layout direction
 * ([PinEntryComposableDirection.LTR] or [PinEntryComposableDirection.RTL]) should be specified to
 * avoid having an inverted composable in case the locale of the screen forces the composables to
 * be drawn in a RTL fashion.
 */

@Composable
fun Cell(
    parentLayoutScope: RowScope,
    id: Int,
    cellsText: List<MutableState<String>>,
    focusRequester: FocusRequester,
    nextFocusRequester: FocusRequester?,
    previousFocusRequester: FocusRequester?,
    cellColor: Color,
    activeBorderColor: Color,
    inActiveBorderColor: Color,
    activeBorderWidth: Dp,
    inActiveBorderWidth: Dp,
    isPassword: Boolean,
    onChange: (String) -> Unit,
    onCodeEntered: (String) -> Unit,
    layoutDirection: Int
) {
    /* "textBeforeChange" is used to determine the change in text. For example if textBeforeChange
       = "5" and the cellsText[id] = "85", we can conclude that the new text entered = 8.
       That is because , although the cursor is invisible, the cursor position may be at the start
       of the TextField instead of the end, so entering a new character (8 in this example) will
       be appended to the start of cellsText[id] string instead of the end.
       So we need "textBeforeChange" state to determine the new entered character whether the cursor
       was at the start or the end of the TextField.
    */
    val textBeforeChange = remember{mutableStateOf("")}
    val borderColor = remember { mutableStateOf(inActiveBorderColor) }
    val borderWidth = remember { mutableStateOf(1.dp) }
    parentLayoutScope.apply{
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val fontSize = min(maxHeight, maxWidth)
            BasicTextField(
                value = cellsText[id].value,
                onValueChange = { it ->
                    if(it.isEmpty()) {
                        cellsText[id].value = it
                        previousFocusRequester?.requestFocus()
                    }
                    else{
                        if(it[0].toString() == textBeforeChange.value && textBeforeChange.value.isNotEmpty()) {
                            cellsText[id].value = it.last().toString()
                        }
                        else {
                            cellsText[id].value = it[0].toString()
                        }
                        if(nextFocusRequester != null)
                            nextFocusRequester.requestFocus()
                        else if(cellsText.all{state -> state.value.isNotEmpty()}) {
                            onCodeEntered(extractCodeString(cellsText, layoutDirection))
                        }
                    }
                    onChange(extractCodeString(cellsText, layoutDirection))
                    textBeforeChange.value = cellsText[id].value
                },
                modifier = Modifier
                    .onFocusChanged {
                        borderColor.value = if (it.isFocused) activeBorderColor else inActiveBorderColor
                        borderWidth.value = if (it.isFocused) activeBorderWidth else inActiveBorderWidth
                    }
                    .focusOrder(focusRequester) {
                        /* When the user clicks "Next" on the keyboard:
                           - If this isn't the last cell, the focus is passed to the next cell
                           - If this is the last cell, and all cells contain values, invoke "onCodeEntered"
                        */
                        if (nextFocusRequester != null)
                            nextFocusRequester.requestFocus()
                        else if (cellsText.all { state -> state.value.isNotEmpty() }) {
                            focusRequester.freeFocus()
                            onCodeEntered(extractCodeString(cellsText, layoutDirection))
                        }
                        else{
                            onChange(extractCodeString(cellsText, layoutDirection))
                        }
                    }

                    .border(borderWidth.value, borderColor.value, LineBorder)
                    .background(cellColor),
                cursorBrush = SolidColor(Color.Unspecified),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if(isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                textStyle = TextStyle(fontSize= LocalDensity.current.run { fontSize.toSp() }, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
            )
        }

    }
}

fun extractCodeString(cellsText: List<MutableState<String>>, layoutDirection: Int): String {
    var codeString = ""
    cellsText.forEach{
        codeString += it.value
    }
    if(layoutDirection != PinEntryComposableDirection.LTR)
        codeString = codeString.reversed()
    return codeString
}

private val LineBorder = GenericShape { box, _ ->
    moveTo(0f, box.height)
    lineTo(box.width, box.height)
    moveTo(0f, box.height-1)
    lineTo(box.width, box.height-1)
    moveTo(0f, box.height-2)
    lineTo(box.width, box.height-2)
    moveTo(0f, box.height-3)
    lineTo(box.width, box.height-3)
    moveTo(0f, box.height-4)
    lineTo(box.width, box.height-4)
    moveTo(0f, box.height-5)
    lineTo(box.width, box.height-5)
}