/*
 * Copyright 2023-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension.detekt;

/**
 * Kotlin API version used by the code under analysis.
 *
 * @author <a href="https://erik.thauvin.net/">Erik C. Thauvin</a>
 * @since 2.0
 */
public enum ApiVersion {
    V_1_0("1.0"),
    V_1_1("1.1"),
    V_1_2("1.2"),
    V_1_3("1.3"),
    V_1_4("1.4"),
    V_1_5("1.5"),
    V_1_6("1.6"),
    V_1_7("1.7"),
    V_1_8("1.8"),
    V_1_9("1.9"),
    V_2_0("2.0"),
    V_2_1("2.1"),
    V_2_2("2.2"),
    V_2_3("2.3"),
    V_2_4("2.4"),
    V_2_5("2.5");

    private final String version;

    ApiVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return version;
    }

    public String version() {
        return version;
    }
}