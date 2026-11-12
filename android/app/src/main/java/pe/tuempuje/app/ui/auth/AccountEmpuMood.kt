package pe.tuempuje.app.ui.auth

import pe.tuempuje.app.ui.components.EmpuMood

internal fun accountEmpuMood(
    loading: Boolean,
    hasError: Boolean,
    emailFocused: Boolean,
    passwordFocused: Boolean,
    passwordVisible: Boolean,
): EmpuMood = when {
    passwordFocused && !passwordVisible -> EmpuMood.PASSWORD
    loading -> EmpuMood.LOADING
    hasError -> EmpuMood.SAD
    emailFocused -> EmpuMood.LOOKING
    else -> EmpuMood.IDLE
}
