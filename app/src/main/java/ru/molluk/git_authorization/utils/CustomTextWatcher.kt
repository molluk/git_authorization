package ru.molluk.git_authorization.utils

import android.text.Editable
import android.text.TextWatcher

interface CustomTextWatcher: TextWatcher {
    override fun afterTextChanged(p0: Editable?) {}

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}