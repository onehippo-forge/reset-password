/*
 * Copyright 2020 Bloomreach Inc. (https://www.bloomreach.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.resetpassword.frontend;

public final class ResetPasswordConst {

    public static final String PASSWORDVALIDATION_LOCATION = "passwordvalidation.location";
    public static final String PASSWORD_RESET_KEY = "passwordResetKey";
    public static final String PASSWORD_RESET_TIMESTAMP = "passwordResetTimestamp";
    public static final String HIPPO_USERS_PATH = "/hippo:configuration/hippo:users/";

    public static final String LOCALE_COOKIE = "loc";
    public static final int LOCALE_COOKIE_MAXAGE = 365 * 24 * 3600; // expire one year from now

    private ResetPasswordConst() {
    }
}
