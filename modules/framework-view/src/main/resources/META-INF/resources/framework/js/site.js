/*
 * Copyright 2013, Rogue.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function initNumericValidators() {
    $(".numeric").keydown(function (e) {
        var keyPressed;
        if (!e) var e = window.event;
        if (e.keyCode) keyPressed = e.keyCode;
        else if (e.which) keyPressed = e.which;
        var hasDecimalPoint = (($(this).val().split('.').length - 1) > 0);
        if (keyPressed == 46 || keyPressed == 8 || ((keyPressed == 190 || keyPressed == 110) && (!hasDecimalPoint)) || keyPressed == 9 || keyPressed == 27 || keyPressed == 13 ||
            // Allow: Ctrl+A
            (keyPressed == 65 && e.ctrlKey === true) ||
            // Allow: home, end, left, right
            (keyPressed >= 35 && keyPressed <= 39)) {
            // let it happen, don't do anything
            return;
        }
        else {
            // Ensure that it is a number and stop the keypress
            if (e.shiftKey || (keyPressed < 48 || keyPressed > 57) && (keyPressed < 96 || keyPressed > 105 )) {
                e.preventDefault();
                return;
            }

            // check if the value has exceed the size set or not.
            var size = $(this).attr("size");
            if (size && $(this).val().length >= size) {
                e.preventDefault();
            }
        }
    });

    $(".numeric").focusout(function (e) {
        var qtyConstraintsEnabled = $(this).attr("qty-constraints");
        if (qtyConstraintsEnabled) {
            var minQty = $(this).attr("minimum-qty");
            if (minQty == null) {
                minQty = 1;
            }
            if ($(this).val() == "" || $(this).val() < minQty) {
                $(this).val(minQty);
            }
        }
    });
}